package com.echarge.modules.device.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventListener;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.event.kafka.KafkaAlertPublisher;
import com.echarge.common.event.kafka.KafkaTopics;
import com.echarge.modules.device.entity.FirmwareUpgradeTask;
import com.echarge.modules.device.entity.NcChargingSession;
import com.echarge.modules.device.entity.NcConnector;
import com.echarge.modules.device.service.impl.NcDeviceServiceImpl;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.entity.NcOpLog;
import com.echarge.modules.device.mapper.NcChargingSessionMapper;
import com.echarge.modules.device.entity.FirmwareVersion;
import com.echarge.modules.device.service.IFirmwareUpgradeTaskService;
import com.echarge.modules.device.service.IFirmwareVersionService;
import com.echarge.modules.device.service.INcConnectorService;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.service.INcDlmHistoryService;
import com.echarge.modules.device.service.INcOpLogService;
import com.echarge.common.websocket.FrontendPushChannel;
import com.echarge.modules.device.websocket.AppWebSocket;
import com.echarge.modules.device.websocket.DeviceEventWebSocket;
import com.echarge.modules.device.websocket.OtaWebSocket;
import com.echarge.common.util.RedisUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备事件消费端
 * 直连模式：实现 DeviceEventListener 接口
 * Kafka 模式：通过 @KafkaListener 消费三个 Topic
 * @author Edwin
 */
@Slf4j
@Component
public class DeviceEventHandler implements DeviceEventListener {

    @Autowired
    private INcDeviceService ncDeviceService;

    @Autowired
    private INcConnectorService ncConnectorService;

    @Autowired
    private IFirmwareUpgradeTaskService upgradeTaskService;

    @Autowired
    private IFirmwareVersionService firmwareVersionService;

    @Autowired
    private INcOpLogService opLogService;

    @Autowired
    private INcDlmHistoryService dlmHistoryService;

    @Autowired
    private NcChargingSessionMapper chargingSessionMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired(required = false)
    private KafkaAlertPublisher kafkaAlertPublisher;

    private final Gson gson = new Gson();

    // ==================== Kafka 消费入口 ====================

    @KafkaListener(
            topics = KafkaTopics.DEVICE_LIFECYCLE,
            groupId = "device-handler",
            autoStartup = "${neuron.kafka.enabled:false}"
    )
    public void onLifecycleMessage(ConsumerRecord<String, String> record) {
        DeviceEvent event = gson.fromJson(record.value(), DeviceEvent.class);
        log.debug("[KAFKA] Lifecycle consumed: key={}, type={}", record.key(), event.getEventType());
        onDeviceEvent(event);
    }

    @KafkaListener(
            topics = KafkaTopics.DEVICE_TELEMETRY,
            groupId = "device-handler",
            autoStartup = "${neuron.kafka.enabled:false}"
    )
    public void onTelemetryMessage(ConsumerRecord<String, String> record) {
        DeviceEvent event = gson.fromJson(record.value(), DeviceEvent.class);
        log.debug("[KAFKA] Telemetry consumed: key={}, type={}", record.key(), event.getEventType());
        onDeviceEvent(event);
    }

    @KafkaListener(
            topics = KafkaTopics.DEVICE_TASK,
            groupId = "device-handler",
            autoStartup = "${neuron.kafka.enabled:false}"
    )
    public void onTaskMessage(ConsumerRecord<String, String> record) {
        DeviceEvent event = gson.fromJson(record.value(), DeviceEvent.class);
        log.debug("[KAFKA] Task consumed: key={}, type={}", record.key(), event.getEventType());
        onDeviceEvent(event);
    }

    // ==================== 统一处理入口 ====================

    /** {@inheritDoc} */
    @Override
    public void onDeviceEvent(DeviceEvent event) {
        switch (event.getEventType()) {
            case DeviceEvent.BOOT_NOTIFICATION:
                handleBootNotification(event);
                break;
            case DeviceEvent.HEARTBEAT:
                handleHeartbeat(event);
                break;
            case DeviceEvent.STATUS_NOTIFICATION:
                handleStatusNotification(event);
                break;
            case DeviceEvent.DEVICE_OFFLINE:
                handleDeviceOffline(event);
                break;
            case DeviceEvent.TOPOLOGY_REPORT:
                handleTopologyReport(event);
                break;
            case DeviceEvent.FIRMWARE_STATUS:
                handleFirmwareStatus(event);
                break;
            case DeviceEvent.DLM_STATUS:
                handleDlmStatus(event);
                break;
            case DeviceEvent.START_TRANSACTION:
                handleStartTransaction(event);
                break;
            case DeviceEvent.STOP_TRANSACTION:
                handleStopTransaction(event);
                break;
            default:
                log.warn("[DeviceEvent] Unknown event type: {}", event.getEventType());
        }
    }

    /**
     * 处理 BootNotification — 设备上线激活
     */
    private void handleBootNotification(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        log.info("[DeviceEvent] BootNotification: chargePointId={}", chargePointId);

        // 解析载荷
        JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);
        String vendor = getJsonString(payload, "chargePointVendor");
        String model = getJsonString(payload, "chargePointModel");
        String serialNumber = getJsonString(payload, "chargePointSerialNumber");
        String firmwareVersion = getJsonString(payload, "firmwareVersion");

        // 用 chargePointId（SN）查找台账记录
        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, chargePointId)
        );

        Date now = new Date();

        if (device != null) {
            // 台账已存在 → 激活设备，更新运行时信息
            device.setOnlineStatus(BizConstant.DEVICE_ONLINE);
            device.setFirmwareVersion(firmwareVersion);
            device.setLastHeartbeat(now);
            device.setLastOnlineTime(now);
            if (device.getFirstOnlineTime() == null) {
                device.setFirstOnlineTime(now);
            }
            if (model != null) {
                device.setDeviceModel(model);
            }
            ncDeviceService.updateById(device);
            log.info("[DeviceEvent] Device activated: sn={}", chargePointId);
        } else {
            // 台账不存在 → 自动创建记录（未登记设备也允许接入）
            device = new NcDevice();
            device.setSn(chargePointId);
            device.setDeviceType(guessDeviceType(model));
            device.setDeviceModel(model);
            device.setFirmwareVersion(firmwareVersion);
            device.setOnlineStatus(BizConstant.DEVICE_ONLINE);
            device.setFirstOnlineTime(now);
            device.setLastOnlineTime(now);
            device.setLastHeartbeat(now);
            NcDeviceServiceImpl.assignRandomLocation(device);
            ncDeviceService.save(device);
            log.info("[DeviceEvent] New device registered (unregistered): sn={}", chargePointId);
        }
        broadcastDeviceStatus(chargePointId, BizConstant.DEVICE_ONLINE, "设备上线");
        JSONObject onlineMsg = new JSONObject();
        onlineMsg.put("status", BizConstant.DEVICE_ONLINE);
        onlineMsg.put("message", "设备上线");
        AppWebSocket.publish("deviceStatus:" + chargePointId, onlineMsg);
    }

    /**
     * 处理 Heartbeat — 更新最后心跳时间
     */
    private void handleHeartbeat(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, chargePointId)
        );
        if (device != null) {
            device.setLastHeartbeat(new Date());
            device.setOnlineStatus(BizConstant.DEVICE_ONLINE);
            ncDeviceService.updateById(device);
        }
    }

    /**
     * 处理 StatusNotification — 根据 connectorId 更新对应设备/枪状态
     * connectorId=0 → N3 Lite 本体
     * connectorId>0 → 对应枪，同时更新所属桩的状态
     */
    private void handleStatusNotification(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        log.info("[DeviceEvent] StatusNotification: chargePointId={}", chargePointId);

        JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);
        String status = getJsonString(payload, "status");
        String errorCode = getJsonString(payload, "errorCode");
        int connectorId = payload.has("connectorId") ? payload.get("connectorId").getAsInt() : -1;

        // 找到 N3 Lite
        NcDevice n3lite = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, chargePointId)
        );
        if (n3lite == null) {
            return;
        }

        if (connectorId == 0) {
            // N3 Lite 本体状态
            n3lite.setOnlineStatus(BizConstant.OCPP_FAULTED.equals(status) ? BizConstant.DEVICE_FAULT : BizConstant.DEVICE_ONLINE);
            ncDeviceService.updateById(n3lite);
            log.info("[DeviceEvent] N3 Lite status updated: sn={}, status={}", chargePointId, status);
            broadcastDeviceStatus(chargePointId, BizConstant.OCPP_FAULTED.equals(status) ? BizConstant.DEVICE_FAULT : BizConstant.DEVICE_ONLINE, status);
        } else if (connectorId > 0) {
            // 更新枪状态
            NcConnector connector = ncConnectorService.getOne(
                    new LambdaQueryWrapper<NcConnector>()
                            .eq(NcConnector::getConnectorId, connectorId)
                            .inSql(NcConnector::getDeviceId,
                                    "SELECT id FROM nc_device WHERE parent_device_id = '" + n3lite.getId() + "'")
            );
            if (connector != null) {
                connector.setStatus(status != null ? status : BizConstant.OCPP_AVAILABLE);
                connector.setUpdateTime(new Date());
                ncConnectorService.updateById(connector);
                log.info("[DeviceEvent] Connector status updated: connectorId={}, status={}", connectorId, status);

                // 检查该桩下所有枪是否都故障，如果是则桩也标记故障
                String pileId = connector.getDeviceId();
                List<NcConnector> allConnectors = ncConnectorService.list(
                        new LambdaQueryWrapper<NcConnector>().eq(NcConnector::getDeviceId, pileId)
                );
                boolean allFaulted = allConnectors.stream().allMatch(c -> BizConstant.OCPP_FAULTED.equals(c.getStatus()));
                NcDevice pile = ncDeviceService.getById(pileId);
                if (pile != null) {
                    pile.setOnlineStatus(allFaulted ? BizConstant.DEVICE_FAULT : BizConstant.DEVICE_ONLINE);
                    ncDeviceService.updateById(pile);
                }
            }
        }

        // 故障时二次生产到 device-alert topic
        if (BizConstant.OCPP_FAULTED.equals(status) && errorCode != null && !BizConstant.OCPP_NO_ERROR.equals(errorCode)) {
            publishAlert(event);
        }
    }

    /**
     * 发布告警事件到 device-alert（Kafka 模式走 Kafka，直连模式直接忽略由 AlertEventHandler 自行处理）
     */
    private void publishAlert(DeviceEvent event) {
        if (kafkaAlertPublisher != null) {
            kafkaAlertPublisher.publishAlert(event);
        }
    }

    /**
     * 处理设备离线 — WebSocket 连接断开
     */
    private void handleDeviceOffline(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, chargePointId)
        );
        if (device != null) {
            device.setOnlineStatus(BizConstant.DEVICE_OFFLINE);
            ncDeviceService.updateById(device);
            log.info("[DeviceEvent] Device offline: sn={}", chargePointId);
            broadcastDeviceStatus(chargePointId, BizConstant.DEVICE_OFFLINE, "设备离线");
            JSONObject offlineMsg = new JSONObject();
            offlineMsg.put("status", BizConstant.DEVICE_OFFLINE);
            offlineMsg.put("message", "设备离线");
            AppWebSocket.publish("deviceStatus:" + chargePointId, offlineMsg);

            // 清除 Redis 中的 DLM 实时数据，避免离线后前端还显示旧数据
            redisUtil.del("device:dlm:" + chargePointId);

            // N3 Lite 离线 → 下挂桩也全部离线，桩下的枪状态改为 Unavailable
            List<NcDevice> children = ncDeviceService.list(
                    new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getParentDeviceId, device.getId())
            );
            for (NcDevice child : children) {
                child.setOnlineStatus(BizConstant.DEVICE_OFFLINE);
                ncDeviceService.updateById(child);
                log.info("[DeviceEvent] Child device offline: sn={}", child.getSn());

                // 该桩下所有枪状态改为 Unavailable
                List<NcConnector> connectors = ncConnectorService.list(
                        new LambdaQueryWrapper<NcConnector>().eq(NcConnector::getDeviceId, child.getId())
                );
                for (NcConnector connector : connectors) {
                    connector.setStatus(BizConstant.OCPP_UNAVAILABLE);
                    connector.setUpdateTime(new Date());
                    ncConnectorService.updateById(connector);
                }
            }
        }
    }

    /**
     * 处理拓扑上报 — N3 Lite 上报下挂 ATP III 充电桩及其枪信息
     * payload 格式:
     * {
     *   "chargePoints": [
     *     {
     *       "sn": "AT-0001",
     *       "model": "ATP III-11.5",
     *       "connectors": [
     *         { "connectorId": 1, "status": "Available" },
     *         { "connectorId": 2, "status": "Charging" }
     *       ]
     *     }
     *   ]
     * }
     */
    private void handleTopologyReport(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        log.info("[DeviceEvent] TopologyReport from: {}", chargePointId);

        // 找到父设备（N3 Lite）
        NcDevice parent = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, chargePointId)
        );
        if (parent == null) {
            log.warn("[DeviceEvent] TopologyReport: parent device not found, sn={}", chargePointId);
            return;
        }

        JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);
        JsonArray chargePoints = payload.getAsJsonArray("chargePoints");
        if (chargePoints == null) {
            return;
        }

        Date now = new Date();

        // 收集本次上报的所有桩SN，用于后续删除不存在的桩
        List<String> reportedPileSns = new ArrayList<>();
        // 收集本次上报的所有枪，key=桩ID，value=connectorId列表
        Map<String, List<Integer>> reportedConnectors = new HashMap<>();

        for (JsonElement cpElement : chargePoints) {
            JsonObject cp = cpElement.getAsJsonObject();
            String sn = getJsonString(cp, "sn");
            String model = getJsonString(cp, "model");

            if (sn == null) {
                continue;
            }
            reportedPileSns.add(sn);

            // 处理桩
            NcDevice pile = ncDeviceService.getOne(
                    new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn)
            );

            if (pile != null) {
                pile.setParentDeviceId(parent.getId());
                pile.setOnlineStatus(BizConstant.DEVICE_ONLINE);
                pile.setDeviceModel(model);
                ncDeviceService.updateById(pile);
                log.info("[DeviceEvent] Pile updated: sn={}, parent={}", sn, chargePointId);
            } else {
                pile = new NcDevice();
                pile.setSn(sn);
                pile.setDeviceType(BizConstant.TYPE_ATP_III);
                pile.setDeviceModel(model);
                pile.setParentDeviceId(parent.getId());
                pile.setOnlineStatus(BizConstant.DEVICE_ONLINE);
                pile.setLastHeartbeat(now);
                ncDeviceService.save(pile);
                log.info("[DeviceEvent] Pile registered: sn={}, parent={}", sn, chargePointId);
            }

            // 处理该桩下的枪
            List<Integer> connectorIds = new ArrayList<>();
            JsonArray connectors = cp.getAsJsonArray("connectors");
            if (connectors != null) {
                for (JsonElement connElement : connectors) {
                    JsonObject conn = connElement.getAsJsonObject();
                    int connectorId = conn.has("connectorId") ? conn.get("connectorId").getAsInt() : 0;
                    String connStatus = getJsonString(conn, "status");

                    if (connectorId == 0) {
                        continue;
                    }
                    connectorIds.add(connectorId);

                    NcConnector connector = ncConnectorService.getOne(
                            new LambdaQueryWrapper<NcConnector>()
                                    .eq(NcConnector::getDeviceId, pile.getId())
                                    .eq(NcConnector::getConnectorId, connectorId)
                    );

                    if (connector != null) {
                        connector.setStatus(connStatus != null ? connStatus : BizConstant.OCPP_AVAILABLE);
                        connector.setUpdateTime(now);
                        ncConnectorService.updateById(connector);
                    } else {
                        connector = new NcConnector();
                        connector.setDeviceId(pile.getId());
                        connector.setConnectorId(connectorId);
                        connector.setStatus(connStatus != null ? connStatus : BizConstant.OCPP_AVAILABLE);
                        connector.setCreateTime(now);
                        connector.setUpdateTime(now);
                        ncConnectorService.save(connector);
                    }
                    log.info("[DeviceEvent] Connector updated: pileId={}, connectorId={}, status={}", pile.getId(), connectorId, connStatus);
                }
            }

            // 删除该桩下不在本次上报中的枪
            ncConnectorService.remove(
                    new LambdaQueryWrapper<NcConnector>()
                            .eq(NcConnector::getDeviceId, pile.getId())
                            .notIn(!connectorIds.isEmpty(), NcConnector::getConnectorId, connectorIds)
            );
            reportedConnectors.put(pile.getId(), connectorIds);
        }

        // 删除不在本次上报中的桩及其枪
        List<NcDevice> existingPiles = ncDeviceService.list(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getParentDeviceId, parent.getId())
        );
        for (NcDevice existingPile : existingPiles) {
            if (!reportedPileSns.contains(existingPile.getSn())) {
                // 先删该桩下所有枪
                ncConnectorService.remove(
                        new LambdaQueryWrapper<NcConnector>().eq(NcConnector::getDeviceId, existingPile.getId())
                );
                // 再删桩
                ncDeviceService.removeById(existingPile.getId());
                log.info("[DeviceEvent] Pile removed (not in topology): sn={}", existingPile.getSn());
            }
        }
    }

    /**
     * 处理 FirmwareStatusNotification — 设备上报固件升级状态
     * OCPP 1.6 status: Downloading, Downloaded, Installing, Installed, DownloadFailed, InstallationFailed, Idle
     */
    /**
     * 处理 DlmReport — 设备周期上报 CT 实时数据，写入 Redis
     * payload 格式:
     * {
     *   "totalCurrent": 28.5,
     *   "voltage": 230.1,
     *   "totalPower": 6555,
     *   "deviceTemp": 42.3,
     *   "wifiRssi": -55,
     *   "breakerRating": 32,
     *   "pileAllocations": [
     *     { "sn": "AT-xxx-01", "allocatedCurrent": 16.0 },
     *     { "sn": "AT-xxx-02", "allocatedCurrent": 12.5 }
     *   ]
     * }
     */
    private void handleDlmStatus(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        String payload = event.getPayload();
        log.info("[DeviceEvent] DlmReport from {}: {}", chargePointId, payload);

        // 完整 JSON 写入 Redis（实时展示）
        String redisKey = "device:dlm:" + chargePointId;
        redisUtil.set(redisKey, payload, 300);

        // 推送 DLMStatus 给订阅了该设备的前端
        JsonObject wsMsg = new JsonObject();
        wsMsg.addProperty("type", "DLM_UPDATE");
        wsMsg.addProperty("deviceSn", chargePointId);
        wsMsg.addProperty("data", payload);
        DeviceEventWebSocket.pushToSubscribers(chargePointId, wsMsg.toString());

        // 异步写入时序表（历史图表）
        dlmHistoryService.saveDlmReport(chargePointId, payload);

        try {
            JsonObject data = gson.fromJson(payload, JsonObject.class);

            // 同步 breakerRating / macAddress 到数据库
            NcDevice device = ncDeviceService.getOne(
                    new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, chargePointId)
            );
            if (device != null) {
                boolean needUpdate = false;

                // 防御：收到 DLMStatus 说明设备在线，修正可能遗漏的离线状态
                if (!BizConstant.DEVICE_ONLINE.equals(device.getOnlineStatus())) {
                    device.setOnlineStatus(BizConstant.DEVICE_ONLINE);
                    device.setLastOnlineTime(new Date());
                    needUpdate = true;
                    log.info("[DeviceEvent] Device auto-recovered to ONLINE via DLMStatus: sn={}", chargePointId);
                    broadcastDeviceStatus(chargePointId, BizConstant.DEVICE_ONLINE, "DLMStatus 自动恢复上线");
                }

                if (data.has("breakerRating")) {
                    int breakerRating = data.get("breakerRating").getAsInt();
                    if (device.getBreakerRating() == null || device.getBreakerRating() != breakerRating) {
                        device.setBreakerRating(breakerRating);
                        needUpdate = true;
                        log.info("[DeviceEvent] BreakerRating updated: sn={}, rating={}A", chargePointId, breakerRating);
                    }
                }

                if (data.has("macAddress") && !data.get("macAddress").isJsonNull()) {
                    String macAddress = data.get("macAddress").getAsString();
                    if (!macAddress.isEmpty() && !macAddress.equals(device.getMacAddress())) {
                        device.setMacAddress(macAddress);
                        needUpdate = true;
                        log.info("[DeviceEvent] MacAddress updated: sn={}, mac={}", chargePointId, macAddress);
                    }
                }

                if (needUpdate) {
                    ncDeviceService.updateById(device);
                }
            }

            // 充电会话检测（遍历桩 → 枪）
            if (data.has("pileAllocations")) {
                JsonArray piles = data.getAsJsonArray("pileAllocations");
                for (JsonElement pileElem : piles) {
                    JsonObject pile = pileElem.getAsJsonObject();
                    String pileSn = getJsonString(pile, "sn");
                    Integer energy = pile.has("energy") && !pile.get("energy").isJsonNull()
                            ? pile.get("energy").getAsInt() : null;
                    Integer chargingMethod = pile.has("charge_Method") && !pile.get("charge_Method").isJsonNull()
                            ? pile.get("charge_Method").getAsInt() : null;

                    if (!pile.has("connectors")) {
                        continue;
                    }
                    JsonArray connectors = pile.getAsJsonArray("connectors");
                    for (JsonElement connElem : connectors) {
                        JsonObject conn = connElem.getAsJsonObject();
                        detectChargingSession(chargePointId, pileSn, conn, energy, chargingMethod);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[DeviceEvent] Failed to process DLM for {}: {}", chargePointId, e.getMessage());
        }
    }

    /**
     * 充电会话数据更新：DLMStatus 只负责更新进行中会话的 energy/duration/chargingMethod，
     * 会话的创建由 StartTransaction、结束由 StopTransaction 负责
     */
    private void detectChargingSession(String deviceSn, String pileSn, JsonObject conn,
                                       Integer energy, Integer chargingMethod) {
        try {
            int connectorId = conn.has("connectorId") ? conn.get("connectorId").getAsInt() : 0;
            if (connectorId == 0) {
                return;
            }
            Integer duration = conn.has("duration") && !conn.get("duration").isJsonNull()
                    ? conn.get("duration").getAsInt() : null;

            // 查找该枪的进行中会话（由 StartTransaction 创建）
            NcChargingSession active = chargingSessionMapper.selectOne(
                    new LambdaQueryWrapper<NcChargingSession>()
                            .eq(NcChargingSession::getDeviceSn, deviceSn)
                            .eq(NcChargingSession::getPileSn, pileSn)
                            .eq(NcChargingSession::getConnectorId, connectorId)
                            .eq(NcChargingSession::getStatus, NcChargingSession.CHARGING)
                            .last("LIMIT 1")
            );

            if (active != null) {
                active.setDuration(duration);
                // 兜底：DLM 在 Finishing 状态会上报 energy=0，不能覆盖已有电量
                if (energy != null && energy > 0) {
                    active.setEnergy(energy);
                }
                if (chargingMethod != null) {
                    active.setChargingMethod(chargingMethod);
                }
                chargingSessionMapper.updateById(active);
                log.info("[充电] DLM数据更新 — 设备={}, 桩={}, 枪={}, txId={}, 电量={}Wh, 时长={}秒, 模式={}",
                        deviceSn, pileSn, connectorId, active.getTransactionId(), energy, duration,
                        chargingMethod != null && chargingMethod == 0 ? "iCharger" : "N3Lite");
            }
        } catch (Exception e) {
            log.warn("[ChargingSession] Update failed for {}/{}: {}", deviceSn, pileSn, e.getMessage());
        }
    }

    private Date parseUnixTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return null;
        }
        return new Date(Long.parseLong(timestamp) * 1000);
    }

    private void handleFirmwareStatus(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);
        String status = getJsonString(payload, "status");
        log.info("[DeviceEvent] FirmwareStatusNotification: sn={}, status={}", chargePointId, status);

        // 查找该设备正在进行中的升级任务
        FirmwareUpgradeTask task = upgradeTaskService.getOne(
                new LambdaQueryWrapper<FirmwareUpgradeTask>()
                        .eq(FirmwareUpgradeTask::getDeviceSn, chargePointId)
                        .in(FirmwareUpgradeTask::getStatus, BizConstant.TASK_PENDING, BizConstant.TASK_DOWNLOADING, BizConstant.TASK_INSTALLING)
                        .orderByDesc(FirmwareUpgradeTask::getCreateTime)
                        .last("LIMIT 1")
        );
        if (task == null) {
            log.warn("[DeviceEvent] No active upgrade task for device: {}", chargePointId);
            return;
        }

        // 映射 OCPP 状态到任务状态
        String taskStatus = task.getStatus();
        int progress = task.getProgress();
        String msg = "";

        switch (status) {
            case "Downloading":
                taskStatus = BizConstant.TASK_DOWNLOADING;
                progress = 30;
                msg = "设备正在下载固件";
                break;
            case "Downloaded":
                taskStatus = BizConstant.TASK_DOWNLOADING;
                progress = 100;
                msg = "固件下载完成";
                break;
            case "Installing":
                taskStatus = BizConstant.TASK_INSTALLING;
                progress = 50;
                msg = "设备正在安装固件";
                break;
            case "Installed":
                taskStatus = BizConstant.TASK_COMPLETED;
                progress = 100;
                msg = "固件升级完成";
                task.setFinishTime(new Date());
                updateDeviceFirmwareVersion(chargePointId, task.getFirmwareId());
                break;
            case "DownloadFailed":
                taskStatus = BizConstant.TASK_FAILED;
                msg = "固件下载失败";
                task.setErrorMsg(msg);
                task.setFinishTime(new Date());
                break;
            case "InstallationFailed":
                taskStatus = BizConstant.TASK_FAILED;
                msg = "固件安装失败";
                task.setErrorMsg(msg);
                task.setFinishTime(new Date());
                break;
            case "Idle":
                // 设备空闲，忽略
                return;
            default:
                log.warn("[DeviceEvent] Unknown firmware status: {}", status);
                return;
        }

        task.setStatus(taskStatus);
        task.setProgress(progress);
        upgradeTaskService.updateById(task);

        // 升级失败时补记操作日志
        if (BizConstant.TASK_FAILED.equals(taskStatus)) {
            NcOpLog failLog = new NcOpLog();
            failLog.setDeviceSn(chargePointId);
            failLog.setOpUser(task.getCreateBy() != null ? task.getCreateBy() : "system");
            failLog.setOpType(NcOpLog.OTA_UPGRADE);
            failLog.setOpContent(msg);
            failLog.setOpResult(NcOpLog.FAIL);
            failLog.setFailReason(msg);
            failLog.setOpTime(new Date());
            failLog.setCreateTime(new Date());
            opLogService.save(failLog);
            log.info("[DeviceEvent] OTA failure logged: sn={}, reason={}", chargePointId, msg);
        }

        // 推送到 Web 端 WebSocket
        JsonObject wsMsg = new JsonObject();
        wsMsg.addProperty("taskId", task.getId());
        wsMsg.addProperty("deviceSn", chargePointId);
        wsMsg.addProperty("status", taskStatus);
        wsMsg.addProperty("progress", progress);
        wsMsg.addProperty("message", msg);
        OtaWebSocket.sendMessage(chargePointId, wsMsg.toString());

        // 推送到 App 端统一 WebSocket
        JSONObject appOtaMsg = new JSONObject();
        appOtaMsg.put("taskId", task.getId());
        appOtaMsg.put("deviceSn", chargePointId);
        appOtaMsg.put("status", taskStatus);
        appOtaMsg.put("progress", progress);
        appOtaMsg.put("message", msg);
        AppWebSocket.publish("ota:" + task.getId(), appOtaMsg);
    }

    /**
     * OTA 升级成功后，根据固件记录更新设备的固件版本号
     */
    private void updateDeviceFirmwareVersion(String deviceSn, String firmwareId) {
        try {
            FirmwareVersion firmware = firmwareVersionService.getById(firmwareId);
            if (firmware == null) {
                log.warn("[OTA] Firmware not found: firmwareId={}", firmwareId);
                return;
            }
            NcDevice device = ncDeviceService.getOne(
                    new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn)
            );
            if (device == null) {
                log.warn("[OTA] Device not found: sn={}", deviceSn);
                return;
            }
            device.setFirmwareVersion(firmware.getVersion());
            ncDeviceService.updateById(device);
            log.info("[OTA] Device firmware version updated: sn={}, version={}", deviceSn, firmware.getVersion());
        } catch (Exception e) {
            log.error("[OTA] Failed to update device firmware version: sn={}, error={}", deviceSn, e.getMessage());
        }
    }

    /**
     * OCPP connector 状态映射到设备在线状态
     */
    private String mapConnectorStatus(String ocppStatus) {
        if (ocppStatus == null) {
            return BizConstant.DEVICE_ONLINE;
        }
        switch (ocppStatus) {
            case BizConstant.OCPP_AVAILABLE:
            case "Charging":
            case "SuspendedEV":
            case "SuspendedEVSE":
            case "Preparing":
            case "Finishing":
                return BizConstant.DEVICE_ONLINE;
            case BizConstant.OCPP_FAULTED:
                return BizConstant.DEVICE_FAULT;
            case BizConstant.OCPP_UNAVAILABLE:
                return BizConstant.DEVICE_OFFLINE;
            default:
                return BizConstant.DEVICE_ONLINE;
        }
    }

    /**
     * 根据型号猜测设备类型
     */
    private String guessDeviceType(String model) {
        if (model == null) {
            return BizConstant.TYPE_N3_LITE;
        }
        String lower = model.toLowerCase();
        if (lower.contains("atp")) {
            return BizConstant.TYPE_ATP_III;
        }
        return BizConstant.TYPE_N3_LITE;
    }

    /**
     * 广播设备状态变化到前端
     * @param eventType ONLINE / OFFLINE / FAULT / ALERT
     */
    private void broadcastDeviceStatus(String sn, String eventType, String detail) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", eventType);
        msg.addProperty("deviceSn", sn);
        msg.addProperty("detail", detail);
        msg.addProperty("timestamp", java.time.Instant.now().toString());
        FrontendPushChannel.broadcast(msg.toString());
    }

    // ==================== 充电事务处理 ====================

    /**
     * 处理 StartTransaction — 设备开始充电，创建充电会话
     */
    private void handleStartTransaction(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);
        int transactionId = payload.get("transactionId").getAsInt();
        int connectorId = payload.has("connectorId") ? payload.get("connectorId").getAsInt() : 1;
        String idTag = getJsonString(payload, "idTag");
        int meterStart = payload.has("meterStart") ? payload.get("meterStart").getAsInt() : 0;

        log.info("[充电] 处理StartTransaction — 设备={}, txId={}, 枪={}, idTag={}, meterStart={}",
                chargePointId, transactionId, connectorId, idTag, meterStart);

        // 通过 connectorId 查找对应的桩 SN
        String pileSn = findPileSnByConnector(chargePointId, connectorId);

        // 先查是否已有 CHARGING 会话（可能由 DLMStatus detectChargingSession 创建）
        NcChargingSession existing = chargingSessionMapper.selectOne(
                new LambdaQueryWrapper<NcChargingSession>()
                        .eq(NcChargingSession::getDeviceSn, chargePointId)
                        .eq(NcChargingSession::getPileSn, pileSn)
                        .eq(NcChargingSession::getConnectorId, connectorId)
                        .eq(NcChargingSession::getStatus, NcChargingSession.CHARGING)
                        .last("LIMIT 1"));

        if (existing != null) {
            // 已有会话，补上 transactionId 和 meterStart
            existing.setTransactionId(transactionId);
            if (meterStart > 0) {
                existing.setMeterStart(meterStart);
            }
            chargingSessionMapper.updateById(existing);
            log.info("[充电] 订单已存在，补充txId — 设备={}, 桩={}, 枪={}, txId={}",
                    chargePointId, pileSn, connectorId, transactionId);
        } else {
            // 没有会话，新建
            NcChargingSession session = new NcChargingSession();
            session.setDeviceSn(chargePointId);
            session.setPileSn(pileSn);
            session.setConnectorId(connectorId);
            session.setTransactionId(transactionId);
            session.setStartTime(new Date());
            session.setEnergy(0);
            session.setMeterStart(meterStart > 0 ? meterStart : null);
            session.setDuration(0);
            session.setStatus(NcChargingSession.CHARGING);
            session.setCreateTime(new Date());
            chargingSessionMapper.insert(session);
            log.info("[充电] 创建充电订单 — 设备={}, 桩={}, 枪={}, txId={}, meterStart={}",
                    chargePointId, pileSn, connectorId, transactionId, meterStart);
        }
    }

    /**
     * 处理 StopTransaction — 设备结束充电，更新充电会话
     */
    private void handleStopTransaction(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);
        int transactionId = payload.get("transactionId").getAsInt();
        int meterStop = payload.has("meterStop") ? payload.get("meterStop").getAsInt() : 0;
        String reason = getJsonString(payload, "reason");

        log.info("[充电] 处理StopTransaction — 设备={}, txId={}, meterStop={}, reason={}",
                chargePointId, transactionId, meterStop, reason);

        // 通过 transactionId 查找最近的充电会话（不限状态，可能已被 detectChargingSession 标记 FINISHED）
        // transactionId 可能重复（服务重启后 AtomicInteger 归零），所以取最新的一条
        NcChargingSession session = chargingSessionMapper.selectOne(
                new LambdaQueryWrapper<NcChargingSession>()
                        .eq(NcChargingSession::getDeviceSn, chargePointId)
                        .eq(NcChargingSession::getTransactionId, transactionId)
                        .orderByDesc(NcChargingSession::getCreateTime)
                        .last("LIMIT 1")
        );

        if (session == null) {
            log.warn("[充电] 未找到充电订单 — 设备={}, txId={}", chargePointId, transactionId);
            return;
        }

        Date now = new Date();
        if (session.getEndTime() == null) {
            session.setEndTime(now);
        }
        session.setStatus(NcChargingSession.FINISHED);
        if (session.getStartTime() != null) {
            session.setDuration((int) ((now.getTime() - session.getStartTime().getTime()) / 1000));
        }
        // 电量计算：优先用 meterStop - meterStart 差值，算不出则保留 DLMStatus 的 energy
        if (meterStop > 0 && session.getMeterStart() != null && session.getMeterStart() > 0) {
            int meterEnergy = meterStop - session.getMeterStart();
            if (meterEnergy > 0) {
                session.setEnergy(meterEnergy);
            }
        }
        chargingSessionMapper.updateById(session);

        log.info("[充电] 订单结束 — 设备={}, 桩={}, 枪={}, txId={}, 时长={}秒, 电量={}Wh, meterStart={}, meterStop={}, reason={}",
                chargePointId, session.getPileSn(), session.getConnectorId(),
                transactionId, session.getDuration(), session.getEnergy(),
                session.getMeterStart(), meterStop, reason);
    }

    /**
     * 通过 connectorId 反查桩 SN：connector 属于哪个桩
     */
    private String findPileSnByConnector(String gatewaySn, int connectorId) {
        NcDevice gateway = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, gatewaySn));
        if (gateway == null) {
            return gatewaySn;
        }

        // 查该网关下所有桩
        List<NcDevice> piles = ncDeviceService.list(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getParentDeviceId, gateway.getId()));

        for (NcDevice pile : piles) {
            NcConnector connector = ncConnectorService.getOne(
                    new LambdaQueryWrapper<NcConnector>()
                            .eq(NcConnector::getDeviceId, pile.getId())
                            .eq(NcConnector::getConnectorId, connectorId));
            if (connector != null) {
                return pile.getSn();
            }
        }

        // 找不到就用网关 SN
        log.warn("[DeviceEvent] No pile found for connector {} under gateway {}", connectorId, gatewaySn);
        return gatewaySn;
    }

    private String getJsonString(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString() : null;
    }
}
