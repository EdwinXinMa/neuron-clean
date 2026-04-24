package com.echarge.protocol.ocpp.v16.action;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.IdTagInfo;
import com.echarge.protocol.ocpp.v16.model.StopTransactionReq;
import com.echarge.protocol.ocpp.v16.model.StopTransactionResp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class StopTransactionHandler implements Ocpp16ActionHandler<StopTransactionReq, StopTransactionResp> {

    private final Gson gson = new Gson();

    @Autowired
    private DeviceEventPublisher eventPublisher;

    /** {@inheritDoc} */
    @Override
    public String action() {
        return OcppAction.STOP_TRANSACTION;
    }

    /** {@inheritDoc} */
    @Override
    public Class<StopTransactionReq> requestType() {
        return StopTransactionReq.class;
    }

    /** {@inheritDoc} */
    @Override
    public StopTransactionResp handle(Session session, StopTransactionReq request) {
        log.info("[充电] 设备上报结束充电(StopTransaction) — 设备={}, txId={}, meterStop={}, reason={}",
                session.getChargePointId(), request.getTransactionId(),
                request.getMeterStop(), request.getReason());

        // 发布事件，让 DeviceEventHandler 更新充电会话记录
        JsonObject payload = new JsonObject();
        payload.addProperty("transactionId", request.getTransactionId());
        payload.addProperty("meterStop", request.getMeterStop());
        payload.addProperty("reason", request.getReason());
        payload.addProperty("timestamp", request.getTimestamp());

        DeviceEvent event = new DeviceEvent(
                DeviceEvent.STOP_TRANSACTION,
                session.getChargePointId(),
                payload.toString()
        );
        eventPublisher.publish(event);

        IdTagInfo idTagInfo = new IdTagInfo("Accepted", null, null);
        return new StopTransactionResp(idTagInfo);
    }
}
