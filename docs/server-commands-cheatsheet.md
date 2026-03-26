# 服务器运维常用命令速查

> 服务器：139.199.71.144（Ubuntu）
> 部署目录：/app/neuron

---

## 一、容器管理

```bash
# 查看所有运行中的容器
docker ps

# 查看所有容器（包括停止的）
docker ps -a

# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启某个容器
docker restart neuron-backend

# 停止某个容器
docker stop neuron-backend

# 启动某个容器
docker start neuron-backend

# 删除并重建某个容器（改了 docker-compose.yml 后用）
docker-compose rm -f backend && docker-compose up -d backend
```

---

## 二、查看日志

### Docker 日志（实时）

```bash
# 实时跟踪 backend 日志（Ctrl+C 退出）
docker logs neuron-backend -f

# 只看最后 50 行
docker logs neuron-backend --tail 50

# 实时跟踪 + 只从最后 50 行开始
docker logs neuron-backend --tail 50 -f

# 其他容器日志
docker logs neuron-kafka --tail 50 -f
docker logs neuron-postgres --tail 50 -f
docker logs neuron-redis --tail 50 -f
docker logs neuron-minio --tail 50 -f
```

### 文件日志（持久化在磁盘）

```bash
# 日志目录
ls /data/neuron/logs/

# 实时跟踪主日志
tail -f /data/neuron/logs/neuron.log

# 实时跟踪 OCPP 协议日志
tail -f /data/neuron/logs/neuron-ocpp.log

# 实时跟踪错误日志
tail -f /data/neuron/logs/neuron-error.log

# 看最后 100 行
tail -100 /data/neuron/logs/neuron.log
```

---

## 三、grep 过滤

```bash
# 从 docker 日志中搜关键字
docker logs neuron-backend --tail 500 | grep "BootNotification"

# 从文件日志中搜关键字
grep "ERROR" /data/neuron/logs/neuron.log

# 忽略大小写
grep -i "kafka" /data/neuron/logs/neuron.log

# 显示匹配行的前后 3 行（看上下文）
grep -C 3 "ERROR" /data/neuron/logs/neuron.log

# 只看今天的错误
grep "2026-03-26" /data/neuron/logs/neuron-error.log

# 搜某个设备的日志
grep "9EN03L260326Y4401" /data/neuron/logs/neuron.log

# 搜多个关键字（或）
grep -E "ERROR|WARN" /data/neuron/logs/neuron.log

# 统计错误次数
grep -c "ERROR" /data/neuron/logs/neuron.log
```

---

## 四、Kafka 相关

```bash
# 查看所有 Topic
docker exec neuron-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

# 查看某个 Topic 详情
docker exec neuron-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic device-lifecycle

# 查看消费组状态
docker exec neuron-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

# 查看消费组消费进度（lag = 积压量）
docker exec neuron-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group device-handler

# 实时消费某个 Topic 的消息（调试用，Ctrl+C 退出）
docker exec neuron-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic device-lifecycle --from-beginning
```

---

## 五、数据库

```bash
# 进入 PostgreSQL 命令行
docker exec -it neuron-postgres psql -U postgres -d neuron_cloud

# 直接执行 SQL
docker exec neuron-postgres psql -U postgres -d neuron_cloud -c "SELECT count(*) FROM nc_device;"

# 备份数据库
docker exec neuron-postgres pg_dump -U postgres neuron_cloud > /data/neuron/backup/neuron_cloud_$(date +%Y%m%d).sql
```

---

## 六、Nginx（前端）

```bash
# 查看 Nginx 状态
systemctl status nginx

# 重启 Nginx
systemctl restart nginx

# 重新加载配置（不中断服务）
nginx -s reload

# 测试配置是否正确
nginx -t

# 查看 Nginx 配置
cat /etc/nginx/sites-available/default

# 查看 Nginx 访问日志
tail -f /var/log/nginx/access.log

# 查看 Nginx 错误日志
tail -f /var/log/nginx/error.log

# 搜索 502/504 错误
grep -E "502|504" /var/log/nginx/error.log

# 前端静态文件目录
ls /var/www/neuron-web/

# 更新前端文件（把新的 dist 上传后）
cp -r dist/* /var/www/neuron-web/
```

---

## 七、系统资源

```bash
# 查看容器资源占用（CPU、内存）
docker stats

# 查看磁盘使用
df -h

# 查看 Docker 占用的磁盘空间
docker system df

# 清理无用镜像/容器（释放空间）
docker system prune -f
```
