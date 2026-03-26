package com.echarge.protocol.ocpp.v201.model;

import com.echarge.protocol.ocpp.v16.model.MeterValue;
import lombok.Data;

import java.util.List;

/**
 * @author Edwin
 */
@Data
public class MeterValuesReq {
    private int evseId;
    private List<MeterValue> meterValue;
}
