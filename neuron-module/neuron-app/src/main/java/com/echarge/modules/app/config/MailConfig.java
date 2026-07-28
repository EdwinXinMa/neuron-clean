package com.echarge.modules.app.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(NeuronMailProperties.class)
public class MailConfig {

    @Bean
    @ConditionalOnProperty(value = "neuron.mail.enabled", havingValue = "true")
    public JavaMailSender mailSender(NeuronMailProperties p) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(p.getHost());
        sender.setPort(p.getPort());
        sender.setUsername(p.getUser());
        sender.setPassword(p.getPass());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(p.getStarttlsEnable()));
        props.put("mail.smtp.ssl.enable", String.valueOf(p.getSslEnable()));
        props.put("mail.smtp.timeout", String.valueOf(p.getTimeout()));
        props.put("mail.smtp.connectiontimeout", String.valueOf(p.getConnectionTimeout()));
        props.put("mail.smtp.writetimeout", String.valueOf(p.getTimeout()));

        return sender;
    }
}
