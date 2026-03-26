package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

import java.util.List;

/**
 * @author Edwin
 */
@Data
public class StopTransactionReq {
    private int transactionId;
    private String idTag;
    private int meterStop;
    private String timestamp;
    private String reason;
    private List<MeterValue> transactionData;
}
