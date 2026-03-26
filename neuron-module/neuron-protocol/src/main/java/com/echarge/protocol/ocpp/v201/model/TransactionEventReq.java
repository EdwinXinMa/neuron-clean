package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

import java.util.List;

/**
 * @author Edwin
 */
@Data
public class TransactionEventReq {
    /** "Started", "Updated", "Ended" */
    private String eventType;
    private String timestamp;
    /** "Authorized", "CablePluggedIn", "ChargingRateChanged", "ChargingStateChanged",
     *  "Deauthorized", "EnergyLimitReached", "EVCommunicationLost", "EVConnectTimeout",
     *  "MeterValueClock", "MeterValuePeriodic", "TimeLimitReached", "Trigger",
     *  "UnlockCommand", "StopAuthorized", "EVDeparted", "EVDetected", "RemoteStop",
     *  "RemoteStart", "AbnormalCondition", "SignedDataReceived", "ResetCommand" */
    private String triggerReason;
    private int seqNo;
    private TransactionData transactionInfo;
    private IdToken idToken;
    private Integer evse;
    private List<com.echarge.protocol.ocpp.v16.model.MeterValue> meterValue;
}
