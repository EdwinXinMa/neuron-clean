package com.echarge.protocol.ocpp.v16.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.HeartbeatResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class HeartbeatHandler implements Ocpp16ActionHandler<Object, HeartbeatResp> {

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
        return new HeartbeatResp(Instant.now().toString());
    }
}
