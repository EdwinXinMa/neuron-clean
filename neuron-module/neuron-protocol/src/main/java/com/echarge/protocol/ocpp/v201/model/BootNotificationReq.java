package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

@Data
public class BootNotificationReq {
    private ChargingStation chargingStation;
    /** "PowerUp", "ApplicationReset", "FirmwareUpdate", "LocalReset", "RemoteReset",
     *  "ScheduledReset", "Triggered", "Unknown", "Watchdog" */
    private String reason;
}
