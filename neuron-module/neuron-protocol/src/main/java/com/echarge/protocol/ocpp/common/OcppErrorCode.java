package com.echarge.protocol.ocpp.common;

import lombok.Getter;

@Getter
public enum OcppErrorCode {

    NOT_IMPLEMENTED("NotImplemented", "Requested Action is not known by receiver"),
    NOT_SUPPORTED("NotSupported", "Requested Action is recognized but not supported"),
    INTERNAL_ERROR("InternalError", "An internal error occurred"),
    PROTOCOL_ERROR("ProtocolError", "Payload for Action is incomplete or syntactically incorrect"),
    SECURITY_ERROR("SecurityError", "During the processing of Action a security issue occurred"),
    FORMATION_VIOLATION("FormationViolation", "Payload for Action is syntactically incorrect"),
    PROPERTY_CONSTRAINT_VIOLATION("PropertyConstraintViolation", "Payload is syntactically correct but content is invalid"),
    OCCURRENCE_CONSTRAINT_VIOLATION("OccurrenceConstraintViolation", "Payload is missing required fields"),
    TYPE_CONSTRAINT_VIOLATION("TypeConstraintViolation", "Payload field has wrong type"),
    GENERIC_ERROR("GenericError", "Any other error not covered by the above");

    private final String code;
    private final String description;

    OcppErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
