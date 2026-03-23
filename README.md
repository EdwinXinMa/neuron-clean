# NeuronCloud — N3 Lite 智能充电管理平台

> Intelligent Charging Management Cloud Platform by AlwaysControl Technology

[![License](https://img.shields.io/badge/license-Proprietary-red.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![OCPP](https://img.shields.io/badge/OCPP-1.6%20%2F%202.0.1-orange.svg)](https://www.openchargealliance.org/)

---

## 项目介绍

NeuronCloud 是旭衡电子（AlwaysControl Technology）为 **N3 Lite 智能充电管理网关** 及 **ATP III 充电桩** 打造的云端管理平台。

平台基于 Spring Boot 3 + Netty 构建，实现了对充电设备的远程接入、状态监控、告警管理及数据采集，支持 OCPP 1.6 / 2.0.1 双版本协议，为充电运营商提供稳定可靠的云端管控能力。

---

## 核心功能

- **设备接入**：支持 OCPP 1.6 / 2.0.1 双版本 WebSocket 接入，自动识别协议版本
- **设备管理**：充电站台账、拓扑管理、设备状态实时监控
- **告警管理**：告警规则配置、告警事件记录与通知
- **数据采集**：充电数据采集、统计与上报
- **远程控制**：远程启停充电、配置下发（DLM）
- **OTA 升级**：设备固件远程升级管理
- **系统管理**：用户、角色、权限、菜单、日志等基础运维功能

---

## 技术架构

### 后端

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.5.5 |
| MyBatis-Plus | 3.5.x |
| Netty | 4.x |
| Redis | 6+ |
| MySQL | 5.7+ |

### 协议支持

| 协议 | 说明 |
|------|------|
| OCPP 1.6 | JSON over WebSocket，端口 9001 |
| OCPP 2.0.1 | JSON over WebSocket，端口 9001 |

### 模块结构

```
neuron-clean
├── neuron-core          # 基础核心（公共工具、配置、权限）
├── neuron-module        # 业务模块
│   ├── neuron-protocol  # OCPP 协议引擎（Netty WebSocket）
│   ├── neuron-device    # 设备管理（台账、拓扑、状态）
│   └── neuron-alert     # 告警管理
└── neuron-system        # 系统管理（用户/权限/字典等）
    ├── neuron-system-api
    ├── neuron-system-impl
    └── neuron-system-start
```

---

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 5.7+
- Redis 6+

### 配置

1. 克隆项目
2. 初始化数据库，执行 `db/` 目录下的 SQL 脚本（`neuron_cloud_init.sql`）
3. 修改 `application-dev.yml` 中的数据库和 Redis 连接配置
4. 启动 `neuron-system-start` 主程序

### Docker 启动

```bash
docker-compose up -d
```

---

## 相关文档

- [架构设计文档](docs/NeuronCloud_Architecture_v5.0.html)
- [通信协议文档](docs/N3Lite云平台通信协议文档.html)
- [产品说明书](docs/N3Lite产品说明书v1.0.pdf)

---

## 版权声明

Copyright (c) 2025 AlwaysControl Technology (Shenzhen) Co., Ltd. All Rights Reserved.

本软件为旭衡电子（深圳）有限公司专有软件，未经授权禁止使用、复制或分发。详见 [LICENSE](./LICENSE)。
