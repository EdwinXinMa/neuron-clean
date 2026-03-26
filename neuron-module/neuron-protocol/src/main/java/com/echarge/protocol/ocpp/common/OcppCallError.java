package com.echarge.protocol.ocpp.common;

import com.echarge.protocol.core.dispatcher.OutboundMessage;

/**
 * @author Edwin
 */
public final class OcppCallError {

    private OcppCallError() {}

    public static OutboundMessage notImplemented(String messageId, String action) {
        return OutboundMessage.callError(messageId,
                OcppErrorCode.NOT_IMPLEMENTED.getCode(),
                "Action not implemented: " + action);
    }

    public static OutboundMessage internalError(String messageId, String detail) {
        return OutboundMessage.callError(messageId,
                OcppErrorCode.INTERNAL_ERROR.getCode(), detail);
    }

    public static OutboundMessage protocolError(String messageId, String detail) {
        return OutboundMessage.callError(messageId,
                OcppErrorCode.PROTOCOL_ERROR.getCode(), detail);
    }

    public static OutboundMessage formationViolation(String messageId, String detail) {
        return OutboundMessage.callError(messageId,
                OcppErrorCode.FORMATION_VIOLATION.getCode(), detail);
    }
}
