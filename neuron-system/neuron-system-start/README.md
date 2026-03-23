# NeuronCloud 启动模块

本模块是 NeuronCloud（N3 Lite 智能充电云平台）的应用启动入口。

## 启动信息

- **启动类：** `com.echarge.NeuronSystemApplication`
- **端口：** 8080
- **API 路径：** http://localhost:8080/api
- **OCPP WebSocket：** ws://localhost:9001/ocpp/{chargePointId}
- **默认账号：** admin / 123456

## 配置文件

| 文件 | 用途 |
|------|------|
| `application.yml` | 公共配置（应用名） |
| `application-dev.yml` | 开发环境（本地数据库/Redis/MinIO） |
| `application-prod.yml` | 生产环境 |
| `application-postgresql.yml` | PostgreSQL 专用配置 |

## 依赖模块

```
neuron-system-start
├── neuron-system-impl   (用户管理、登录)
├── neuron-device        (设备管理、固件升级、操作日志)
├── neuron-alert         (告警记录)
└── neuron-protocol      (OCPP 协议网关)
```

## 运行方式

**IDEA 启动：** 运行 `NeuronSystemApplication.main()`

**Maven 打包：**
```bash
mvn clean package -DskipTests
java -jar target/neuron-system-start-1.0.0.jar
```

**Docker 部署：** 使用根目录的 `Dockerfile` 和 `docker-compose.yml`
