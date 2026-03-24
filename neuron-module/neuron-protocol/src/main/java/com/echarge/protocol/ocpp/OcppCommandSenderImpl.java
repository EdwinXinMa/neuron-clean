package com.echarge.protocol.ocpp;

import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.core.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OcppCommandSenderImpl implements OcppCommandSender {

    @Autowired
    private SessionManager sessionManager;

    @Override
    public boolean isDeviceConnected(String chargePointId) {
        Session session = sessionManager.getByChargePointId(chargePointId);
        return session != null && session.isActive();
    }

    @Override
    public void sendCall(String chargePointId, String message) {
        sessionManager.sendMessage(chargePointId, message);
    }
}
