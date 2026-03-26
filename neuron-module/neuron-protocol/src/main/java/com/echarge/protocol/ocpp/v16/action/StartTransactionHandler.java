package com.echarge.protocol.ocpp.v16.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.IdTagInfo;
import com.echarge.protocol.ocpp.v16.model.StartTransactionReq;
import com.echarge.protocol.ocpp.v16.model.StartTransactionResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class StartTransactionHandler implements Ocpp16ActionHandler<StartTransactionReq, StartTransactionResp> {

    private final AtomicInteger transactionIdGenerator = new AtomicInteger(1);

    @Override
    public String action() {
        return OcppAction.START_TRANSACTION;
    }

    @Override
    public Class<StartTransactionReq> requestType() {
        return StartTransactionReq.class;
    }

    @Override
    public StartTransactionResp handle(Session session, StartTransactionReq request) {
        log.info("[OCPP1.6] StartTransaction from {}: connector={}, idTag={}, meterStart={}",
                session.getChargePointId(), request.getConnectorId(),
                request.getIdTag(), request.getMeterStart());

        // TODO: create transaction record in database, generate real transactionId
        int transactionId = transactionIdGenerator.getAndIncrement();
        IdTagInfo idTagInfo = new IdTagInfo("Accepted", null, null);
        return new StartTransactionResp(transactionId, idTagInfo);
    }
}
