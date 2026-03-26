package com.echarge.protocol.ocpp.v16;

import com.echarge.protocol.core.dispatcher.InboundMessage;
import com.echarge.protocol.core.dispatcher.MessageDispatcher;
import com.echarge.protocol.core.dispatcher.OutboundMessage;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.ocpp.common.OcppCallError;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class Ocpp16Dispatcher implements MessageDispatcher {

    private static final String PROTOCOL = "ocpp1.6";
    private final Map<String, Ocpp16ActionHandler<?, ?>> handlers;
    private final Gson gson = new Gson();

    public Ocpp16Dispatcher(List<Ocpp16ActionHandler<?, ?>> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(Ocpp16ActionHandler::action, Function.identity()));
        log.info("OCPP 1.6 dispatcher initialized with {} handlers: {}", handlers.size(), handlers.keySet());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public OutboundMessage dispatch(Session session, InboundMessage message) {
        String action = message.getAction();
        Ocpp16ActionHandler handler = handlers.get(action);

        if (handler == null) {
            log.warn("No handler for OCPP 1.6 action: {}", action);
            return OcppCallError.notImplemented(message.getMessageId(), action);
        }

        try {
            Object request = gson.fromJson(message.getPayload(), handler.requestType());
            Object response = handler.handle(session, request);
            JsonElement responseJson = gson.toJsonTree(response);
            return OutboundMessage.callResult(message.getMessageId(), responseJson);
        } catch (Exception e) {
            log.error("Error handling OCPP 1.6 action {}: {}", action, e.getMessage(), e);
            return OcppCallError.internalError(message.getMessageId(), e.getMessage());
        }
    }

    @Override
    public String supportedProtocol() {
        return PROTOCOL;
    }
}
