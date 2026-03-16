package com.echarge.protocol.transport;

import com.echarge.protocol.core.dispatcher.InboundMessage;
import com.echarge.protocol.core.dispatcher.OutboundMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Codec for OCPP JSON array format.
 * Inbound:  TextWebSocketFrame → InboundMessage
 * Outbound: OutboundMessage → TextWebSocketFrame
 *
 * OCPP message format:
 * CALL:       [2, "messageId", "action", {payload}]
 * CALLRESULT: [3, "messageId", {payload}]
 * CALLERROR:  [4, "messageId", "errorCode", "errorDescription", {errorDetails}]
 */
@Slf4j
public class OcppFrameCodec extends MessageToMessageCodec<TextWebSocketFrame, OutboundMessage> {

    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame frame, List<Object> out) {
        String text = frame.text();
        log.debug("OCPP IN: {}", text);

        try {
            JsonArray array = JsonParser.parseString(text).getAsJsonArray();
            int messageType = array.get(0).getAsInt();
            String messageId = array.get(1).getAsString();

            switch (messageType) {
                case 2: // CALL
                    String action = array.get(2).getAsString();
                    JsonElement payload = array.size() > 3 ? array.get(3) : null;
                    out.add(new InboundMessage(messageType, messageId, action, payload));
                    break;
                case 3: // CALLRESULT
                    JsonElement resultPayload = array.size() > 2 ? array.get(2) : null;
                    out.add(new InboundMessage(messageType, messageId, null, resultPayload));
                    break;
                case 4: // CALLERROR
                    out.add(new InboundMessage(messageType, messageId,
                            array.get(2).getAsString(), array.size() > 4 ? array.get(4) : null));
                    break;
                default:
                    log.warn("Unknown OCPP message type: {}", messageType);
            }
        } catch (Exception e) {
            log.error("Failed to decode OCPP frame: {}", text, e);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, OutboundMessage msg, List<Object> out) {
        JsonArray array = new JsonArray();
        array.add(msg.getMessageType());
        array.add(msg.getMessageId());

        if (msg.getMessageType() == 3) {
            // CALLRESULT: [3, messageId, payload]
            array.add(msg.getPayload() != null ? msg.getPayload() : new JsonArray());
        } else if (msg.getMessageType() == 4) {
            // CALLERROR: [4, messageId, errorCode, errorDescription, {}]
            array.add(msg.getErrorCode() != null ? msg.getErrorCode() : "InternalError");
            array.add(msg.getErrorDescription() != null ? msg.getErrorDescription() : "");
            array.add(msg.getPayload() != null ? msg.getPayload() : new com.google.gson.JsonObject());
        }

        String text = array.toString();
        log.debug("OCPP OUT: {}", text);
        out.add(new TextWebSocketFrame(text));
    }
}
