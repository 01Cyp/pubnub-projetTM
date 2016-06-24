package com.pubnub.api.endpoints.presence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.builder.dto.StateOperation;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.SubscriptionManager;
import com.pubnub.api.models.consumer.presence.PNSetStateResult;
import com.pubnub.api.models.server.Envelope;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Accessors(chain = true, fluent = true)
public class SetState extends Endpoint<Envelope<Map<String, Object>>, PNSetStateResult> {

    @Getter(AccessLevel.NONE)
    private SubscriptionManager subscriptionManager;

    @Setter
    private List<String> channels;
    @Setter
    private List<String> channelGroups;
    @Setter
    private Object state;
    @Setter
    private String uuid;

    public SetState(PubNub pubnubInstance, SubscriptionManager subscriptionManagerInstance) {
        super(pubnubInstance);
        this.subscriptionManager = subscriptionManagerInstance;
        channels = new ArrayList<>();
        channelGroups = new ArrayList<>();
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (state == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_STATE_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (channels.size() == 0 && channelGroups.size() == 0) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_AND_GROUP_MISSING).build();
        }
    }

    @Override
    protected Call<Envelope<Map<String, Object>>> doWork(Map<String, String> params) throws PubNubException {
        ObjectWriter ow = new ObjectMapper().writer();
        String selectedUUID = uuid != null ? uuid : this.getPubnub().getConfiguration().getUuid();
        String stringifiedState;

        // only store the state change if we are modifying it for ourselves.
        if (selectedUUID.equals(this.getPubnub().getConfiguration().getUuid())) {
            StateOperation stateOperation = StateOperation.builder()
                    .state(state)
                    .channels(channels)
                    .channelGroups(channelGroups)
                    .build();
            subscriptionManager.adaptStateBuilder(stateOperation);
        }

        PresenceService service = this.createRetrofit().create(PresenceService.class);

        if (channelGroups.size() > 0) {
            params.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        try {
            stringifiedState = ow.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).errormsg(e.getMessage()).build();
        }

        stringifiedState = PubNubUtil.urlEncode(stringifiedState);
        params.put("state", stringifiedState);

        String channelCSV = channels.size() > 0 ? PubNubUtil.joinString(channels, ",") : ",";

        return service.setState(this.getPubnub().getConfiguration().getSubscribeKey(), channelCSV, selectedUUID, params);
    }

    @Override
    protected PNSetStateResult createResponse(Response<Envelope<Map<String, Object>>> input) throws PubNubException {

        if (input.body() == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR).build();
        }

        PNSetStateResult.PNSetStateResultBuilder pnSetStateResult = PNSetStateResult.builder()
                .state(input.body().getPayload());

        return pnSetStateResult.build();
    }

    protected int getConnectTimeout() {
        return this.getPubnub().getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return this.getPubnub().getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNSetStateOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

}
