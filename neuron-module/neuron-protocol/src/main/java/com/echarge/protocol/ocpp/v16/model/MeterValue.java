package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

import java.util.List;

@Data
public class MeterValue {
    private String timestamp;
    private List<SampledValue> sampledValue;
}
