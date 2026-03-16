package com.echarge.protocol.ocpp.v201.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v201.Ocpp201ActionHandler;
import com.echarge.protocol.ocpp.v201.model.IdTokenInfo;
import com.echarge.protocol.ocpp.v201.model.TransactionEventReq;
import com.echarge.protocol.ocpp.v201.model.TransactionEventResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionEventHandler implements Ocpp201ActionHandler<TransactionEventReq, TransactionEventResp> {

    @Override
    public String action() {
        return OcppAction.TRANSACTION_EVENT;
    }

    @Override
    public Class<TransactionEventReq> requestType() {
        return TransactionEventReq.class;
    }

    @Override
    public TransactionEventResp handle(Session session, TransactionEventReq request) {
        log.info("[OCPP2.0.1] TransactionEvent from {}: type={}, trigger={}, txId={}",
                session.getChargePointId(), request.getEventType(),
                request.getTriggerReason(),
                request.getTransactionInfo() != null ? request.getTransactionInfo().getTransactionId() : "N/A");

        // TODO: handle Started/Updated/Ended events, update transaction in DB
        return new TransactionEventResp(new IdTokenInfo("Accepted"));
    }
}
