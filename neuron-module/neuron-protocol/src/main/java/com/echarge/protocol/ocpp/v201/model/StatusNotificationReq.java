package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

/**
 * @author Edwin
 */
@Data
public class StatusNotificationReq {
    private String timestamp;
    /** "Available", "Occupied", "Reserved", "Unavailable", "Faulted" */
    private String connectorStatus;
    private int evseId;
    private int connectorId;
}
