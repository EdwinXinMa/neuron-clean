package com.echarge.protocol.ocpp.v16.model;

import lombok.Data;

/**
 * @author Edwin
 */
@Data
public class SampledValue {
    private String value;
    private String context;
    private String format;
    private String measurand;
    private String phase;
    private String location;
    private String unit;
}
