package com.echarge.protocol.ocpp.v16.action;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.StatusNotificationReq;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class StatusNotificationHandler implements Ocpp16ActionHandler<StatusNotificationReq, Object> {

    @Autowired
    private DeviceEventPublisher eventPublisher;

    private final Gson gson = new Gson();

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

        // 发布状态变更事件
        DeviceEvent event = new DeviceEvent(
                DeviceEvent.STATUS_NOTIFICATION,
                session.getChargePointId(),
                gson.toJson(request)
        );
        eventPublisher.publish(event);

        return new Object();
    }
}
