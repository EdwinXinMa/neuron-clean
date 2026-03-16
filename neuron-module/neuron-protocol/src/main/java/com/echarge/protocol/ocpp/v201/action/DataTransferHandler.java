package com.echarge.protocol.ocpp.v201.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v201.Ocpp201ActionHandler;
import com.echarge.protocol.ocpp.v201.model.DataTransferReq;
import com.echarge.protocol.ocpp.v16.model.DataTransferResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("v201DataTransferHandler")
public class DataTransferHandler implements Ocpp201ActionHandler<DataTransferReq, DataTransferResp> {

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
        log.info("[OCPP2.0.1] DataTransfer from {}: vendor={}, messageId={}",
                session.getChargePointId(), request.getVendorId(), request.getMessageId());

        // TODO: handle vendor-specific data transfer
        return new DataTransferResp("Accepted", null);
    }
}
