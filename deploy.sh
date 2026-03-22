#!/bin/bash
set -e

APP_DIR="/app/neuron"
IMAGE="neuron-backend"

echo "===== NeuronCloud 部署 ====="

cd $APP_DIR

# 创建数据目录
mkdir -p /data/neuron/postgres /data/neuron/redis /data/neuron/minio
mkdir -p /data/neuron/logs /data/neuron/uploads

# 构建镜像
echo "[1/3] 构建镜像..."
docker build -t $IMAGE:latest .

# 停旧容器
echo "[2/3] 停止旧容器..."
docker-compose down --remove-orphans 2>/dev/null || true

# 启动
echo "[3/3] 启动服务..."
docker-compose up -d

echo "等待服务就绪..."
sleep 15
docker-compose ps

echo "===== 部署完成 ====="
echo "API:  http://$(curl -s ifconfig.me):8080/api"
echo "OCPP: ws://$(curl -s ifconfig.me):9001/ocpp"
echo "MinIO: http://$(curl -s ifconfig.me):9002"
