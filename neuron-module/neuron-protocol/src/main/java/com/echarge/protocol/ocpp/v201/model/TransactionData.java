package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

@Data
public class TransactionData {
    private String transactionId;
    /** "Idle", "EVConnected", "Charging", "SuspendedEVSE", "SuspendedEV", "Ended" */
    private String chargingState;
    /** "DeAuthorized", "EmergencyStop", "EnergyLimitReached", "EVDisconnected",
     *  "GroundFault", "ImmediateReset", "Local", "LocalOutOfCredit", "MasterPass",
     *  "Other", "OvercurrentFault", "PowerLoss", "PowerQuality", "Reboot",
     *  "Remote", "SOCLimitReached", "StoppedByEV", "TimeLimitReached", "Timeout" */
    private String stoppedReason;
    private Integer remoteStartId;
}
