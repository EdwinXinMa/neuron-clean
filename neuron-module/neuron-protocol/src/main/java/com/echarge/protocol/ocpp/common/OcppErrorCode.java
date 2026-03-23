package com.echarge.protocol.ocpp.common;

import lombok.Getter;

@Getter
public enum OcppErrorCode {

    /** 请求的Action接收方不认识 */
    NOT_IMPLEMENTED("NotImplemented", "Requested Action is not known by receiver"),
    /** 请求的Action已识别但不支持 */
    NOT_SUPPORTED("NotSupported", "Requested Action is recognized but not supported"),
    /** 内部错误 */
    INTERNAL_ERROR("InternalError", "An internal error occurred"),
    /** 载荷不完整或语法错误 */
    PROTOCOL_ERROR("ProtocolError", "Payload for Action is incomplete or syntactically incorrect"),
    /** 处理Action时发生安全问题 */
    SECURITY_ERROR("SecurityError", "During the processing of Action a security issue occurred"),
    /** 载荷语法不正确 */
    FORMATION_VIOLATION("FormationViolation", "Payload for Action is syntactically incorrect"),
    /** 载荷语法正确但内容无效 */
    PROPERTY_CONSTRAINT_VIOLATION("PropertyConstraintViolation", "Payload is syntactically correct but content is invalid"),
    /** 载荷缺少必填字段 */
    OCCURRENCE_CONSTRAINT_VIOLATION("OccurrenceConstraintViolation", "Payload is missing required fields"),
    /** 载荷字段类型错误 */
    TYPE_CONSTRAINT_VIOLATION("TypeConstraintViolation", "Payload field has wrong type"),
    /** 其他未覆盖的错误 */
    GENERIC_ERROR("GenericError", "Any other error not covered by the above");

    private final String code;
    private final String description;

    OcppErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
