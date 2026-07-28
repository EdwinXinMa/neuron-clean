package com.echarge.modules.app.service;

import com.echarge.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
public class EmailCodeService {

    private static final int CODE_LENGTH = 6;
    private static final long CODE_EXPIRE_SECONDS = 300; // 5 分钟
    private static final long RATE_LIMIT_SECONDS = 60;   // 同邮箱 60 秒内不能重复发

    private static final String CODE_KEY_PREFIX = "app:email:code:";
    private static final String RATE_KEY_PREFIX = "app:email:rate:";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MailService mailService;

    /**
     * 发送验证码
     * @param email   目标邮箱
     * @param purpose register / forgotPassword
     * @return null 成功，非 null 为错误消息
     */
    public String sendCode(String email, String purpose) {
        if (!mailService.isEnabled()) {
            return "邮件服务未启用";
        }

        String rateKey = RATE_KEY_PREFIX + purpose + ":" + email;
        if (redisUtil.hasKey(rateKey)) {
            return "发送过于频繁，请稍后再试";
        }

        String code = generateCode();
        String codeKey = CODE_KEY_PREFIX + purpose + ":" + email;
        redisUtil.set(codeKey, code, CODE_EXPIRE_SECONDS);
        redisUtil.set(rateKey, "1", RATE_LIMIT_SECONDS);

        String subject = "register".equals(purpose)
                ? "N3 Lite - Registration Verification Code"
                : "N3 Lite - Password Reset Verification Code";
        String html = buildCodeHtml(code, purpose);
        mailService.sendHtml(email, subject, html);

        log.info("验证码已发送: email={}, purpose={}", email, purpose);
        return null;
    }

    /**
     * 校验验证码，通过后立即删除
     * @return true 校验通过
     */
    public boolean verifyCode(String email, String purpose, String code) {
        String codeKey = CODE_KEY_PREFIX + purpose + ":" + email;
        Object stored = redisUtil.get(codeKey);
        if (stored != null && stored.toString().equals(code)) {
            redisUtil.del(codeKey);
            return true;
        }
        return false;
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt((int) Math.pow(10, CODE_LENGTH));
        return String.format("%0" + CODE_LENGTH + "d", num);
    }

    private String buildCodeHtml(String code, String purpose) {
        String title = "register".equals(purpose) ? "Registration Verification" : "Password Reset";
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f4f4;">
                <div style="max-width:600px;margin:20px auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                  <div style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:30px;text-align:center;">
                    <h1 style="color:#fff;margin:0;font-size:24px;">N3 Lite Cloud</h1>
                  </div>
                  <div style="padding:40px 30px;">
                    <h2 style="color:#333;margin:0 0 20px;font-size:20px;">%s</h2>
                    <p style="color:#666;line-height:1.6;margin:0 0 20px;">Your verification code is:</p>
                    <div style="background:#f8f9fa;border-left:4px solid #667eea;padding:20px;margin:0 0 30px;text-align:center;">
                      <span style="font-size:32px;font-weight:bold;color:#667eea;letter-spacing:8px;font-family:Consolas,monospace;">%s</span>
                    </div>
                    <p style="color:#666;line-height:1.6;margin:0 0 10px;"><strong>Valid for:</strong> 5 minutes</p>
                    <p style="color:#999;font-size:14px;line-height:1.6;margin:20px 0 0;">If you did not request this code, please ignore this email.</p>
                  </div>
                  <div style="background:#f8f9fa;padding:20px 30px;text-align:center;border-top:1px solid #e9ecef;">
                    <p style="color:#999;font-size:12px;margin:0;">This is an automated email. Please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(title, code);
    }
}
