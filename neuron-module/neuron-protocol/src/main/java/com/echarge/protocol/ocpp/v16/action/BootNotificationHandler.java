package com.echarge.protocol.ocpp.v16.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.BootNotificationReq;
import com.echarge.protocol.ocpp.v16.model.BootNotificationResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class BootNotificationHandler implements Ocpp16ActionHandler<BootNotificationReq, BootNotificationResp> {

    @Override
    public String action() {
        return OcppAction.BOOT_NOTIFICATION;
    }

    @Override
    public Class<BootNotificationReq> requestType() {
        return BootNotificationReq.class;
    }

    @Override
    public BootNotificationResp handle(Session session, BootNotificationReq request) {
        log.info("[OCPP1.6] BootNotification from {}: vendor={}, model={}, firmware={}",
                session.getChargePointId(), request.getChargePointVendor(),
                request.getChargePointModel(), request.getFirmwareVersion());

        // TODO: persist charge point info, validate registration
        return new BootNotificationResp("Accepted", Instant.now().toString(), 300);
    }
}
