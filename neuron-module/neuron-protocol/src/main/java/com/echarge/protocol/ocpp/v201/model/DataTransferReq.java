package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

/**
 * @author Edwin
 */
@Data
public class DataTransferReq {
    private String vendorId;
    private String messageId;
    private String data;
}
