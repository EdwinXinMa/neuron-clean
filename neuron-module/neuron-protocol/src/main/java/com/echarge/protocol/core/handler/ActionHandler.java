package com.echarge.protocol.core.handler;

import com.echarge.protocol.core.session.Session;

public interface ActionHandler<REQ, RESP> {

    /**
     * The OCPP action name this handler processes (e.g. "BootNotification")
     */
    String action();

    /**
     * The request type class for Gson deserialization
     */
    Class<REQ> requestType();

    /**
     * Handle the request and return a response
     */
    RESP handle(Session session, REQ request);
}
