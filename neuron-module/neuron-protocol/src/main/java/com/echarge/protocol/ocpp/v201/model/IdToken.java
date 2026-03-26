package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

/**
 * @author Edwin
 */
@Data
public class IdToken {
    private String idToken;
    /** "Central", "eMAID", "ISO14443", "ISO15693", "KeyCode", "Local",
     *  "MacAddress", "NoAuthorization" */
    private String type;
}
