package com.echarge.protocol.ocpp.v16.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.IdTagInfo;
import com.echarge.protocol.ocpp.v16.model.StopTransactionReq;
import com.echarge.protocol.ocpp.v16.model.StopTransactionResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class StopTransactionHandler implements Ocpp16ActionHandler<StopTransactionReq, StopTransactionResp> {

    @Override
    public String action() {
        return OcppAction.STOP_TRANSACTION;
    }

    @Override
    public Class<StopTransactionReq> requestType() {
        return StopTransactionReq.class;
    }

    @Override
    public StopTransactionResp handle(Session session, StopTransactionReq request) {
        log.info("[OCPP1.6] StopTransaction from {}: txId={}, meterStop={}, reason={}",
                session.getChargePointId(), request.getTransactionId(),
                request.getMeterStop(), request.getReason());

        // TODO: update transaction record in database
        IdTagInfo idTagInfo = new IdTagInfo("Accepted", null, null);
        return new StopTransactionResp(idTagInfo);
    }
}
