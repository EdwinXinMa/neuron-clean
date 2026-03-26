package com.echarge.protocol.ocpp.v201.action;

import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppAction;
import com.echarge.protocol.ocpp.v201.Ocpp201ActionHandler;
import com.echarge.protocol.ocpp.v201.model.AuthorizeReq;
import com.echarge.protocol.ocpp.v201.model.AuthorizeResp;
import com.echarge.protocol.ocpp.v201.model.IdTokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Edwin
 */
@Slf4j
@Component("v201AuthorizeHandler")
public class AuthorizeHandler implements Ocpp201ActionHandler<AuthorizeReq, AuthorizeResp> {

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
        log.info("[OCPP2.0.1] Authorize from {}: idToken={}, type={}",
                session.getChargePointId(),
                request.getIdToken() != null ? request.getIdToken().getIdToken() : "N/A",
                request.getIdToken() != null ? request.getIdToken().getType() : "N/A");

        // TODO: validate idToken against database
        return new AuthorizeResp(new IdTokenInfo("Accepted"));
    }
}
