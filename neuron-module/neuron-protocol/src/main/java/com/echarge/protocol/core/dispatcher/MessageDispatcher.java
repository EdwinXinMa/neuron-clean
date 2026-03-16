package com.echarge.protocol.core.dispatcher;

import com.echarge.protocol.core.session.Session;

public interface MessageDispatcher {

    /**
     * Dispatch an inbound CALL message to the appropriate handler
     */
    OutboundMessage dispatch(Session session, InboundMessage message);

    /**
     * The OCPP sub-protocol this dispatcher handles (e.g. "ocpp1.6", "ocpp2.0.1")
     */
    String supportedProtocol();
}
