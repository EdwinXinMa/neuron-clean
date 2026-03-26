package com.echarge.protocol.ocpp.v16.action;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.HeartbeatResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class HeartbeatHandler implements Ocpp16ActionHandler<Object, HeartbeatResp> {

    @Autowired
    private DeviceEventPublisher eventPublisher;

    @Override
    public String action() {
        return OcppAction.HEARTBEAT;
    }

    @Override
    public Class<Object> requestType() {
        return Object.class;
    }

    @Override
    public HeartbeatResp handle(Session session, Object request) {
        log.debug("[OCPP1.6] Heartbeat from {}", session.getChargePointId());

        // 发布心跳事件
        DeviceEvent event = new DeviceEvent(
                DeviceEvent.HEARTBEAT,
                session.getChargePointId(),
                null
        );
        eventPublisher.publish(event);

        return new HeartbeatResp(Instant.now().toString());
    }
}
