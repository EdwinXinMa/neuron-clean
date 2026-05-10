package com.echarge.common.i18n;

import java.util.HashMap;
import java.util.Map;

/**
 * Web 端 i18n 翻译表（简体 → 英文 / 繁体 / 西班牙语 / 葡萄牙语）
 * 支持精确匹配和前缀匹配（用于动态拼接的错误消息）
 * @author Edwin
 */
public class WebI18n {

    /** key=简体, value=[英文, 繁体, 西班牙语, 葡萄牙语] */
    private static final Map<String, String[]> MESSAGES = new HashMap<>();

    static {
        // ── 固件升级（FirmwareUpgradeController）────────────────────────
        put("firmwareId 和 deviceSn 不能为空",
                "firmwareId and deviceSn are required",
                "firmwareId 和 deviceSn 為必填項",
                "firmwareId y deviceSn son obligatorios",
                "firmwareId e deviceSn são obrigatórios");
        put("固件状态不是已发布，无法升级",
                "Firmware is not in released status, cannot upgrade",
                "韌體狀態非已發布，無法升級",
                "El firmware no está publicado, no se puede actualizar",
                "Firmware não está publicado, não é possível atualizar");
        put("设备 OCPP 连接不存在，无法下发升级指令",
                "Device OCPP connection not found, cannot send upgrade command",
                "設備 OCPP 連接不存在，無法下發升級指令",
                "Conexión OCPP no encontrada, no se puede enviar comando",
                "Conexão OCPP não encontrada, não é possível enviar comando");
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
        put("设备不在线，无法升级",
                "Device is offline, cannot upgrade",
                "設備不在線，無法升級",
                "Dispositivo offline, no se puede actualizar",
                "Dispositivo offline, não é possível atualizar");

        // ── 固件版本（FirmwareVersionController）────────────────────────
        put("文件不能为空",
                "File is required",
                "文件為必填項",
                "El archivo es obligatorio",
                "O arquivo é obrigatório");
        put("版本说明不能为空",
                "Release notes are required",
                "版本說明為必填項",
                "Las notas de versión son obligatorias",
                "As notas de versão são obrigatórias");
        put("上传成功",
                "Upload successful",
                "上傳成功",
                "Cargado exitosamente",
                "Upload realizado com sucesso");
        put("发布成功",
                "Released successfully",
                "發布成功",
                "Publicado exitosamente",
                "Publicado com sucesso");
        put("废弃成功",
                "Deprecated successfully",
                "已廢棄",
                "Deprecado exitosamente",
                "Deprecado com sucesso");
        put("固件不存在",
                "Firmware not found",
                "韌體不存在",
                "Firmware no encontrado",
                "Firmware não encontrado");
        put("只有草稿或已废弃状态的固件才能删除",
                "Only draft or deprecated firmware can be deleted",
                "只有草稿或已廢棄狀態的韌體才能刪除",
                "Solo el firmware en borrador o deprecado puede eliminarse",
                "Apenas firmware em rascunho ou deprecado pode ser excluído");
        put("删除成功",
                "Deleted successfully",
                "刪除成功",
                "Eliminado exitosamente",
                "Excluído com sucesso");
        put("只有草稿状态的固件才能发布",
                "Only draft firmware can be released",
                "只有草稿狀態的韌體才能發布",
                "Solo el firmware en borrador puede publicarse",
                "Apenas firmware em rascunho pode ser publicado");
        put("只有已发布状态的固件才能废弃",
                "Only released firmware can be deprecated",
                "只有已發布狀態的韌體才能廢棄",
                "Solo el firmware publicado puede deprecarse",
                "Apenas firmware publicado pode ser deprecado");
        put("最新发布版本不能废弃，请先发布新版本",
                "Cannot deprecate the latest released version, please release a new version first",
                "最新發布版本不能廢棄，請先發布新版本",
                "No se puede deprecar la última versión publicada, publique una nueva versión primero",
                "Não é possível deprecar a última versão publicada, publique uma nova versão primeiro");

        // ── 设备管理（NcDeviceController）────────────────────────────────
        put("设备不存在",
                "Device not found",
                "設備不存在",
                "Dispositivo no encontrado",
                "Dispositivo não encontrado");
        put("已激活的设备不允许删除",
                "Activated devices cannot be deleted",
                "已激活的設備不允許刪除",
                "Los dispositivos activados no pueden eliminarse",
                "Dispositivos ativados não podem ser excluídos");
        put("缺少设备ID",
                "Device ID is required",
                "缺少設備ID",
                "Falta el ID del dispositivo",
                "ID do dispositivo é obrigatório");
        put("修改成功",
                "Updated successfully",
                "修改成功",
                "Actualizado exitosamente",
                "Atualizado com sucesso");
        put("录入成功",
                "Registered successfully",
                "錄入成功",
                "Registrado exitosamente",
                "Registrado com sucesso");
        put("已禁用",
                "Disabled",
                "已禁用",
                "Deshabilitado",
                "Desabilitado");
        put("已启用",
                "Enabled",
                "已啟用",
                "Habilitado",
                "Habilitado");
        put("SN为空",
                "SN is empty",
                "SN 為空",
                "SN está vacío",
                "SN está vazio");
        put("经销商为空",
                "Dealer is empty",
                "經銷商為空",
                "El distribuidor está vacío",
                "O distribuidor está vazio");
        put("出货日期为空",
                "Ship date is empty",
                "出貨日期為空",
                "La fecha de envío está vacía",
                "A data de envio está vazia");
        put("breakerRating 不能为空",
                "breakerRating is required",
                "breakerRating 為必填項",
                "breakerRating es obligatorio",
                "breakerRating é obrigatório");
        put("DLM 配置已更新",
                "DLM configuration updated",
                "DLM 配置已更新",
                "Configuración DLM actualizada",
                "Configuração DLM atualizada");
        put("deviceList 不能为空",
                "deviceList is required",
                "deviceList 為必填項",
                "deviceList es obligatorio",
                "deviceList é obrigatório");
        put("工作模式切换指令已下发",
                "Work mode switch command sent",
                "工作模式切換指令已下發",
                "Comando de cambio de modo enviado",
                "Comando de mudança de modo enviado");
        put("设备离线，无法下发重启命令",
                "Device is offline, cannot send reboot command",
                "設備離線，無法下發重啟命令",
                "Dispositivo offline, no se puede enviar comando de reinicio",
                "Dispositivo offline, não é possível enviar comando de reinicialização");
        put("重启命令已下发",
                "Reboot command sent",
                "重啟命令已下發",
                "Comando de reinicio enviado",
                "Comando de reinicialização enviado");

        // ── 服务层（NcDeviceServiceImpl）─────────────────────────────────
        put("设备SN不能为空",
                "Device SN is required",
                "設備SN為必填項",
                "SN del dispositivo es obligatorio",
                "SN do dispositivo é obrigatório");
        put("经销商不能为空",
                "Dealer is required",
                "經銷商為必填項",
                "El distribuidor es obligatorio",
                "O distribuidor é obrigatório");
        put("出货日期不能为空",
                "Ship date is required",
                "出貨日期為必填項",
                "La fecha de envío es obligatoria",
                "A data de envio é obrigatória");
        put("设备SN已存在",
                "Device SN already exists",
                "設備SN已存在",
                "El SN del dispositivo ya existe",
                "SN do dispositivo já existe");
        put("设备离线，无法下发工作模式切换",
                "Device is offline, cannot send work mode switch command",
                "設備離線，無法下發工作模式切換",
                "Dispositivo offline, no se puede cambiar el modo de trabajo",
                "Dispositivo offline, não é possível alterar o modo de trabalho");
        put("桩 SN 不能为空",
                "Charger SN is required",
                "充電樁 SN 為必填項",
                "SN del cargador es obligatorio",
                "SN do carregador é obrigatório");

        // ── 动态前缀（前缀匹配用）────────────────────────────────────────
        put("设备不存在: ",
                "Device not found: ",
                "設備不存在: ",
                "Dispositivo no encontrado: ",
                "Dispositivo não encontrado: ");
        put("生成固件下载链接失败: ",
                "Failed to generate firmware download link: ",
                "生成韌體下載連結失敗: ",
                "Error al generar enlace de firmware: ",
                "Erro ao gerar link de firmware: ");
        put("下发升级指令失败: ",
                "Failed to send upgrade command: ",
                "下發升級指令失敗: ",
                "Error al enviar comando de actualización: ",
                "Erro ao enviar comando de atualização: ");
        put("上传失败: ",
                "Upload failed: ",
                "上傳失敗: ",
                "Error al subir archivo: ",
                "Erro ao enviar arquivo: ");
        put("生成下载链接失败: ",
                "Failed to generate download link: ",
                "生成下載連結失敗: ",
                "Error al generar enlace de descarga: ",
                "Erro ao gerar link de download: ");
        put("导入失败: ",
                "Import failed: ",
                "導入失敗: ",
                "Error al importar: ",
                "Erro ao importar: ");
        put("版本 ",
                "Version ",
                "版本 ",
                "Versión ",
                "Versão ");
        put("版本号不能小于或等于已有版本 ",
                "Version must be greater than existing version ",
                "版本號不能小於或等於已有版本 ",
                "La versión debe ser mayor que la versión existente ",
                "A versão deve ser maior que a versão existente ");
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
