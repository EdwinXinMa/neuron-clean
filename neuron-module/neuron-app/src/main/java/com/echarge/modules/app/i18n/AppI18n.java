package com.echarge.modules.app.i18n;

import java.util.HashMap;
import java.util.Map;

/**
 * App 端 i18n 翻译表（简体 → 英文 / 繁体）
 * 支持精确匹配和前缀匹配（用于动态拼接的错误消息）
 * @author Edwin
 */
public class AppI18n {

    /** key=简体, value=[英文, 繁体] */
    private static final Map<String, String[]> MESSAGES = new HashMap<>();

    static {
        // ── Token / 认证 ──────────────────────────────────────────
        put("未提供 Token",                "Token not provided",                      "未提供 Token");
        put("Token 无效",                  "Invalid token",                           "Token 無效");
        put("Token 已过期或签名无效",        "Token expired or invalid signature",      "Token 已過期或簽名無效");

        // ── 用户 ──────────────────────────────────────────────────
        put("用户不存在",                  "User not found",                          "用戶不存在");
        put("账号已禁用",                  "Account is disabled",                     "帳號已被停用");
        put("密码错误",                    "Incorrect password",                      "密碼錯誤");
        put("邮箱、密码、姓名不能为空",      "Email, password and name are required",   "電郵、密碼及姓名為必填項");
        put("该邮箱已注册",                "Email already registered",                "此電郵已被註冊");
        put("注册成功",                    "Registration successful",                 "註冊成功");
        put("邮箱和密码不能为空",           "Email and password are required",         "電郵及密碼為必填項");

        // ── 设备绑定 ───────────────────────────────────────────────
        put("设备序列号不能为空",           "Device SN is required",                   "設備序列號為必填項");
        put("该设备已绑定",                "Device already bound",                    "該設備已綁定");
        put("绑定成功",                    "Device bound successfully",               "設備綁定成功");
        put("解绑成功",                    "Device unbound successfully",             "設備解綁成功");

        // ── 固件 ───────────────────────────────────────────────────
        put("currentVersion 不能为空",     "currentVersion is required",              "currentVersion 為必填項");
        put("暂无已发布的固件版本",          "No firmware version available",           "暫無已發佈的韌體版本");
        put("固件文件不存在",              "Firmware file not found",                 "韌體文件不存在");
        put("固件文件不能为空",            "Firmware file is required",               "韌體文件為必填項");
        put("该设备未绑定到当前用户",        "Device not bound to current user",        "該設備未綁定至當前用戶");
        put("设备不在线，无法升级",          "Device is offline, cannot upgrade",       "設備不在線，無法升級");
        put("设备 OCPP 连接不存在，无法下发升级指令", "Device OCPP connection not found, cannot send upgrade command", "設備 OCPP 連接不存在，無法下發升級指令");
        put("文件名格式不正确，应为 N3Lite-X.Y.Z.bin 或 N3Lite-X.Y.Z_XXXXX.bin",
                "Invalid filename format, expected N3Lite-X.Y.Z.bin or N3Lite-X.Y.Z_XXXXX.bin",
                "文件名格式不正確，應為 N3Lite-X.Y.Z.bin 或 N3Lite-X.Y.Z_XXXXX.bin");
        put("升级任务已创建",              "Upgrade task created",                    "升級任務已建立");
        put("任务不存在",                  "Task not found",                          "任務不存在");
        put("无权查看该任务",              "No permission to view this task",         "無權查看此任務");

        // ── 固件（动态前缀，前缀匹配用）────────────────────────────
        put("生成下载链接失败: ",          "Failed to generate download link: ",      "生成下載連結失敗: ");
        put("文件上传失败: ",             "File upload failed: ",                    "文件上傳失敗: ");
        put("生成固件下载链接失败: ",       "Failed to generate firmware download link: ", "生成韌體下載連結失敗: ");
        put("下发升级指令失败: ",          "Failed to send upgrade command: ",        "下發升級指令失敗: ");

        // ── RPC / 设备状态 ─────────────────────────────────────────
        put("设备离线",                    "Device offline",                          "裝置離線");
        put("固件更新指令已下发",           "Firmware update command sent",            "韌體更新指令已下發");
        put("升级指令已下发，等待设备响应",  "Upgrade command sent, waiting for device response", "升級指令已下發，等待裝置回應");
        put("设备不在线",                  "Device is offline",                       "設備不在線");
        put("发送指令超时",                "Command timed out",                       "發送指令逾時");
        put("该功能仅在本地模式下可用",      "This feature is only available in local mode",  "此功能僅在本地模式下可用");
        put("method 不能为空",             "method is required",                             "method 不能為空");
        put("云模式仅支持设置 InflowMaxCurrent", "Only InflowMaxCurrent can be set in cloud mode", "雲端模式僅支援設置 InflowMaxCurrent");
        put("InflowMaxCurrent 不能为空",   "InflowMaxCurrent is required",                   "InflowMaxCurrent 不能為空");
        put("mac 不能为空",                "mac is required",                                "mac 不能為空");
        put("设备不在线，无法发起充电",     "Device is offline, cannot start charging",       "設備不在線，無法發起充電");
        put("该桩未上报枪信息，请等待设备上线后重试", "Connector info not available, please wait for device to come online", "充電槍信息未上報，請等待設備上線後重試");
        put("设备响应超时",                "Device response timeout",                        "設備回應逾時");
        put("设备不在线，无法停止充电",     "Device is offline, cannot stop charging",        "設備不在線，無法停止充電");
        put("该桩当前没有进行中的充电会话", "No active charging session on this charger",     "此充電樁當前無進行中的充電會話");
        put("deviceList 不能为空",         "deviceList is required",                         "deviceList 不能為空");
        put("设备不存在",                  "Device not found",                               "設備不存在");
        put("当前已是最新版本，无需更新",   "Already on the latest version",                  "當前已是最新版本，無需更新");
        put("设备不在线，无法下发更新",     "Device is offline, cannot send update",          "設備不在線，無法下發更新");
        put("生成固件下载链接失败",         "Failed to generate firmware download link",      "生成韌體下載連結失敗");
        put("升级失败",                    "Upgrade failed",                                 "升級失敗");
        put("充电桩",                      "Charger",                                        "充電樁");

        // ── 动态前缀 ────────────────────────────────────────────────
        put("不支持的 method: ",           "Unsupported method: ",                    "不支援的 method: ");
        put("不支持的 configname: ",       "Unsupported configname: ",                "不支援的 configname: ");
        put("未找到桩: ",                  "Charger not found: ",                     "未找到充電樁: ");
        put("设备拒绝启动充电（",          "Device rejected start charging (",        "設備拒絕啟動充電（");
        put("设备拒绝停止充电（",          "Device rejected stop charging (",         "設備拒絕停止充電（");
    }

    private static void put(String zh, String en, String tw) {
        MESSAGES.put(zh, new String[]{en, tw});
    }

    /**
     * 根据语言返回对应消息。精确匹配优先，其次前缀匹配（处理动态拼接消息）。
     * @param zh  原始简体消息
     * @param lang 语言标识：zh / en / tw
     * @return 翻译后的消息，无匹配时原样返回
     */
    public static String get(String zh, String lang) {
        if (zh == null) {
            return "";
        }
        if ("zh".equals(lang)) {
            return zh;
        }
        // 精确匹配
        String[] trans = MESSAGES.get(zh);
        if (trans != null) {
            return "tw".equals(lang) ? trans[1] : trans[0];
        }
        // 前缀匹配（动态消息）
        for (Map.Entry<String, String[]> entry : MESSAGES.entrySet()) {
            if (zh.startsWith(entry.getKey())) {
                String translatedPrefix = "tw".equals(lang) ? entry.getValue()[1] : entry.getValue()[0];
                return translatedPrefix + zh.substring(entry.getKey().length());
            }
        }
        return zh;
    }

    /**
     * 从 Accept-Language Header 解析语言标识
     * en / en-* → "en"；zh-TW / zh-HK → "tw"；其他 → "zh"
     */
    public static String parseLang(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return "zh";
        }
        String lang = acceptLanguage.split(",")[0].trim().toLowerCase();
        if (lang.startsWith("en")) {
            return "en";
        }
        if (lang.startsWith("zh-tw") || lang.startsWith("zh-hk")) {
            return "tw";
        }
        return "zh";
    }
}
