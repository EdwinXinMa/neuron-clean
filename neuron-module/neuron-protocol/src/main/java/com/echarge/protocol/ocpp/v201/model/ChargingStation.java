package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

/**
 * @author Edwin
 */
@Data
public class ChargingStation {
    private String model;
    private String vendorName;
    private String serialNumber;
    private String firmwareVersion;
}
