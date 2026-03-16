package com.echarge.protocol.ocpp.v16.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BootNotificationResp {
    /** "Accepted", "Pending", "Rejected" */
    private String status;
    private String currentTime;
    /** Heartbeat interval in seconds */
    private int interval;
}
