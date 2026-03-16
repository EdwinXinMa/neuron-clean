package com.echarge.protocol.ocpp.v201.model;

import lombok.Data;

@Data
public class AuthorizeReq {
    private IdToken idToken;
    private Integer evseId;
    private String certificate;
}
