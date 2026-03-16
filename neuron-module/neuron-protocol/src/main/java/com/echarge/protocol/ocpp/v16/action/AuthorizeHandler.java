package com.echarge.protocol.ocpp.v16.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v16.Ocpp16ActionHandler;
import com.echarge.protocol.ocpp.v16.model.AuthorizeReq;
import com.echarge.protocol.ocpp.v16.model.AuthorizeResp;
import com.echarge.protocol.ocpp.v16.model.IdTagInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthorizeHandler implements Ocpp16ActionHandler<AuthorizeReq, AuthorizeResp> {

    @Override
    public String action() {
        return OcppAction.AUTHORIZE;
    }

    @Override
    public Class<AuthorizeReq> requestType() {
        return AuthorizeReq.class;
    }

    @Override
    public AuthorizeResp handle(Session session, AuthorizeReq request) {
        log.info("[OCPP1.6] Authorize from {}: idTag={}", session.getChargePointId(), request.getIdTag());

        // TODO: validate idTag against database
        IdTagInfo idTagInfo = new IdTagInfo("Accepted", null, null);
        return new AuthorizeResp(idTagInfo);
    }
}
