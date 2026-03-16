# OCPP 1.6 协议详解

> Open Charge Point Protocol 1.6-J (JSON over WebSocket)
> 本文档面向 NeuronSystem 云平台开发团队，涵盖协议背景、核心价值、技术细节，以及充电桩从注册到完成交易的完整交互流程。

---

## 目录

1. [OCPP 是什么](#1-ocpp-是什么)
2. [为什么需要 OCPP](#2-为什么需要-ocpp)
3. [OCPP 的核心优势](#3-ocpp-的核心优势)
4. [OCPP 能解决什么问题](#4-ocpp-能解决什么问题)
5. [NeuronSystem 为什么要支持 OCPP](#5-neuronsystem-为什么要支持-ocpp)
6. [OCPP 1.6 协议架构](#6-ocpp-16-协议架构)
7. [完整交互流程：从注册到交易完成](#7-完整交互流程从注册到交易完成)
8. [消息类型与格式](#8-消息类型与格式)
9. [核心消息一览表](#9-核心消息一览表)
10. [与 NeuronSystem 的结合](#10-与-neuronsystem-的结合)

---

## 1. OCPP 是什么

**OCPP（Open Charge Point Protocol）** 是由 **OCA（Open Charge Alliance，开放充电联盟）** 制定的一套开放通信协议，用于 **充电桩（Charge Point）** 与 **中央管理系统（Central System / CSMS）** 之间的通信。

简单理解：**OCPP 就是充电桩和云平台之间"说话"的标准语言。**

### 版本演进

| 版本 | 发布时间 | 传输方式 | 状态 |
|------|---------|---------|------|
| OCPP 1.2 | 2010 | SOAP/XML | 已淘汰 |
| OCPP 1.5 | 2012 | SOAP/XML | 逐步淘汰 |
| **OCPP 1.6** | **2015** | **SOAP/XML 或 JSON/WebSocket** | **主流，广泛部署** |
| OCPP 2.0.1 | 2020 | JSON/WebSocket | 新一代，逐步推广 |

**OCPP 1.6-J**（J = JSON）是当前全球部署量最大的版本，绝大多数充电桩厂商都支持。

---

## 2. 为什么需要 OCPP

### 2.1 行业痛点：没有标准之前

在 OCPP 出现之前，充电行业面临严重的碎片化问题：

- **每家桩企都有私有协议**：ABB、Schneider、Delta 等厂商各自定义通信协议，互不兼容
- **运营商被厂商绑定**：选了某家的桩，就必须用他家的后台系统，想换桩？整个系统推倒重来
- **互联互通困难**：A 公司的 APP 找不到 B 公司的桩，用户体验极差
- **开发成本高昂**：每接入一种桩，就要重新开发一套对接适配层
- **运维噩梦**：不同协议的桩混合部署时，维护和升级极其复杂

### 2.2 类比理解

想象一下如果每个手机厂商都用自己的充电接口——这就是 OCPP 出现之前充电行业的状态。OCPP 就相当于充电行业的 **USB-C 标准**。

---

## 3. OCPP 的核心优势

### 3.1 开放标准，厂商无关

- 任何厂商都可以免费实现 OCPP
- 运营商可以自由选择和混用不同品牌的充电桩
- 避免厂商锁定（Vendor Lock-in）

### 3.2 降低对接成本

- 云平台只需实现一套协议，即可接入市面上绝大多数充电桩
- 新桩接入从"月级开发"降低到"天级配置"
- 统一的消息格式，降低开发和调试难度

### 3.3 互联互通

- 不同运营商的桩可以通过同一平台管理
- 为漫游（Roaming）提供基础——用户用一个 APP 可以在多个运营商的桩上充电
- 支持 OCPI、OICP 等漫游协议的底层前提

### 3.4 远程管理能力

- 远程启停充电、远程固件升级
- 远程修改配置、远程诊断
- 实时监控桩的状态和充电数据

### 3.5 成熟且经过验证

- 全球数百万充电桩在使用
- 经过十多年的迭代和验证
- 丰富的开源实现和测试工具

---

## 4. OCPP 能解决什么问题

| 问题域 | OCPP 解决方案 |
|--------|-------------|
| **设备管理** | BootNotification 注册、Heartbeat 心跳、StatusNotification 状态上报 |
| **充电控制** | RemoteStartTransaction / RemoteStopTransaction 远程启停 |
| **计量计费** | MeterValues 电表数据上报、StartTransaction / StopTransaction 交易记录 |
| **用户认证** | Authorize 刷卡/扫码鉴权、本地白名单（LocalAuthList） |
| **固件升级** | UpdateFirmware 远程 OTA、FirmwareStatusNotification 状态回调 |
| **远程配�** | ChangeConfiguration 修改参数、GetConfiguration 查询参数 |
| **故障诊断** | DiagnosticsStatusNotification、GetDiagnostics 获取日志 |
| **负载管理** | SetChargingProfile 设置充电功率曲线（智能调度） |
| **安全性** | TLS 加密通信、证书认证 |

---

## 5. NeuronSystem 为什么要支持 OCPP

### 5.1 海外市场的硬性门槛

#### OCPP 是出海的"入场券"
- 欧洲、北美、澳洲等主流市场，**OCPP 兼容性是充电运营商的基本采购要求**
- 欧盟 AFIR（Alternative Fuels Infrastructure Regulation）法规明确要求充电基础设施支持开放协议
- 美国 NEVI（National Electric Vehicle Infrastructure）联邦补贴项目，**强制要求充电桩支持 OCPP 1.6 或以上**
- 不支持 OCPP = 无法参与海外招投标，直接被排除在市场之外

#### 海外客户的真实诉求
- 海外运营商管理的桩来自多个品牌，**他们需要一个平台能统一管理所有 OCPP 桩**
- 运营商不会为了接入某一家的桩去部署那家的私有后台——这在海外是不可接受的
- 支持 OCPP 意味着 NeuronSystem 可以作为**第三方运营平台**，接入任意品牌的标准桩

### 5.2 巨大的市场空间与发展前景

#### 全球充电桩爆发式增长
- 全球公共充电桩保有量已超过 **400 万台**（2024），其中海外市场增速超过 40%
- IEA 预测到 2030 年全球需要 **1500 万+** 公共充电桩
- 欧洲 2035 年禁售燃油车，充电基础设施需求将持续井喷
- 东南亚、中东、南美等新兴市场正在起步，是蓝海

#### OCPP 的市场渗透率
- 全球新部署的充电桩中，**超过 80% 支持 OCPP**
- OCPP 1.6 是当前绝对主流，市场占有率最高
- OCPP 2.0.1 正在逐步推广，但 1.6 仍将长期共存（至少 5-8 年）
- 支持 OCPP 1.6 + 2.0.1 双版本，可以覆盖几乎所有海外充电桩

#### 充电运营是长期生意
- 充电桩不是一次性买卖，运营商需要**持续的平台服务**（SaaS 模式）
- 每接入一台桩 = 一个长期付费客户
- 平台的价值随接入桩数增长而增长（网络效应）

### 5.3 NeuronSystem 的战略价值

#### 双协议架构的差异化优势
```
NeuronSystem 云平台
├── MQTT 接入层（自研设备）    ──► N3 网关 + ATP III 充电桩（自有产品线）
└── OCPP 接入层（标准设备）    ──► 任意第三方 OCPP 充电桩（开放生态）
```

- **自有产品用 MQTT**：N3 网关通过 MQTT 接入，协议灵活、数据丰富、可深度定制
- **第三方产品用 OCPP**：标准充电桩通过 OCPP 接入，即插即用、生态开放
- 这种**双协议架构**在市场上是差异化竞争力——既有自研产品的深度，又有开放生态的广度

#### 从硬件商到平台商的跃迁
- 只卖硬件（ATP III + N3）：利润有限，市场天花板低
- **硬件 + 平台**：通过 OCPP 接入第三方桩，NeuronSystem 变成**充电运营平台**
- 平台可以输出给海外运营商、物业、车队管理公司等，商业模式从"卖设备"升级为"卖服务"

#### 互联互通的基础
- 支持 OCPP 后，可以进一步对接 **OCPI（Open Charge Point Interface）** 漫游协议
- 实现跨运营商的充电漫游——用户用一个 APP 在多个运营商的桩上充电
- 这是海外充电网络的核心需求，也是平台估值的关键指标

### 5.4 技术层面的务实选择

#### 为什么先做 OCPP 1.6 而不是直接上 2.0.1
- **1.6 是存量最大的版本**：全球数百万台桩在用，接入即有客户
- **1.6 实现复杂度适中**：消息集固定（28 个），协议文档成熟，开源参考多
- **2.0.1 向下兼容性好**：1.6 的核心概念在 2.0.1 中都保留了，先做 1.6 的架构设计可以平滑升级
- **neuron-protocol 已有基础**：Netty WebSocket 网关已搭建（端口 9001），支持 1.6-J 和 2.0.1 的消息解析

#### 投入产出比高
- 实现 OCPP 1.6 的核心流程（注册→鉴权→充电→计费），开发量约 2-3 人月
- 完成后即可接入市面上绝大多数充电桩，**一次开发，长期受益**
- 相比为每个厂商的私有协议做适配，OCPP 的 ROI 高出一个数量级

### 5.5 需要注意的挑战

尽管 OCPP 是必选项，实践中仍有一些挑战需要应对：

| 挑战 | 应对策略 |
|------|---------|
| **厂商实现差异大** | 建立桩型测试矩阵，逐步积累兼容性适配库 |
| **1.6 安全模型较弱** | 启用 TLS + Basic Auth，关键操作加业务层校验 |
| **WebSocket 长连接压力** | Netty 天然支持高并发长连接，当前架构已能支撑万级设备 |
| **离线场景数据可靠性** | 结合 LocalAuthList + StopTransaction 补报机制 |
| **缺少标准计费模型** | 平台侧自建计费引擎，通过 SetChargingProfile 下发功率策略 |
| **协议扩展性有限** | 标准功能用 OCPP，增值功能走 DataTransfer 或 MQTT 通道 |

---

## 6. OCPP 1.6 协议架构

### 6.1 通信模型

```
+------------------+          WebSocket (JSON)          +------------------+
|                  |  ◄──────────────────────────────►  |                  |
|   Charge Point   |         wss://host/ocpp/           |  Central System  |
|   (充电桩)        |         长连接，双向通信              |  (云平台)         |
|                  |                                     |                  |
+------------------+                                     +------------------+
```

- **传输层**：WebSocket over TLS（wss://）
- **数据格式**：JSON
- **连接方向**：充电桩主动连接云平台（桩是 WebSocket Client）
- **通信模式**：双向 RPC——双方都可以主动发起请求

### 6.2 角色定义

| 角色 | 说明 |
|------|------|
| **Charge Point (CP)** | 充电桩，是 WebSocket 客户端，主动连接到中央系统 |
| **Central System (CS)** | 云平台/后台管理系统，是 WebSocket 服务端 |

### 6.3 连接 URL 规范

```
wss://{host}:{port}/ocpp/{chargePointIdentity}
```

- `chargePointIdentity`：桩的唯一标识符（通常是序列号）
- 示例：`wss://charge.alwayscontrol.net:9001/ocpp/ACT-N3-20240001`

---

## 7. 完整交互流程：从注册到交易完成

以下是一个充电桩从首次上线到完成一笔充电交易的**完整生命周期**。

### 7.1 阶段一：建立连接

```
充电桩                                              云平台
  |                                                   |
  |  ──── WebSocket 握手 (wss://host/ocpp/CP001) ──►  |
  |                                                   |
  |  ◄──── WebSocket 101 Switching Protocols ────────  |
  |                                                   |
  |            [ WebSocket 长连接建立 ]                  |
```

**关键点：**
- 桩上电后，根据配置的 URL 主动发起 WebSocket 连接
- URL 中包含桩的身份标识（ChargePointIdentity）
- 云平台可以在握手阶段通过 HTTP Header 做初步认证
- 支持 Basic Auth 或 TLS 客户端证书认证

### 7.2 阶段二：启动注册（BootNotification）

```
充电桩                                              云平台
  |                                                   |
  |  ──── BootNotification.req ─────────────────────►  |
  |       {                                           |
  |         chargePointVendor: "AlwaysControl",       |
  |         chargePointModel: "ATP-III-AC-7kW",       |
  |         chargePointSerialNumber: "ACT20240001",   |
  |         firmwareVersion: "1.2.0",                 |
  |         meterType: "ACM100",                      |
  |         meterSerialNumber: "MTR20240001"          |
  |       }                                           |
  |                                                   |
  |  ◄──── BootNotification.conf ───────────────────  |
  |       {                                           |
  |         status: "Accepted",                       |
  |         currentTime: "2025-03-05T10:00:00Z",      |
  |         interval: 300                              |
  |       }                                           |
```

**BootNotification 的三种响应状态：**

| 状态 | 含义 | 桩的行为 |
|------|------|---------|
| `Accepted` | 注册成功 | 正常工作，按 interval 发心跳 |
| `Pending` | 等待审核 | 仅发心跳，不接受充电请求 |
| `Rejected` | 拒绝注册 | 等待 interval 后重新发送 BootNotification |

**关键点：**
- BootNotification 是桩连接后**必须**发送的第一条消息
- 云平台通过 `interval` 告知桩心跳间隔（秒）
- 云平台通过 `currentTime` 同步桩的时钟
- 如果状态是 Rejected，桩只能发 BootNotification，不能做其他操作

### 7.3 阶段三：状态上报（StatusNotification）

```
充电桩                                              云平台
  |                                                   |
  |  ──── StatusNotification.req ───────────────────►  |
  |       {                                           |
  |         connectorId: 0,      // 0=桩整体          |
  |         errorCode: "NoError",                     |
  |         status: "Available"                       |
  |       }                                           |
  |  ◄──── StatusNotification.conf ─────────────────  |
  |       {}                                          |
  |                                                   |
  |  ──── StatusNotification.req ───────────────────►  |
  |       {                                           |
  |         connectorId: 1,      // 1=枪1             |
  |         errorCode: "NoError",                     |
  |         status: "Available"                       |
  |       }                                           |
  |  ◄──── StatusNotification.conf ─────────────────  |
  |       {}                                          |
```

**充电桩连接器状态机：**

```
                    ┌──────────────┐
          ┌────────│  Available   │◄────────┐
          │        │   (空闲)      │         │
          │        └──────┬───────┘         │
          │               │ 插枪             │ 拔枪
          │               ▼                 │
          │        ┌──────────────┐         │
          │        │  Preparing   │─────────┤
          │        │  (准备中)     │         │
          │        └──────┬───────┘         │
          │               │ 鉴权通过         │
          │               ▼                 │
          │        ┌──────────────┐         │
          │        │  Charging    │─────────┘
          │        │  (充电中)     │
          │        └──────┬───────┘
          │               │ 充电结束
          │               ▼
          │        ┌──────────────┐
          │        │  Finishing   │──────────┐
          │        │  (结束中)     │          │
          │        └──────────────┘          │
          │                                  │ 拔枪
          │        ┌──────────────┐          │
          ├───────►│  Faulted     │          │
          │        │  (故障)       │          │
          │        └──────────────┘          │
          │                                  ▼
          │        ┌──────────────┐   ┌──────────────┐
          └───────►│ Unavailable  │   │  Available   │
                   │  (不可用)     │   │  (回到空闲)   │
                   └──────────────┘   └──────────────┘
```

**connectorId 说明：**
- `0`：代表充电桩整体
- `1, 2, ...`：代表各个充电枪/连接器

### 7.4 阶段四：心跳保活（Heartbeat）

```
充电桩                                              云平台
  |                                                   |
  |  ──── Heartbeat.req ───────────────────────────►  |
  |       {}                                          |
  |                                                   |
  |  ◄──── Heartbeat.conf ─────────────────────────  |
  |       {                                           |
  |         currentTime: "2025-03-05T10:05:00Z"       |
  |       }                                           |
  |                                                   |
  |         [ 每 interval 秒重复... ]                   |
```

**关键点：**
- 心跳间隔由 BootNotification 响应中的 `interval` 决定
- 云平台通过心跳判断桩是否在线
- 心跳响应中的 `currentTime` 用于桩的时钟校准
- 如果桩有其他消息发送（如 StatusNotification），可以重置心跳计时器

### 7.5 阶段五：用户鉴权（Authorize）

用户到桩前刷卡/扫码，桩发起鉴权请求：

```
充电桩                                              云平台
  |                                                   |
  |  ──── Authorize.req ───────────────────────────►  |
  |       {                                           |
  |         idTag: "USER_RFID_001"                    |
  |       }                                           |
  |                                                   |
  |  ◄──── Authorize.conf ─────────────────────────  |
  |       {                                           |
  |         idTagInfo: {                              |
  |           status: "Accepted",                     |
  |           expiryDate: "2025-12-31T23:59:59Z",     |
  |           parentIdTag: "PARENT_001"               |
  |         }                                         |
  |       }                                           |
```

**idTagInfo.status 取值：**

| 状态 | 含义 |
|------|------|
| `Accepted` | 鉴权通过，允许充电 |
| `Blocked` | 卡已被封锁 |
| `Expired` | 卡已过期 |
| `Invalid` | 卡号无效/未识别 |
| `ConcurrentTx` | 该卡已有进行中的交易 |

**鉴权触发场景：**
- 用户刷 RFID 卡
- 用户扫码（云平台下发 RemoteStartTransaction 前可先鉴权）
- 桩本地也可以维护一个白名单（LocalAuthList），离线时使用

### 7.6 阶段六：开始充电（StartTransaction）

鉴权通过后，桩开始充电并上报交易开始：

```
充电桩                                              云平台
  |                                                   |
  |  ──── StatusNotification.req ───────────────────►  |
  |       { connectorId: 1, status: "Preparing" }    |
  |  ◄──── StatusNotification.conf ─────────────────  |
  |                                                   |
  |  ──── StartTransaction.req ─────────────────────►  |
  |       {                                           |
  |         connectorId: 1,                           |
  |         idTag: "USER_RFID_001",                   |
  |         meterStart: 100000,    // Wh，电表起始读数  |
  |         timestamp: "2025-03-05T10:10:00Z"         |
  |       }                                           |
  |                                                   |
  |  ◄──── StartTransaction.conf ───────────────────  |
  |       {                                           |
  |         transactionId: 12345,  // 云平台分配交易ID  |
  |         idTagInfo: {                              |
  |           status: "Accepted"                      |
  |         }                                         |
  |       }                                           |
  |                                                   |
  |  ──── StatusNotification.req ───────────────────►  |
  |       { connectorId: 1, status: "Charging" }     |
  |  ◄──── StatusNotification.conf ─────────────────  |
```

**关键点：**
- `meterStart` 是电表的**绝对读数**（Wh），不是本次充电量
- 云平台返回 `transactionId`，后续所有该笔交易的消息都引用这个 ID
- 如果云平台返回 `Accepted` 以外的状态，桩应停止充电

### 7.7 阶段七：充电过程中的电表数据上报（MeterValues）

充电过程中，桩定期上报电表数据：

```
充电桩                                              云平台
  |                                                   |
  |  ──── MeterValues.req ─────────────────────────►  |
  |       {                                           |
  |         connectorId: 1,                           |
  |         transactionId: 12345,                     |
  |         meterValue: [{                            |
  |           timestamp: "2025-03-05T10:15:00Z",      |
  |           sampledValue: [                         |
  |             {                                     |
  |               value: "100500",                    |
  |               measurand: "Energy.Active.Import.Register", |
  |               unit: "Wh"                          |
  |             },                                    |
  |             {                                     |
  |               value: "7200",                      |
  |               measurand: "Power.Active.Import",   |
  |               unit: "W"                           |
  |             },                                    |
  |             {                                     |
  |               value: "32.1",                      |
  |               measurand: "Current.Import",        |
  |               unit: "A"                           |
  |             },                                    |
  |             {                                     |
  |               value: "224.5",                     |
  |               measurand: "Voltage",               |
  |               unit: "V"                           |
  |             },                                    |
  |             {                                     |
  |               value: "45",                        |
  |               measurand: "SoC",                   |
  |               unit: "Percent"                     |
  |             }                                     |
  |           ]                                       |
  |         }]                                        |
  |       }                                           |
  |                                                   |
  |  ◄──── MeterValues.conf ───────────────────────  |
  |       {}                                          |
  |                                                   |
  |       [ 每隔 MeterValueSampleInterval 秒重复 ]     |
```

**常见 Measurand（测量量）：**

| Measurand | 说明 | 单位 |
|-----------|------|------|
| `Energy.Active.Import.Register` | 累计充电电量 | Wh |
| `Power.Active.Import` | 当前充电功率 | W |
| `Current.Import` | 充电电流 | A |
| `Voltage` | 电压 | V |
| `SoC` | 电池 SOC（直流桩） | Percent |
| `Temperature` | 温度 | Celsius |

**关键点：**
- 上报间隔通过配置项 `MeterValueSampleInterval` 控制（默认 60 秒）
- 上报哪些测量量通过 `MeterValuesSampledData` 配置
- 平台用这些数据做实时监控和计费

### 7.8 阶段八：远程控制（云平台主动发起）

#### 场景A：用户在 APP 上扫码启动充电

```
充电桩                                              云平台
  |                                                   |
  |  ◄──── RemoteStartTransaction.req ──────────────  |
  |       {                                           |
  |         connectorId: 1,                           |
  |         idTag: "APP_USER_001",                    |
  |         chargingProfile: {    // 可选：限制功率     |
  |           chargingProfileId: 1,                   |
  |           stackLevel: 0,                          |
  |           chargingProfilePurpose: "TxProfile",    |
  |           chargingProfileKind: "Absolute",        |
  |           chargingSchedule: {                     |
  |             chargingRateUnit: "W",                |
  |             chargingSchedulePeriod: [             |
  |               { startPeriod: 0, limit: 7000 }    |
  |             ]                                     |
  |           }                                       |
  |         }                                         |
  |       }                                           |
  |                                                   |
  |  ──── RemoteStartTransaction.conf ──────────────►  |
  |       { status: "Accepted" }                      |
  |                                                   |
  |       [ 桩开始充电流程，后续同阶段六 ]                |
```

#### 场景B：远程停止充电

```
充电桩                                              云平台
  |                                                   |
  |  ◄──── RemoteStopTransaction.req ───────────────  |
  |       { transactionId: 12345 }                    |
  |                                                   |
  |  ──── RemoteStopTransaction.conf ───────────────►  |
  |       { status: "Accepted" }                      |
  |                                                   |
  |       [ 桩开始结束充电流程，后续同阶段九 ]             |
```

#### 场景C：智能充电/负载管理

```
充电桩                                              云平台
  |                                                   |
  |  ◄──── SetChargingProfile.req ──────────────────  |
  |       {                                           |
  |         connectorId: 1,                           |
  |         csChargingProfiles: {                     |
  |           chargingProfileId: 2,                   |
  |           stackLevel: 1,                          |
  |           chargingProfilePurpose: "TxProfile",    |
  |           chargingProfileKind: "Absolute",        |
  |           chargingSchedule: {                     |
  |             chargingRateUnit: "W",                |
  |             chargingSchedulePeriod: [             |
  |               { startPeriod: 0, limit: 3500 },   |
  |               { startPeriod: 3600, limit: 7000 } |
  |             ]                                     |
  |           }                                       |
  |         }                                         |
  |       }                                           |
  |                                                   |
  |  ──── SetChargingProfile.conf ──────────────────►  |
  |       { status: "Accepted" }                      |
```

**用途：** 谷电时段降功率、尖峰时段限流、多桩共享功率池（负载均衡）。

### 7.9 阶段九：结束充电（StopTransaction）

```
充电桩                                              云平台
  |                                                   |
  |  ──── StatusNotification.req ───────────────────►  |
  |       { connectorId: 1, status: "Finishing" }     |
  |  ◄──── StatusNotification.conf ─────────────────  |
  |                                                   |
  |  ──── StopTransaction.req ─────────────────────►  |
  |       {                                           |
  |         transactionId: 12345,                     |
  |         idTag: "USER_RFID_001",                   |
  |         meterStop: 107200,    // Wh，电表终止读数   |
  |         timestamp: "2025-03-05T11:10:00Z",        |
  |         reason: "Local",                          |
  |         transactionData: [{   // 可选：最终采样     |
  |           timestamp: "2025-03-05T11:10:00Z",      |
  |           sampledValue: [                         |
  |             {                                     |
  |               value: "107200",                    |
  |               measurand: "Energy.Active.Import.Register", |
  |               unit: "Wh"                          |
  |             }                                     |
  |           ]                                       |
  |         }]                                        |
  |       }                                           |
  |                                                   |
  |  ◄──── StopTransaction.conf ───────────────────  |
  |       {                                           |
  |         idTagInfo: {                              |
  |           status: "Accepted"                      |
  |         }                                         |
  |       }                                           |
  |                                                   |
  |  ──── StatusNotification.req ───────────────────►  |
  |       { connectorId: 1, status: "Available" }     |
  |  ◄──── StatusNotification.conf ─────────────────  |
```

**StopTransaction.reason 取值：**

| Reason | 含义 |
|--------|------|
| `Local` | 用户在桩上刷卡/按钮停止 |
| `Remote` | 云平台远程停止 |
| `EVDisconnected` | 车端拔枪 |
| `EmergencyStop` | 紧急停止 |
| `HardReset` | 硬件重置 |
| `PowerLoss` | 断电 |
| `DeAuthorized` | 鉴权被撤销 |
| `Other` | 其他原因 |

**计费计算（云平台侧）：**
```
充电量 = meterStop - meterStart = 107200 - 100000 = 7200 Wh = 7.2 kWh
充电时长 = 11:10 - 10:10 = 60 分钟
费用 = 7.2 kWh × 电价（含时段费率） + 服务费
```

### 7.10 完整流程时序图总览

```
充电桩 (CP)                                         云平台 (CS)
    |                                                   |
    |  ════════ 阶段一：连接 ══════════════════════════  |
    |  ──── WebSocket Connect ──────────────────────►   |
    |  ◄──── 101 Switching Protocols ───────────────    |
    |                                                   |
    |  ════════ 阶段二：注册 ══════════════════════════  |
    |  ──── BootNotification.req ───────────────────►   |
    |  ◄──── BootNotification.conf {Accepted} ──────    |
    |                                                   |
    |  ════════ 阶段三：状态上报 ════════════════════════ |
    |  ──── StatusNotification (conn=0, Available) ──►  |
    |  ──── StatusNotification (conn=1, Available) ──►  |
    |                                                   |
    |  ════════ 阶段四：心跳 ══════════════════════════  |
    |  ──── Heartbeat.req ──────────────────────────►   |
    |  ◄──── Heartbeat.conf ────────────────────────    |
    |        [ 重复 ... ]                               |
    |                                                   |
    |  ════════ 用户来充电 ═════════════════════════════ |
    |                                                   |
    |  ════════ 阶段五：鉴权 ══════════════════════════  |
    |  ──── Authorize.req {idTag} ──────────────────►   |
    |  ◄──── Authorize.conf {Accepted} ─────────────    |
    |                                                   |
    |  ════════ 阶段六：开始充电 ════════════════════════ |
    |  ──── StatusNotification (Preparing) ─────────►   |
    |  ──── StartTransaction.req ───────────────────►   |
    |  ◄──── StartTransaction.conf {txId: 12345} ───    |
    |  ──── StatusNotification (Charging) ──────────►   |
    |                                                   |
    |  ════════ 阶段七：充电中 ═════════════════════════ |
    |  ──── MeterValues.req ────────────────────────►   |
    |  ◄──── MeterValues.conf ──────────────────────    |
    |        [ 每 60 秒重复 ... ]                        |
    |                                                   |
    |  ════════ 阶段九：结束充电 ════════════════════════ |
    |  ──── StatusNotification (Finishing) ──────────►  |
    |  ──── StopTransaction.req ────────────────────►   |
    |  ◄──── StopTransaction.conf ──────────────────    |
    |  ──── StatusNotification (Available) ─────────►   |
    |                                                   |
    |  ════════ 回到心跳保活 ═══════════════════════════ |
    |  ──── Heartbeat.req ──────────────────────────►   |
    |        [ 等待下一个用户 ... ]                       |
```

---

## 8. 消息类型与格式

### 8.1 OCPP 1.6-J 消息帧格式

OCPP 1.6-J 定义了三种消息类型，通过 JSON 数组传输：

#### CALL（请求）
```json
[2, "unique-msg-id-001", "BootNotification", {
  "chargePointVendor": "AlwaysControl",
  "chargePointModel": "ATP-III"
}]
```
- `2` = MessageTypeId（CALL）
- `"unique-msg-id-001"` = UniqueId，用于匹配响应
- `"BootNotification"` = Action 名称
- `{...}` = Payload

#### CALLRESULT（成功响应）
```json
[3, "unique-msg-id-001", {
  "status": "Accepted",
  "currentTime": "2025-03-05T10:00:00Z",
  "interval": 300
}]
```
- `3` = MessageTypeId（CALLRESULT）
- UniqueId 与对应的 CALL 一致
- `{...}` = Response Payload

#### CALLERROR（错误响应）
```json
[4, "unique-msg-id-001", "FormationViolation", "Invalid JSON format", {}]
```
- `4` = MessageTypeId（CALLERROR）
- ErrorCode + ErrorDescription + ErrorDetails

### 8.2 错误码

| ErrorCode | 含义 |
|-----------|------|
| `NotImplemented` | 请求的 Action 未实现 |
| `NotSupported` | 请求的 Action 已识别但不支持 |
| `InternalError` | 内部错误 |
| `ProtocolError` | 协议错误 |
| `SecurityError` | 安全错误 |
| `FormationViolation` | 消息格式不合法 |
| `PropertyConstraintViolation` | 字段约束不满足 |
| `OccurenceConstraintViolation` | 必填字段缺失/多余字段 |
| `TypeConstraintViolation` | 字段类型错误 |
| `GenericError` | 通用错误 |

---

## 9. 核心消息一览表

### 9.1 充电桩发起的消息（CP → CS）

| 消息 | 用途 | 触发时机 |
|------|------|---------|
| `Authorize` | 用户鉴权 | 刷卡/扫码时 |
| `BootNotification` | 启动注册 | 桩上电/重连时 |
| `DataTransfer` | 自定义数据传输 | 厂商扩展功能 |
| `DiagnosticsStatusNotification` | 诊断上传状态 | 上传日志进度变化 |
| `FirmwareStatusNotification` | 固件升级状态 | 固件下载/安装进度变化 |
| `Heartbeat` | 心跳 | 周期性 |
| `MeterValues` | 电表数据上报 | 充电过程中周期性/触发式 |
| `StartTransaction` | 开始交易 | 用户启动充电时 |
| `StatusNotification` | 状态变更通知 | 桩/枪状态变化时 |
| `StopTransaction` | 结束交易 | 充电停止时 |

### 9.2 云平台发起的消息（CS → CP）

| 消息 | 用途 | 场景 |
|------|------|------|
| `CancelReservation` | 取消预约 | 用户取消预约 |
| `ChangeAvailability` | 修改可用状态 | 维护/启用充电枪 |
| `ChangeConfiguration` | 修改配置 | 远程调参 |
| `ClearCache` | 清除鉴权缓存 | 强制刷新本地缓存 |
| `ClearChargingProfile` | 清除充电配置 | 取消功率限制 |
| `DataTransfer` | 自定义数据传输 | 厂商扩展功能 |
| `GetCompositeSchedule` | 获取综合充电计划 | 查询当前功率限制 |
| `GetConfiguration` | 获取配置 | 查询桩参数 |
| `GetDiagnostics` | 获取诊断日志 | 远程排障 |
| `GetLocalListVersion` | 获取本地白名单版本 | 同步前检查 |
| `RemoteStartTransaction` | 远程启动充电 | APP 扫码充电 |
| `RemoteStopTransaction` | 远程停止充电 | APP 停止/异常停止 |
| `ReserveNow` | 预约充电 | 用户预约 |
| `Reset` | 重置桩 | Hard/Soft 重启 |
| `SendLocalList` | 更新本地白名单 | 离线鉴权名单同步 |
| `SetChargingProfile` | 设置充电配置 | 智能充电/负载管理 |
| `TriggerMessage` | 触发消息上报 | 主动拉取桩状态 |
| `UnlockConnector` | 解锁连接器 | 远程解锁枪头 |
| `UpdateFirmware` | 固件升级 | 远程 OTA |

---

## 10. 与 NeuronSystem 的结合

### 10.1 当前架构

```
ATP III 充电桩                N3 网关               NeuronSystem 云平台
    |                           |                        |
    | ◄──── PLC 通信 ────►      |                        |
    |                           | ◄──── MQTT ────►       |
    |                           |                        |
                                                         |
其他 OCPP 充电桩 ────── OCPP WebSocket ──────────────►    |
                        (端口 9001)                       |
```

### 10.2 双协议支持

NeuronSystem 需要同时支持两种接入方式：

| 接入方式 | 适用场景 | 模块 |
|---------|---------|------|
| **MQTT** | 自研 N3 网关 + ATP III 桩 | neuron-mqtt（待开发） |
| **OCPP** | 第三方标准充电桩直连 | neuron-protocol（已有） |

### 10.3 OCPP 消息到业务的映射

```
OCPP 消息层                     业务处理层
────────────                    ──────────
BootNotification    ──────►     设备注册/上线
StatusNotification  ──────►     设备状态更新
Heartbeat           ──────►     在线状态维护
Authorize           ──────►     用户认证服务
StartTransaction    ──────►     订单创建
MeterValues         ──────►     充电数据采集 → 实时监控
StopTransaction     ──────►     订单结算 → 计费
DataTransfer        ──────►     厂商扩展处理
```

---

## 附录：参考资料

- [OCPP 1.6 Specification (OCA 官方)](https://openchargealliance.org/)
- [OCPP 1.6 JSON Schema](https://github.com/nicoweidner/ocpp1.6-json-schema)
- [OCPP 协议测试工具](https://github.com/steve-community/steve) — SteVe, 开源 OCPP 后台

---

> 文档版本：v1.0 | 最后更新：2025-03-05 | 维护人：NeuronSystem 开发团队
