package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

@Data
public class ChargingStation {
    private String model;
    private String vendorName;
    private String serialNumber;
    private String firmwareVersion;
}
