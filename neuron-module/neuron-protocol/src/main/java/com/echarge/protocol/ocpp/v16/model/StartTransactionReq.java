package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

/**
 * @author Edwin
 */
@Data
public class StartTransactionReq {
    private int connectorId;
    private String idTag;
    private int meterStart;
    private String timestamp;
    private Integer reservationId;
}
