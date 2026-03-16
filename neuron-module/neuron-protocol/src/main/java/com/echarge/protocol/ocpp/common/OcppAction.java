package com.echarge.protocol.ocpp.common;

public final class OcppAction {

    private OcppAction() {}

    // Common actions (both 1.6 and 2.0.1)
    public static final String BOOT_NOTIFICATION = "BootNotification";
    public static final String HEARTBEAT = "Heartbeat";
    public static final String STATUS_NOTIFICATION = "StatusNotification";
    public static final String AUTHORIZE = "Authorize";
    public static final String METER_VALUES = "MeterValues";
    public static final String DATA_TRANSFER = "DataTransfer";

    // OCPP 1.6 specific
    public static final String START_TRANSACTION = "StartTransaction";
    public static final String STOP_TRANSACTION = "StopTransaction";
    public static final String REMOTE_START_TRANSACTION = "RemoteStartTransaction";
    public static final String REMOTE_STOP_TRANSACTION = "RemoteStopTransaction";
    public static final String RESET = "Reset";
    public static final String CHANGE_AVAILABILITY = "ChangeAvailability";
    public static final String GET_CONFIGURATION = "GetConfiguration";
    public static final String CHANGE_CONFIGURATION = "ChangeConfiguration";
    public static final String CLEAR_CACHE = "ClearCache";
    public static final String UNLOCK_CONNECTOR = "UnlockConnector";

    // OCPP 2.0.1 specific
    public static final String TRANSACTION_EVENT = "TransactionEvent";
    public static final String REQUEST_START_TRANSACTION = "RequestStartTransaction";
    public static final String REQUEST_STOP_TRANSACTION = "RequestStopTransaction";
}
