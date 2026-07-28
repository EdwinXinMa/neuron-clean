package com.echarge.modules.app.service;

import com.echarge.modules.app.config.NeuronMailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private NeuronMailProperties mailProperties;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(mailProperties.getEnabled()) && mailSender != null;
    }

    public void sendHtml(String to, String subject, String htmlContent) {
        if (!isEnabled()) {
            log.warn("邮件服务未启用，跳过发送: to={}, subject={}", to, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("邮件发送成功: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("邮件发送失败: to={}, subject={}", to, subject, e);
        }
    }
}
