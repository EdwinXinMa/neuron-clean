package com.echarge.protocol.ocpp.v16.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Edwin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTransferResp {
    /** "Accepted", "Rejected", "UnknownMessageId", "UnknownVendorId" */
    private String status;
    private String data;
}
