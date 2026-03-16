package com.echarge.protocol.ocpp.v16.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartTransactionResp {
    private int transactionId;
    private IdTagInfo idTagInfo;
}
