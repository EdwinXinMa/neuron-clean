package com.echarge.protocol.ocpp;

import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.protocol.core.dispatcher.InboundMessage;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.core.session.SessionManager;
import com.echarge.protocol.ocpp.common.PendingCallManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class OcppCommandSenderImpl implements OcppCommandSender {

    @Autowired
    private SessionManager sessionManager;

    /** {@inheritDoc} */
    @Override
    public boolean isDeviceConnected(String chargePointId) {
        Session session = sessionManager.getByChargePointId(chargePointId);
        return session != null && session.isActive();
    }

    /** {@inheritDoc} */
    @Override
    public void sendCall(String chargePointId, String message) {
        sessionManager.sendMessage(chargePointId, message);
    }

    /** {@inheritDoc} */
    @Override
    public String sendCallAndWait(String chargePointId, String message, String messageId, long timeoutSeconds) {
        CompletableFuture<InboundMessage> future = PendingCallManager.register(messageId, timeoutSeconds);
        sessionManager.sendMessage(chargePointId, message);
        try {
            InboundMessage response = future.get(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
            if (response.getMessageType() == 3) {
                // CALLRESULT
                return response.getPayload().toString();
            } else {
                // CALLERROR
                log.warn("[OCPP] CALLERROR response for {}: {}", messageId, response.getPayload());
                return null;
            }
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("[OCPP] Timeout waiting for response: messageId={}", messageId);
            return null;
        } catch (Exception e) {
            log.error("[OCPP] Error waiting for response: messageId={}, error={}", messageId, e.getMessage());
            return null;
        }
    }
}
