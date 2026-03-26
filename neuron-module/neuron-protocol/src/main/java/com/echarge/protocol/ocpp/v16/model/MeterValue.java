package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

import java.util.List;

/**
 * @author Edwin
 */
@Data
public class MeterValue {
    private String timestamp;
    private List<SampledValue> sampledValue;
}
