package com.echarge.protocol.ocpp.v201.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v201.Ocpp201ActionHandler;
import com.echarge.protocol.ocpp.v201.model.StatusNotificationReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Edwin
 */
@Slf4j
@Component("v201StatusNotificationHandler")
public class StatusNotificationHandler implements Ocpp201ActionHandler<StatusNotificationReq, Object> {

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
        log.info("[OCPP2.0.1] StatusNotification from {}: evseId={}, connectorId={}, status={}",
                session.getChargePointId(), request.getEvseId(),
                request.getConnectorId(), request.getConnectorStatus());

        // TODO: update EVSE/connector status in database
        return new Object();
    }
}
