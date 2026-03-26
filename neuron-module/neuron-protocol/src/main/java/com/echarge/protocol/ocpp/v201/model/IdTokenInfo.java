package com.echarge.protocol.ocpp.v201.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Edwin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdTokenInfo {
    /** "Accepted", "Blocked", "Expired", "Invalid", "NoCredit",
     *  "NotAllowedTypeEVSE", "NotAtThisLocation", "NotAtThisTime", "Unknown" */
    private String status;
}
