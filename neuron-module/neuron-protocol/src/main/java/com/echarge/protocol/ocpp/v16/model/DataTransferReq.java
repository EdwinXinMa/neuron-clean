package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

@Data
public class DataTransferReq {
    private String vendorId;
    private String messageId;
    private String data;
}
