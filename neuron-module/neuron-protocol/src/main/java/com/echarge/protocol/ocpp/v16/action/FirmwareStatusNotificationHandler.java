package com.echarge.protocol.ocpp.v16.action;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.FirmwareStatusNotificationReq;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class FirmwareStatusNotificationHandler implements Ocpp16ActionHandler<FirmwareStatusNotificationReq, Object> {

    @Autowired
    private DeviceEventPublisher eventPublisher;

    private final Gson gson = new Gson();

    @Override
    public String action() {
        return OcppAction.FIRMWARE_STATUS_NOTIFICATION;
    }

    @Override
    public Class<FirmwareStatusNotificationReq> requestType() {
        return FirmwareStatusNotificationReq.class;
    }

    @Override
    public Object handle(Session session, FirmwareStatusNotificationReq request) {
        log.info("[OCPP1.6] FirmwareStatusNotification from {}: status={}",
                session.getChargePointId(), request.getStatus());

        DeviceEvent event = new DeviceEvent(
                DeviceEvent.FIRMWARE_STATUS,
                session.getChargePointId(),
                gson.toJson(request)
        );
        eventPublisher.publish(event);

        // OCPP 1.6 FirmwareStatusNotification.conf is empty
        return new Object();
    }
}
