package com.echarge.modules.app.i18n;

import java.util.HashMap;
import java.util.Map;

/**
 * App 端 i18n 翻译表（简体 → 英文 / 繁体 / 西班牙语 / 葡萄牙语）
 * 支持精确匹配和前缀匹配（用于动态拼接的错误消息）
 * @author Edwin
 */
public class AppI18n {

    /** key=简体, value=[英文, 繁体, 西班牙语, 葡萄牙语] */
    private static final Map<String, String[]> MESSAGES = new HashMap<>();

    static {
        // ── Token / 认证 ──────────────────────────────────────────
        put("未提供 Token",
                "Token not provided",
                "未提供 Token",
                "Token no proporcionado",
                "Token não fornecido");
        put("Token 无效",
                "Invalid token",
                "Token 無效",
                "Token inválido",
                "Token inválido");
        put("Token 已过期或签名无效",
                "Token expired or invalid signature",
                "Token 已過期或簽名無效",
                "Token expirado o firma inválida",
                "Token expirado ou assinatura inválida");

        // ── 用户 ──────────────────────────────────────────────────
        put("用户不存在",
                "User not found",
                "用戶不存在",
                "Usuario no encontrado",
                "Usuário não encontrado");
        put("账号已禁用",
                "Account is disabled",
                "帳號已被停用",
                "Cuenta deshabilitada",
                "Conta desativada");
        put("密码错误",
                "Incorrect password",
                "密碼錯誤",
                "Contraseña incorrecta",
                "Senha incorreta");
        put("邮箱、密码、姓名不能为空",
                "Email, password and name are required",
                "電郵、密碼及姓名為必填項",
                "Email, contraseña y nombre son obligatorios",
                "Email, senha e nome são obrigatórios");
        put("该邮箱已注册",
                "Email already registered",
                "此電郵已被註冊",
                "Email ya registrado",
                "Email já registrado");
        put("注册成功",
                "Registration successful",
                "註冊成功",
                "Registro exitoso",
                "Cadastro realizado");
        put("邮箱和密码不能为空",
                "Email and password are required",
                "電郵及密碼為必填項",
                "Email y contraseña son obligatorios",
                "Email e senha são obrigatórios");

        // ── 设备绑定 ───────────────────────────────────────────────
        put("设备序列号不能为空",
                "Device SN is required",
                "設備序列號為必填項",
                "SN del dispositivo es obligatorio",
                "SN do dispositivo é obrigatório");
        put("该设备已绑定",
                "Device already bound",
                "該設備已綁定",
                "Dispositivo ya vinculado",
                "Dispositivo já vinculado");
        put("绑定成功",
                "Device bound successfully",
                "設備綁定成功",
                "Vinculado exitosamente",
                "Vinculado com sucesso");
        put("解绑成功",
                "Device unbound successfully",
                "設備解綁成功",
                "Desvinculado exitosamente",
                "Desvinculado com sucesso");
        put("授权请求已发送，请等待设备主人同意",
                "Authorization request sent, waiting for device owner's approval",
                "授權請求已發送，請等待設備主人同意",
                "Solicitud enviada, esperando aprobación del propietario",
                "Solicitação enviada, aguardando aprovação do proprietário");
        put("请求过于频繁，请 1 分钟后再试",
                "Too many requests, please try again in 1 minute",
                "請求過於頻繁，請 1 分鐘後再試",
                "Demasiadas solicitudes, intente en 1 minuto",
                "Muitas solicitações, tente novamente em 1 minuto");
        put("requestId 不能为空",
                "requestId is required",
                "requestId 不能為空",
                "requestId es obligatorio",
                "requestId é obrigatório");
        put("action 无效，必须为 APPROVE 或 REJECT",
                "Invalid action, must be APPROVE or REJECT",
                "action 無效，必須為 APPROVE 或 REJECT",
                "Acción inválida, debe ser APPROVE o REJECT",
                "Ação inválida, deve ser APPROVE ou REJECT");
        put("请求不存在或已过期",
                "Request not found or expired",
                "請求不存在或已過期",
                "Solicitud no encontrada o expirada",
                "Solicitação não encontrada ou expirada");
        put("无权操作",
                "No permission",
                "無權操作",
                "Sin permiso",
                "Sem permissão");
        put("已由其他人授权，无需重复操作",
                "Already approved by another user",
                "已由其他人授權，無需重複操作",
                "Ya aprobado por otro usuario",
                "Já aprovado por outro usuário");
        put("已同意，授权码已发送给申请人",
                "Approved, authorization code sent to requester",
                "已同意，授權碼已發送給申請人",
                "Aprobado, código enviado al solicitante",
                "Aprovado, código enviado ao solicitante");
        put("已拒绝",
                "Rejected",
                "已拒絕",
                "Rechazado",
                "Rejeitado");
        put("requestId、authCode、deviceSn 不能为空",
                "requestId, authCode and deviceSn are required",
                "requestId、authCode、deviceSn 不能為空",
                "requestId, authCode y deviceSn son obligatorios",
                "requestId, authCode e deviceSn são obrigatórios");
        put("请求状态异常，请重新发起绑定",
                "Request status error, please initiate binding again",
                "請求狀態異常，請重新發起綁定",
                "Estado de solicitud anómalo, inicie el vinculado nuevamente",
                "Estado da solicitação inválido, inicie a vinculação novamente");
        put("授权码已过期",
                "Authorization code expired",
                "授權碼已過期",
                "Código de autorización expirado",
                "Código de autorização expirado");
        put("授权码错误",
                "Incorrect authorization code",
                "授權碼錯誤",
                "Código de autorización incorrecto",
                "Código de autorização incorreto");

        // ── 固件 ───────────────────────────────────────────────────
        put("currentVersion 不能为空",
                "currentVersion is required",
                "currentVersion 為必填項",
                "currentVersion es obligatorio",
                "currentVersion é obrigatório");
        put("暂无已发布的固件版本",
                "No firmware version available",
                "暫無已發佈的韌體版本",
                "Sin versiones de firmware disponibles",
                "Nenhuma versão de firmware disponível");
        put("固件文件不存在",
                "Firmware file not found",
                "韌體文件不存在",
                "Archivo de firmware no encontrado",
                "Arquivo de firmware não encontrado");
        put("固件文件不能为空",
                "Firmware file is required",
                "韌體文件為必填項",
                "Archivo de firmware es obligatorio",
                "Arquivo de firmware é obrigatório");
        put("该设备未绑定到当前用户",
                "Device not bound to current user",
                "該設備未綁定至當前用戶",
                "Dispositivo no vinculado al usuario",
                "Dispositivo não vinculado ao usuário");
        put("设备不在线，无法升级",
                "Device is offline, cannot upgrade",
                "設備不在線，無法升級",
                "Dispositivo offline, no se puede actualizar",
                "Dispositivo offline, não é possível atualizar");
        put("设备 OCPP 连接不存在，无法下发升级指令",
                "Device OCPP connection not found, cannot send upgrade command",
                "設備 OCPP 連接不存在，無法下發升級指令",
                "Conexión OCPP no encontrada, no se puede enviar comando",
                "Conexão OCPP não encontrada, não é possível enviar comando");
        put("文件名格式不正确，应为 N3Lite-X.Y.Z.bin 或 N3Lite-X.Y.Z_XXXXX.bin",
                "Invalid filename format, expected N3Lite-X.Y.Z.bin or N3Lite-X.Y.Z_XXXXX.bin",
                "文件名格式不正確，應為 N3Lite-X.Y.Z.bin 或 N3Lite-X.Y.Z_XXXXX.bin",
                "Formato de nombre de archivo incorrecto",
                "Formato de nome de arquivo incorreto");
        put("升级任务已创建",
                "Upgrade task created",
                "升級任務已建立",
                "Tarea de actualización creada",
                "Tarefa de atualização criada");
        put("任务不存在",
                "Task not found",
                "任務不存在",
                "Tarea no encontrada",
                "Tarefa não encontrada");
        put("无权查看该任务",
                "No permission to view this task",
                "無權查看此任務",
                "Sin permiso para ver la tarea",
                "Sem permissão para ver a tarefa");

        // ── 固件（动态前缀，前缀匹配用）────────────────────────────
        put("生成下载链接失败: ",
                "Failed to generate download link: ",
                "生成下載連結失敗: ",
                "Error al generar enlace de descarga: ",
                "Erro ao gerar link de download: ");
        put("文件上传失败: ",
                "File upload failed: ",
                "文件上傳失敗: ",
                "Error al subir archivo: ",
                "Erro ao enviar arquivo: ");
        put("生成固件下载链接失败: ",
                "Failed to generate firmware download link: ",
                "生成韌體下載連結失敗: ",
                "Error al generar enlace de firmware: ",
                "Erro ao gerar link de firmware: ");
        put("下发升级指令失败: ",
                "Failed to send upgrade command: ",
                "下發升級指令失敗: ",
                "Error al enviar comando: ",
                "Erro ao enviar comando: ");

        // ── RPC / 设备状态 ─────────────────────────────────────────
        put("设备离线",
                "Device offline",
                "裝置離線",
                "Dispositivo desconectado",
                "Dispositivo offline");
        put("固件更新指令已下发",
                "Firmware update command sent",
                "韌體更新指令已下發",
                "Comando de actualización enviado",
                "Comando de atualização enviado");
        put("升级指令已下发，等待设备响应",
                "Upgrade command sent, waiting for device response",
                "升級指令已下發，等待裝置回應",
                "Comando enviado, esperando respuesta",
                "Comando enviado, aguardando resposta");
        put("设备不在线",
                "Device is offline",
                "設備不在線",
                "Dispositivo no está en línea",
                "Dispositivo não está online");
        put("发送指令超时",
                "Command timed out",
                "發送指令逾時",
                "Tiempo de espera agotado",
                "Tempo limite esgotado");
        put("该功能仅在本地模式下可用",
                "This feature is only available in local mode",
                "此功能僅在本地模式下可用",
                "Función solo disponible en modo local",
                "Função disponível apenas no modo local");
        put("method 不能为空",
                "method is required",
                "method 不能為空",
                "method es obligatorio",
                "method é obrigatório");
        put("云模式仅支持设置 InflowMaxCurrent",
                "Only InflowMaxCurrent can be set in cloud mode",
                "雲端模式僅支援設置 InflowMaxCurrent",
                "En modo nube solo InflowMaxCurrent",
                "No modo nuvem apenas InflowMaxCurrent");
        put("InflowMaxCurrent 不能为空",
                "InflowMaxCurrent is required",
                "InflowMaxCurrent 不能為空",
                "InflowMaxCurrent es obligatorio",
                "InflowMaxCurrent é obrigatório");
        put("mac 不能为空",
                "mac is required",
                "mac 不能為空",
                "mac es obligatorio",
                "mac é obrigatório");
        put("设备不在线，无法发起充电",
                "Device is offline, cannot start charging",
                "設備不在線，無法發起充電",
                "Dispositivo offline, no se puede iniciar carga",
                "Dispositivo offline, não é possível iniciar carregamento");
        put("该桩未上报枪信息，请等待设备上线后重试",
                "Connector info not available, please wait for device to come online",
                "充電槍信息未上報，請等待設備上線後重試",
                "Información del conector no disponible, espere y reintente",
                "Informação do conector não disponível, aguarde e tente novamente");
        put("设备响应超时",
                "Device response timeout",
                "設備回應逾時",
                "Tiempo de respuesta agotado",
                "Tempo de resposta esgotado");
        put("设备不在线，无法停止充电",
                "Device is offline, cannot stop charging",
                "設備不在線，無法停止充電",
                "Dispositivo offline, no se puede detener la carga",
                "Dispositivo offline, não é possível parar o carregamento");
        put("该桩当前没有进行中的充电会话",
                "No active charging session on this charger",
                "此充電樁當前無進行中的充電會話",
                "Sin sesión de carga activa",
                "Sem sessão de carregamento ativa");
        put("deviceList 不能为空",
                "deviceList is required",
                "deviceList 不能為空",
                "deviceList es obligatorio",
                "deviceList é obrigatório");
        put("设备不存在",
                "Device not found",
                "設備不存在",
                "Dispositivo no encontrado",
                "Dispositivo não encontrado");
        put("当前已是最新版本，无需更新",
                "Already on the latest version",
                "當前已是最新版本，無需更新",
                "Ya está en la última versión",
                "Já está na versão mais recente");
        put("设备不在线，无法下发更新",
                "Device is offline, cannot send update",
                "設備不在線，無法下發更新",
                "Dispositivo offline, no se puede enviar actualización",
                "Dispositivo offline, não é possível enviar atualização");
        put("生成固件下载链接失败",
                "Failed to generate firmware download link",
                "生成韌體下載連結失敗",
                "Error al generar enlace de firmware",
                "Erro ao gerar link de firmware");
        put("升级失败",
                "Upgrade failed",
                "升級失敗",
                "Actualización fallida",
                "Atualização falhou");
        put("充电桩",
                "Charger",
                "充電樁",
                "Cargador",
                "Carregador");

        // ── 动态前缀 ────────────────────────────────────────────────
        put("不支持的 method: ",
                "Unsupported method: ",
                "不支援的 method: ",
                "Método no soportado: ",
                "Método não suportado: ");
        put("不支持的 configname: ",
                "Unsupported configname: ",
                "不支援的 configname: ",
                "Configname no soportado: ",
                "Configname não suportado: ");
        put("未找到桩: ",
                "Charger not found: ",
                "未找到充電樁: ",
                "Cargador no encontrado: ",
                "Carregador não encontrado: ");
        put("设备拒绝启动充电（",
                "Device rejected start charging (",
                "設備拒絕啟動充電（",
                "Dispositivo rechazó iniciar carga (",
                "Dispositivo recusou iniciar carregamento (");
        put("设备拒绝停止充电（",
                "Device rejected stop charging (",
                "設備拒絕停止充電（",
                "Dispositivo rechazó detener carga (",
                "Dispositivo recusou parar carregamento (");
    }

    private static void put(String zh, String en, String tw, String es, String pt) {
        MESSAGES.put(zh, new String[]{en, tw, es, pt});
    }

    /**
     * 根据语言返回对应消息。精确匹配优先，其次前缀匹配（处理动态拼接消息）。
     * @param zh  原始简体消息
     * @param lang 语言标识：zh / en / tw / es / pt
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
            return translateByLang(zh, trans, lang);
        }
        // 前缀匹配（动态消息）
        for (Map.Entry<String, String[]> entry : MESSAGES.entrySet()) {
            if (zh.startsWith(entry.getKey())) {
                String translatedPrefix = translateByLang(entry.getKey(), entry.getValue(), lang);
                return translatedPrefix + zh.substring(entry.getKey().length());
            }
        }
        return zh;
    }

    private static String translateByLang(String zh, String[] trans, String lang) {
        switch (lang) {
            case "en": return trans[0];
            case "tw": return trans[1];
            case "es": return trans[2];
            case "pt": return trans[3];
            default:   return zh;
        }
    }

    /**
     * 从 Accept-Language Header 解析语言标识
     * en / en-* → "en"；zh-TW / zh-HK → "tw"；es / es-* → "es"；pt / pt-* → "pt"；其他 → "zh"
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
        if (lang.startsWith("es")) {
            return "es";
        }
        if (lang.startsWith("pt")) {
            return "pt";
        }
        return "zh";
    }
}
