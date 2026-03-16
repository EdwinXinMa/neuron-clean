package com.echarge.protocol.ocpp.v16.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdTagInfo {
    /** "Accepted", "Blocked", "Expired", "Invalid", "ConcurrentTx" */
    private String status;
    private String expiryDate;
    private String parentIdTag;
}
