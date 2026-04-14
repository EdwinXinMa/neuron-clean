package com.echarge.protocol.ocpp.v16.action;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.IdTagInfo;
import com.echarge.protocol.ocpp.v16.model.StartTransactionReq;
import com.echarge.protocol.ocpp.v16.model.StartTransactionResp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class StartTransactionHandler implements Ocpp16ActionHandler<StartTransactionReq, StartTransactionResp> {

    private final Gson gson = new Gson();

    @Autowired
    private DeviceEventPublisher eventPublisher;

    /** {@inheritDoc} */
    @Override
    public String action() {
        return OcppAction.START_TRANSACTION;
    }

    /** {@inheritDoc} */
    @Override
    public Class<StartTransactionReq> requestType() {
        return StartTransactionReq.class;
    }

    /** {@inheritDoc} */
    @Override
    public StartTransactionResp handle(Session session, StartTransactionReq request) {
        log.info("[OCPP1.6] StartTransaction from {}: connector={}, idTag={}, meterStart={}",
                session.getChargePointId(), request.getConnectorId(),
                request.getIdTag(), request.getMeterStart());

        // 秒级时间戳后6位 + 随机2位，保证唯一且不超 int 范围
        int transactionId = (int) (System.currentTimeMillis() / 1000 % 1_000_000) * 100
                + ThreadLocalRandom.current().nextInt(100);

        // 发布事件，让 DeviceEventHandler 创建充电会话记录
        JsonObject payload = new JsonObject();
        payload.addProperty("transactionId", transactionId);
        payload.addProperty("connectorId", request.getConnectorId());
        payload.addProperty("idTag", request.getIdTag());
        payload.addProperty("meterStart", request.getMeterStart());
        payload.addProperty("timestamp", request.getTimestamp());

        DeviceEvent event = new DeviceEvent(
                DeviceEvent.START_TRANSACTION,
                session.getChargePointId(),
                payload.toString()
        );
        eventPublisher.publish(event);

        IdTagInfo idTagInfo = new IdTagInfo("Accepted", null, null);
        return new StartTransactionResp(transactionId, idTagInfo);
    }
}
