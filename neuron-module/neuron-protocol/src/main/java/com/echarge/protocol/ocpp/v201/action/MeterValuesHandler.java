package com.echarge.protocol.ocpp.v201.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v201.Ocpp201ActionHandler;
import com.echarge.protocol.ocpp.v201.model.MeterValuesReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("v201MeterValuesHandler")
public class MeterValuesHandler implements Ocpp201ActionHandler<MeterValuesReq, Object> {

    @Override
    public String action() {
        return OcppAction.METER_VALUES;
    }

    @Override
    public Class<MeterValuesReq> requestType() {
        return MeterValuesReq.class;
    }

    @Override
    public Object handle(Session session, MeterValuesReq request) {
        log.debug("[OCPP2.0.1] MeterValues from {}: evseId={}, values={}",
                session.getChargePointId(), request.getEvseId(),
                request.getMeterValue() != null ? request.getMeterValue().size() : 0);

        // TODO: store meter values
        return new Object();
    }
}
