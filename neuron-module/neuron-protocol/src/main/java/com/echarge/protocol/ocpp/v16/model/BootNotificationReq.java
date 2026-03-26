package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

/**
 * @author Edwin
 */
@Data
public class BootNotificationReq {
    private String chargePointVendor;
    private String chargePointModel;
    private String chargePointSerialNumber;
    private String chargeBoxSerialNumber;
    private String firmwareVersion;
    private String iccid;
    private String imsi;
    private String meterType;
    private String meterSerialNumber;
}
