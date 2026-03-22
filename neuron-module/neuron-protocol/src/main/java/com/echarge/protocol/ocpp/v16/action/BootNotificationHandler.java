package com.echarge.protocol.ocpp.v16.action;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.BootNotificationReq;
import com.echarge.protocol.ocpp.v16.model.BootNotificationResp;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class BootNotificationHandler implements Ocpp16ActionHandler<BootNotificationReq, BootNotificationResp> {

    @Autowired
    private DeviceEventPublisher eventPublisher;

    private final Gson gson = new Gson();

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
        log.info("[OCPP1.6] BootNotification from {}: vendor={}, model={}, sn={}, firmware={}",
                session.getChargePointId(), request.getChargePointVendor(),
                request.getChargePointModel(), request.getChargePointSerialNumber(),
                request.getFirmwareVersion());

        // 发布设备上线事件
        DeviceEvent event = new DeviceEvent(
                DeviceEvent.BOOT_NOTIFICATION,
                session.getChargePointId(),
                gson.toJson(request)
        );
        eventPublisher.publish(event);

        return new BootNotificationResp("Accepted", Instant.now().toString(), 300);
    }
}
