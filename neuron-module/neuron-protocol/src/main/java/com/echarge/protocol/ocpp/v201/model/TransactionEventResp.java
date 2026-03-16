package com.echarge.protocol.ocpp.v201.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventResp {
    private IdTokenInfo idTokenInfo;
}
