package com.echarge.modules.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "neuron.mail")
public class NeuronMailProperties {

    private Boolean enabled = false;
    private String host;
    private Integer port;
    private String from;
    private String user;
    private String pass;
    private Boolean starttlsEnable = true;
    private Boolean sslEnable = false;
    private Long timeout = 5000L;
    private Long connectionTimeout = 5000L;
}
