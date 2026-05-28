package com.echarge.modules.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Firebase Admin SDK 初始化
 * 本地开发：resources/firebase-service-account.json（不提交 Git）
 * 服务器：环境变量 APP_FCM_CREDENTIAL 指向挂载的绝对路径
 */
@Slf4j
@Configuration
public class FcmConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount;
            String credentialPath = System.getenv("APP_FCM_CREDENTIAL");
            if (credentialPath != null && !credentialPath.isBlank()) {
                log.info("[FCM] 从环境变量路径加载凭证: {}", credentialPath);
                serviceAccount = new FileInputStream(credentialPath);
            } else {
                serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream("firebase-service-account.json");
            }
            if (serviceAccount == null) {
                log.warn("[FCM] 凭证文件未找到，FCM 推送不可用");
                return;
            }
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("[FCM] Firebase Admin SDK 初始化成功");
            }
        } catch (Exception e) {
            log.error("[FCM] Firebase Admin SDK 初始化失败", e);
        }
    }
}
