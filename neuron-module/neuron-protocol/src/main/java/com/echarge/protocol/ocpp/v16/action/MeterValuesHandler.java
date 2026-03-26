package com.echarge.protocol.ocpp.v16.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.MeterValuesReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class MeterValuesHandler implements Ocpp16ActionHandler<MeterValuesReq, Object> {

    /** {@inheritDoc} */
    @Override
    public String action() {
        return OcppAction.METER_VALUES;
    }

    /** {@inheritDoc} */
    @Override
    public Class<MeterValuesReq> requestType() {
        return MeterValuesReq.class;
    }

    /** {@inheritDoc} */
    @Override
    public Object handle(Session session, MeterValuesReq request) {
        log.debug("[OCPP1.6] MeterValues from {}: connector={}, txId={}, values={}",
                session.getChargePointId(), request.getConnectorId(),
                request.getTransactionId(),
                request.getMeterValue() != null ? request.getMeterValue().size() : 0);

        // TODO: store meter values for billing/monitoring
        // empty response per OCPP spec
        return new Object();
    }
}
