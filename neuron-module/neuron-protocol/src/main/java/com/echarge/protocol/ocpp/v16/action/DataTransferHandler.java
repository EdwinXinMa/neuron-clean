package com.echarge.protocol.ocpp.v16.action;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.DataTransferReq;
import com.echarge.protocol.ocpp.v16.model.DataTransferResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataTransferHandler implements Ocpp16ActionHandler<DataTransferReq, DataTransferResp> {

    @Autowired
    private DeviceEventPublisher eventPublisher;

    @Override
    public String action() {
        return OcppAction.DATA_TRANSFER;
    }

    @Override
    public Class<DataTransferReq> requestType() {
        return DataTransferReq.class;
    }

    @Override
    public DataTransferResp handle(Session session, DataTransferReq request) {
        log.info("[OCPP1.6] DataTransfer from {}: vendor={}, messageId={}",
                session.getChargePointId(), request.getVendorId(), request.getMessageId());

        String messageId = request.getMessageId();

        if ("TopologyReport".equals(messageId)) {
            // 拓扑上报：N3 Lite 上报下挂 ATP III 桩和枪信息
            DeviceEvent event = new DeviceEvent(
                    DeviceEvent.TOPOLOGY_REPORT,
                    session.getChargePointId(),
                    request.getData()
            );
            eventPublisher.publish(event);
            return new DataTransferResp("Accepted", null);
        }

        if ("DLMStatus".equals(messageId)) {
            // DLM 运行数据上报：CT 电流、电压、功率、温度等
            DeviceEvent event = new DeviceEvent(
                    DeviceEvent.DLM_STATUS,
                    session.getChargePointId(),
                    request.getData()
            );
            eventPublisher.publish(event);
            return new DataTransferResp("Accepted", null);
        }

        log.warn("[OCPP1.6] Unknown DataTransfer messageId: {}", messageId);
        return new DataTransferResp("Accepted", null);
    }
}
