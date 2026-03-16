package com.echarge.protocol.ocpp.v16.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.StatusNotificationReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StatusNotificationHandler implements Ocpp16ActionHandler<StatusNotificationReq, Object> {

    @Override
    public String action() {
        return OcppAction.STATUS_NOTIFICATION;
    }

    @Override
    public Class<StatusNotificationReq> requestType() {
        return StatusNotificationReq.class;
    }

    @Override
    public Object handle(Session session, StatusNotificationReq request) {
        log.info("[OCPP1.6] StatusNotification from {}: connector={}, status={}, error={}",
                session.getChargePointId(), request.getConnectorId(),
                request.getStatus(), request.getErrorCode());

        // TODO: update connector status in database
        return new Object(); // empty response per OCPP spec
    }
}
