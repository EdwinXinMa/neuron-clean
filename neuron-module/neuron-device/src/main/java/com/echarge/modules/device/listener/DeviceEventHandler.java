package com.echarge.modules.device.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventListener;
import com.echarge.modules.device.entity.NcConnector;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcConnectorService;
import com.echarge.modules.device.service.INcDeviceService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备事件消费端
 * 接收 protocol 模块发来的设备事件，执行业务逻辑
 */
@Slf4j
@Component
public class DeviceEventHandler implements DeviceEventListener {

    @Autowired
    private INcDeviceService ncDeviceService;

    @Autowired
    private INcConnectorService ncConnectorService;

    private final Gson gson = new Gson();

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
            device.setOnlineStatus("ONLINE");
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
            device.setOnlineStatus("ONLINE");
            device.setFirstOnlineTime(now);
            device.setLastOnlineTime(now);
            device.setLastHeartbeat(now);
            ncDeviceService.save(device);
            log.info("[DeviceEvent] New device registered (unregistered): sn={}", chargePointId);
        }
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
            device.setOnlineStatus("ONLINE");
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
        if (n3lite == null) return;

        if (connectorId == 0) {
            // N3 Lite 本体状态
            n3lite.setOnlineStatus("Faulted".equals(status) ? "FAULT" : "ONLINE");
            ncDeviceService.updateById(n3lite);
            log.info("[DeviceEvent] N3 Lite status updated: sn={}, status={}", chargePointId, status);
        } else if (connectorId > 0) {
            // 更新枪状态
            NcConnector connector = ncConnectorService.getOne(
                    new LambdaQueryWrapper<NcConnector>()
                            .eq(NcConnector::getConnectorId, connectorId)
                            .inSql(NcConnector::getDeviceId,
                                    "SELECT id FROM nc_device WHERE parent_device_id = '" + n3lite.getId() + "'")
            );
            if (connector != null) {
                connector.setStatus(status != null ? status : "Available");
                connector.setUpdateTime(new Date());
                ncConnectorService.updateById(connector);
                log.info("[DeviceEvent] Connector status updated: connectorId={}, status={}", connectorId, status);

                // 检查该桩下所有枪是否都故障，如果是则桩也标记故障
                String pileId = connector.getDeviceId();
                List<NcConnector> allConnectors = ncConnectorService.list(
                        new LambdaQueryWrapper<NcConnector>().eq(NcConnector::getDeviceId, pileId)
                );
                boolean allFaulted = allConnectors.stream().allMatch(c -> "Faulted".equals(c.getStatus()));
                NcDevice pile = ncDeviceService.getById(pileId);
                if (pile != null) {
                    pile.setOnlineStatus(allFaulted ? "FAULT" : "ONLINE");
                    ncDeviceService.updateById(pile);
                }
            }
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
            device.setOnlineStatus("OFFLINE");
            ncDeviceService.updateById(device);
            log.info("[DeviceEvent] Device offline: sn={}", chargePointId);

            // N3 Lite 离线 → 下挂桩也全部离线，桩下的枪状态改为 Unavailable
            List<NcDevice> children = ncDeviceService.list(
                    new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getParentDeviceId, device.getId())
            );
            for (NcDevice child : children) {
                child.setOnlineStatus("OFFLINE");
                ncDeviceService.updateById(child);
                log.info("[DeviceEvent] Child device offline: sn={}", child.getSn());

                // 该桩下所有枪状态改为 Unavailable
                List<NcConnector> connectors = ncConnectorService.list(
                        new LambdaQueryWrapper<NcConnector>().eq(NcConnector::getDeviceId, child.getId())
                );
                for (NcConnector connector : connectors) {
                    connector.setStatus("Unavailable");
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
        if (chargePoints == null) return;

        Date now = new Date();

        // 收集本次上报的所有桩SN，用于后续删除不存在的桩
        List<String> reportedPileSns = new ArrayList<>();
        // 收集本次上报的所有枪，key=桩ID，value=connectorId列表
        Map<String, List<Integer>> reportedConnectors = new HashMap<>();

        for (JsonElement cpElement : chargePoints) {
            JsonObject cp = cpElement.getAsJsonObject();
            String sn = getJsonString(cp, "sn");
            String model = getJsonString(cp, "model");

            if (sn == null) continue;
            reportedPileSns.add(sn);

            // 处理桩
            NcDevice pile = ncDeviceService.getOne(
                    new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn)
            );

            if (pile != null) {
                pile.setParentDeviceId(parent.getId());
                pile.setOnlineStatus("ONLINE");
                pile.setDeviceModel(model);
                ncDeviceService.updateById(pile);
                log.info("[DeviceEvent] Pile updated: sn={}, parent={}", sn, chargePointId);
            } else {
                pile = new NcDevice();
                pile.setSn(sn);
                pile.setDeviceType("ATP_III");
                pile.setDeviceModel(model);
                pile.setParentDeviceId(parent.getId());
                pile.setOnlineStatus("ONLINE");
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

                    if (connectorId == 0) continue;
                    connectorIds.add(connectorId);

                    NcConnector connector = ncConnectorService.getOne(
                            new LambdaQueryWrapper<NcConnector>()
                                    .eq(NcConnector::getDeviceId, pile.getId())
                                    .eq(NcConnector::getConnectorId, connectorId)
                    );

                    if (connector != null) {
                        connector.setStatus(connStatus != null ? connStatus : "Available");
                        connector.setUpdateTime(now);
                        ncConnectorService.updateById(connector);
                    } else {
                        connector = new NcConnector();
                        connector.setDeviceId(pile.getId());
                        connector.setConnectorId(connectorId);
                        connector.setStatus(connStatus != null ? connStatus : "Available");
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
     * OCPP connector 状态映射到设备在线状态
     */
    private String mapConnectorStatus(String ocppStatus) {
        if (ocppStatus == null) return "ONLINE";
        switch (ocppStatus) {
            case "Available":
            case "Charging":
            case "SuspendedEV":
            case "SuspendedEVSE":
            case "Preparing":
            case "Finishing":
                return "ONLINE";
            case "Faulted":
                return "FAULT";
            case "Unavailable":
                return "OFFLINE";
            default:
                return "ONLINE";
        }
    }

    /**
     * 根据型号猜测设备类型
     */
    private String guessDeviceType(String model) {
        if (model == null) return "N3_LITE";
        String lower = model.toLowerCase();
        if (lower.contains("atp")) return "ATP_III";
        return "N3_LITE";
    }

    private String getJsonString(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString() : null;
    }
}
