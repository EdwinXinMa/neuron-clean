package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

import java.util.List;

@Data
public class MeterValuesReq {
    private int connectorId;
    private Integer transactionId;
    private List<MeterValue> meterValue;
}
