package com.echarge.protocol.core.dispatcher;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutboundMessage {

    /** 3=CALLRESULT, 4=CALLERROR */
    private int messageType;
    private String messageId;
    private JsonElement payload;
    /** Only for CALLERROR */
    private String errorCode;
    private String errorDescription;

    public static OutboundMessage callResult(String messageId, JsonElement payload) {
        return new OutboundMessage(3, messageId, payload, null, null);
    }

    public static OutboundMessage callError(String messageId, String errorCode, String errorDescription) {
        return new OutboundMessage(4, messageId, null, errorCode, errorDescription);
    }
}
