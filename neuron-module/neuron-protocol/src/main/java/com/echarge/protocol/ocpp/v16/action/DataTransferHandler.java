package com.echarge.protocol.ocpp.v16.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.DataTransferReq;
import com.echarge.protocol.ocpp.v16.model.DataTransferResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataTransferHandler implements Ocpp16ActionHandler<DataTransferReq, DataTransferResp> {

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

        // TODO: handle vendor-specific data transfer
        return new DataTransferResp("Accepted", null);
    }
}
