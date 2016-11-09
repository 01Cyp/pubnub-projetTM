package com.pubnub.api.endpoints.pubsub;

import com.google.gson.Gson;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.PublishSequenceManager;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.vendor.Crypto;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class Publish extends Endpoint<List<Object>, PNPublishResult> {

    @Setter
    private Object message;
    @Setter
    private String channel;
    @Setter
    private Boolean shouldStore;
    @Setter
    private Boolean usePOST;
    @Setter
    private Object meta;
    @Setter
    private Boolean replicate;

    private PublishSequenceManager publishSequenceManager;

    public Publish(PubNub pubnub, PublishSequenceManager providedPublishSequenceManager, Retrofit retrofit) {
        super(pubnub, retrofit);

        this.publishSequenceManager = providedPublishSequenceManager;
        this.replicate = true;
    }

    @Override
    protected final void validateParams() throws PubNubException {
        if (message == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_MESSAGE_MISSING).build();
        }
        if (channel == null || channel.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getPublishKey() == null || this.getPubnub().getConfiguration().getPublishKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PUBLISH_KEY_MISSING).build();
        }
    }

    @Override
    protected final Call<List<Object>> doWork(Map<String, String> params) throws PubNubException {
        String stringifiedMessage = new Gson().toJson(message);;
        String stringifiedMeta;

        if (meta != null) {
            stringifiedMeta = new Gson().toJson(meta);
            stringifiedMeta = PubNubUtil.urlEncode(stringifiedMeta);
            params.put("meta", stringifiedMeta);
        }

        if (shouldStore != null) {
            if (shouldStore) {
                params.put("store", "1");
            } else {
                params.put("store", "0");
            }
        }

        params.put("seqn", String.valueOf(publishSequenceManager.getNextSequence()));

        if (!replicate) {
            params.put("norep", "true");
        }

        if (this.getPubnub().getConfiguration().getCipherKey() != null) {
            Crypto crypto = new Crypto(this.getPubnub().getConfiguration().getCipherKey());
            stringifiedMessage = crypto.encrypt(stringifiedMessage).replace("\n", "");
        }

        PubSubService service = this.getRetrofit().create(PubSubService.class);

        if (usePOST != null && usePOST) {
            Object payloadToSend;

            if (this.getPubnub().getConfiguration().getCipherKey() != null) {
                payloadToSend = stringifiedMessage;
            } else {
                payloadToSend = message;
            }

            return service.publishWithPost(this.getPubnub().getConfiguration().getPublishKey(),
                    this.getPubnub().getConfiguration().getSubscribeKey(),
                    channel, payloadToSend, params);
        } else {

            if (this.getPubnub().getConfiguration().getCipherKey() != null) {
                stringifiedMessage = "\"".concat(stringifiedMessage).concat("\"");
            }

            stringifiedMessage = PubNubUtil.urlEncode(stringifiedMessage);

            return service.publish(this.getPubnub().getConfiguration().getPublishKey(),
                    this.getPubnub().getConfiguration().getSubscribeKey(),
                    channel, stringifiedMessage, params);
        }
    }

    @Override
    protected final PNPublishResult createResponse(final Response<List<Object>> input) throws PubNubException {
        PNPublishResult.PNPublishResultBuilder pnPublishResult = PNPublishResult.builder();
        pnPublishResult.timetoken(Long.valueOf(input.body().get(2).toString()));

        return pnPublishResult.build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNPublishOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

}
