package com.echarge.protocol.core.dispatcher;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Edwin
 */
@Data
@AllArgsConstructor
public class InboundMessage {

    /** OCPP message type: 2=CALL, 3=CALLRESULT, 4=CALLERROR */
    private int messageType;
    private String messageId;
    private String action;
    private JsonElement payload;
}
