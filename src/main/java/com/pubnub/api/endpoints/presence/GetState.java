package com.pubnub.api.endpoints.presence;

import com.fasterxml.jackson.databind.JsonNode;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.presence.PNGetStateResult;
import com.pubnub.api.models.server.Envelope;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class GetState extends Endpoint<Envelope<JsonNode>, PNGetStateResult> {

    @Setter
    private List<String> channels;
    @Setter
    private List<String> channelGroups;
    @Setter
    private String uuid;

    public GetState(PubNub pubnub, Retrofit retrofit) {
        super(pubnub, retrofit);
        channels = new ArrayList<>();
        channelGroups = new ArrayList<>();
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (channels.size() == 0 && channelGroups.size() == 0) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_AND_GROUP_MISSING).build();
        }
    }

    @Override
    protected Call<Envelope<JsonNode>> doWork(Map<String, String> params) {
        PresenceService service = this.getRetrofit().create(PresenceService.class);

        if (channelGroups.size() > 0) {
            params.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        String channelCSV = channels.size() > 0 ? PubNubUtil.joinString(channels, ",") : ",";

        String selectedUUID = uuid != null ? uuid : this.getPubnub().getConfiguration().getUuid();

        return service.getState(this.getPubnub().getConfiguration().getSubscribeKey(), channelCSV, selectedUUID, params);
    }

    @Override
    protected PNGetStateResult createResponse(final Response<Envelope<JsonNode>> input) throws PubNubException {
        Map<String, JsonNode> stateMappings = new HashMap<>();

        if (channels.size() == 1 && channelGroups.size() == 0) {
            stateMappings.put(channels.get(0), input.body().getPayload());
        } else {
            for (Iterator<Map.Entry<String, JsonNode>> it = input.body().getPayload().fields(); it.hasNext();) {
                Map.Entry<String, JsonNode> stateMapping = it.next();
                stateMappings.put(stateMapping.getKey(), stateMapping.getValue());
            }
        }

        return PNGetStateResult.builder().stateByUUID(stateMappings).build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNGetState;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

}
