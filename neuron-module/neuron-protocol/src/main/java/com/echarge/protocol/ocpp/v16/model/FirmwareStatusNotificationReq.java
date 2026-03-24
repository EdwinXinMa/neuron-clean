package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

@Data
public class FirmwareStatusNotificationReq {
    /**
     * OCPP 1.6 FirmwareStatusNotification status values:
     * Downloaded, DownloadFailed, Downloading, Idle, InstallationFailed, Installing, Installed
     */
    private String status;
}
