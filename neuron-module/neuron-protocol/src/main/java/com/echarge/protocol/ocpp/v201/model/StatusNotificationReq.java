package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

@Data
public class StatusNotificationReq {
    private String timestamp;
    /** "Available", "Occupied", "Reserved", "Unavailable", "Faulted" */
    private String connectorStatus;
    private int evseId;
    private int connectorId;
}
