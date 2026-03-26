package com.echarge.protocol.ocpp.v201.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v201.Ocpp201ActionHandler;
import com.echarge.protocol.ocpp.v201.model.BootNotificationReq;
import com.echarge.protocol.ocpp.v201.model.BootNotificationResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * @author Edwin
 */
@Slf4j
@Component("v201BootNotificationHandler")
public class BootNotificationHandler implements Ocpp201ActionHandler<BootNotificationReq, BootNotificationResp> {

    /** {@inheritDoc} */
    @Override
    public String action() {
        return OcppAction.BOOT_NOTIFICATION;
    }

    /** {@inheritDoc} */
    @Override
    public Class<BootNotificationReq> requestType() {
        return BootNotificationReq.class;
    }

    /** {@inheritDoc} */
    @Override
    public BootNotificationResp handle(Session session, BootNotificationReq request) {
        log.info("[OCPP2.0.1] BootNotification from {}: vendor={}, model={}, reason={}",
                session.getChargePointId(),
                request.getChargingStation() != null ? request.getChargingStation().getVendorName() : "N/A",
                request.getChargingStation() != null ? request.getChargingStation().getModel() : "N/A",
                request.getReason());

        // TODO: persist charging station info
        return new BootNotificationResp("Accepted", Instant.now().toString(), 60);
    }
}
