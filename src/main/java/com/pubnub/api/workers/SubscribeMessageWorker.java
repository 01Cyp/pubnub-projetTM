package com.pubnub.api.workers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.managers.ListenerManager;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.models.consumer.PNErrorData;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.server.PresenceEnvelope;
import com.pubnub.api.models.server.PublishMetaData;
import com.pubnub.api.models.server.SubscribeMessage;
import com.pubnub.api.vendor.Crypto;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;


@Slf4j
public class SubscribeMessageWorker implements Runnable {

    private PubNub pubnub;
    private ListenerManager listenerManager;
    private LinkedBlockingQueue<SubscribeMessage> queue;

    private boolean isRunning;

    public SubscribeMessageWorker(PubNub pubnubInstance, ListenerManager listenerManagerInstance, LinkedBlockingQueue<SubscribeMessage> queueInstance) {
        this.pubnub = pubnubInstance;
        this.listenerManager = listenerManagerInstance;
        this.queue = queueInstance;
    }

    @Override
    public void run() {
        takeMessage();
    }


    private void takeMessage() {
        this.isRunning = true;

        while (this.isRunning) {
            try {
                this.processIncomingPayload(this.queue.take());
            } catch (InterruptedException e) {
                this.isRunning = false;
                log.warn("take message interrupted", e);
            }
        }
    }

    private JsonNode processMessage(final JsonNode input) {
        // if we do not have a crypto key, there is no way to process the node; let's return.
        if (pubnub.getConfiguration().getCipherKey() == null) {
            return input;
        }

        Crypto crypto = new Crypto(pubnub.getConfiguration().getCipherKey());
        MapperManager mapper = this.pubnub.getMapper();
        String inputText;
        String outputText;
        JsonNode outputObject;

        if (input.isObject() && input.has("pn_other")) {
            inputText = input.get("pn_other").asText();
        } else {
            inputText = input.asText();
        }

        try {
            outputText = crypto.decrypt(inputText);
        } catch (PubNubException e) {
            PNStatus pnStatus = PNStatus.builder().error(true)
                    .errorData(new PNErrorData(e.getMessage(), e))
                    .operation(PNOperationType.PNSubscribeOperation)
                    .category(PNStatusCategory.PNDecryptionErrorCategory)
                    .build();

            listenerManager.announce(pnStatus);
            return null;
        }

        try {
            outputObject = mapper.fromJson(outputText, JsonNode.class);
        } catch (PubNubException e) {
            PNStatus pnStatus = PNStatus.builder().error(true)
                    .errorData(new PNErrorData(e.getMessage(), e))
                    .operation(PNOperationType.PNSubscribeOperation)
                    .category(PNStatusCategory.PNMalformedResponseCategory)
                    .build();

            listenerManager.announce(pnStatus);
            return null;
        }

        // inject the decoded response into the payload
        if (input.isObject() && input.has("pn_other")) {
            ObjectNode objectNode = (ObjectNode) input;
            objectNode.set("pn_other", outputObject);
            outputObject = objectNode;
        }

        return outputObject;
    }

    private void processIncomingPayload(final SubscribeMessage message) {
        MapperManager mapper = this.pubnub.getMapper();
        String channel = message.getChannel();
        String subscriptionMatch = message.getSubscriptionMatch();
        PublishMetaData publishMetaData = message.getPublishMetaData();

        if (channel != null && channel.equals(subscriptionMatch)) {
            subscriptionMatch = null;
        }

        if (message.getChannel().endsWith("-pnpres")) {
            PresenceEnvelope presencePayload = mapper.convertValue(message.getPayload(), PresenceEnvelope.class);

            String strippedPresenceChannel = null;
            String strippedPresenceSubscription = null;

            if (channel != null) {
                strippedPresenceChannel = PubNubUtil.replaceLast(channel, "-pnpres", "");
            }
            if (subscriptionMatch != null) {
                strippedPresenceSubscription = PubNubUtil.replaceLast(subscriptionMatch, "-pnpres", "");
            }

            PNPresenceEventResult pnPresenceEventResult = PNPresenceEventResult.builder()
                    .event(presencePayload.getAction())
                    // deprecated
                    .actualChannel((subscriptionMatch != null) ? channel : null)
                    .subscribedChannel(subscriptionMatch != null ? subscriptionMatch : channel)
                    // deprecated
                    .channel(strippedPresenceChannel)
                    .subscription(strippedPresenceSubscription)
                    .state(presencePayload.getData())
                    .timetoken(publishMetaData.getPublishTimetoken())
                    .occupancy(presencePayload.getOccupancy())
                    .uuid(presencePayload.getUuid())
                    .timestamp(presencePayload.getTimestamp())
                    .build();

            listenerManager.announce(pnPresenceEventResult);
        } else {
            JsonNode extractedMessage = processMessage(message.getPayload());

            if (extractedMessage == null) {
                log.debug("unable to parse payload on #processIncomingMessages");
            }

            PNMessageResult pnMessageResult = PNMessageResult.builder()
                    .message(extractedMessage)
                    // deprecated
                    .actualChannel((subscriptionMatch != null) ? channel : null)
                    .subscribedChannel(subscriptionMatch != null ? subscriptionMatch : channel)
                    // deprecated
                    .channel(channel)
                    .subscription(subscriptionMatch)
                    .timetoken(publishMetaData.getPublishTimetoken())
                    .publisher(message.getIssuingClientId())
                    .build();


            listenerManager.announce(pnMessageResult);
        }
    }

}
