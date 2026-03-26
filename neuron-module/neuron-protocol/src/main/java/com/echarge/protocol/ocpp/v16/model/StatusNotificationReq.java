package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

/**
 * @author Edwin
 */
@Data
public class StatusNotificationReq {
    private int connectorId;
    /** Available, Preparing, Charging, SuspendedEVSE, SuspendedEV, Finishing, Reserved, Unavailable, Faulted */
    private String status;
    private String errorCode;
    private String info;
    private String timestamp;
    private String vendorId;
    private String vendorErrorCode;
}
