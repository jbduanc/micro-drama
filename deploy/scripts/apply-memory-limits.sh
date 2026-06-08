#!/bin/bash
# 个人项目 2核8G — 内存限制脚本
# 用法: bash apply-memory-limits.sh
# 可先 vim 修改下方变量，再执行
set -euo pipefail

SERVICES_DIR="${SERVICES_DIR:-$HOME/services}"

# ========== 可按需修改 ==========
JWT_SECRET="${JWT_SECRET:-abc22aahrfbcmdda2gg6ee9056fdhjgh}"
DB_PASSWORD="${DB_PASSWORD:-djbo1616}"
REDIS_PASSWORD="${REDIS_PASSWORD:-djbo1616}"

# Java 堆（个人项目推荐值，OOM 时单次 +64m）
JAVA_ADMIN_XMX="${JAVA_ADMIN_XMX:-280m}"
JAVA_CONTENT_XMX="${JAVA_CONTENT_XMX:-300m}"
JAVA_USER_XMX="${JAVA_USER_XMX:-300m}"
JAVA_PAYMENT_XMX="${JAVA_PAYMENT_XMX:-300m}"
JAVA_GATEWAY_XMX="${JAVA_GATEWAY_XMX:-240m}"

# 基础设施上限（docker update，立即生效）
NEXUS_MEM="${NEXUS_MEM:-768m}"
KAFKA_MEM="${KAFKA_MEM:-512m}"
POSTGRES_MEM="${POSTGRES_MEM:-256m}"
REDIS_MEM="${REDIS_MEM:-96m}"
# ================================

echo ">>> 备份 compose"
TS=$(date +%Y%m%d_%H%M%S)
cp "$SERVICES_DIR/docker-compose-services.yml" \
  "$SERVICES_DIR/docker-compose-services.yml.bak.$TS"

echo ">>> 写入带内存限制的业务 compose"
cat > "$SERVICES_DIR/docker-compose-services.yml" <<EOF
version: '3.8'

networks:
  default:
    name: app-network
    external: true

services:
  backend-admin:
    image: 127.0.0.1:8082/backend-micro-drama-admin:latest
    container_name: backend-admin
    mem_limit: 384m
    memswap_limit: 384m
    volumes:
      - ./config/backend-admin:/config:ro
    environment:
      - JAVA_TOOL_OPTIONS=-Xms96m -Xmx${JAVA_ADMIN_XMX} -XX:+UseG1GC -XX:MaxMetaspaceSize=128m
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=postgres
      - DB_USER=postgres
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/application.yml
    ports:
      - "6001:6001"
    restart: unless-stopped

  backend-content:
    image: 127.0.0.1:8082/backend-micro-drama-content:latest
    container_name: backend-content
    mem_limit: 400m
    memswap_limit: 400m
    volumes:
      - ./config/backend-content:/config:ro
    environment:
      - JAVA_TOOL_OPTIONS=-Xms96m -Xmx${JAVA_CONTENT_XMX} -XX:+UseG1GC -XX:MaxMetaspaceSize=128m
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=postgres
      - DB_USER=postgres
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/application.yml
    ports:
      - "6002:6002"
    restart: unless-stopped

  backend-user:
    image: 127.0.0.1:8082/backend-micro-drama-user:latest
    container_name: backend-user
    mem_limit: 400m
    memswap_limit: 400m
    volumes:
      - ./config/backend-user:/config:ro
    environment:
      - JAVA_TOOL_OPTIONS=-Xms96m -Xmx${JAVA_USER_XMX} -XX:+UseG1GC -XX:MaxMetaspaceSize=128m
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=postgres
      - DB_USER=postgres
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/application.yml
    ports:
      - "6003:6003"
    restart: unless-stopped

  backend-payment:
    image: 127.0.0.1:8082/backend-micro-drama-payment:latest
    container_name: backend-payment
    mem_limit: 400m
    memswap_limit: 400m
    volumes:
      - ./config/backend-payment:/config:ro
    environment:
      - JAVA_TOOL_OPTIONS=-Xms96m -Xmx${JAVA_PAYMENT_XMX} -XX:+UseG1GC -XX:MaxMetaspaceSize=128m
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=postgres
      - DB_USER=postgres
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - CHAIN_GRPC_ADDRESS=static://backend-chain:9092
      - SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/application.yml
    ports:
      - "6004:6004"
    restart: unless-stopped
    depends_on:
      - backend-chain

  backend-video:
    image: 127.0.0.1:8082/backend-micro-drama-video:latest
    container_name: backend-video
    mem_limit: 128m
    memswap_limit: 128m
    volumes:
      - ./config/backend-video:/config:ro
    environment:
      - CONFIG_FILE=/config/application.yml
    ports:
      - "6005:6005"
    restart: unless-stopped

  backend-transcoder:
    image: 127.0.0.1:8082/backend-micro-drama-transcoder:latest
    container_name: backend-transcoder
    mem_limit: 256m
    memswap_limit: 256m
    volumes:
      - ./config/backend-transcoder:/config:ro
    environment:
      - CONFIG_FILE=/config/application.yml
    ports:
      - "6006:6006"
    restart: unless-stopped

  backend-chain:
    image: 127.0.0.1:8082/backend-micro-drama-chain:latest
    container_name: backend-chain
    mem_limit: 64m
    memswap_limit: 64m
    volumes:
      - ./config/backend-chain:/config:ro
    environment:
      - CONFIG_FILE=/config/application.yml
    ports:
      - "6007:9092"
    restart: unless-stopped

  gateway-auth:
    image: 127.0.0.1:8082/backend-micro-drama-gateway-auth:latest
    container_name: gateway-auth
    mem_limit: 320m
    memswap_limit: 320m
    volumes:
      - ./config/gateway-auth:/config:ro
    environment:
      - JAVA_TOOL_OPTIONS=-Xms64m -Xmx${JAVA_GATEWAY_XMX} -XX:+UseG1GC -XX:MaxMetaspaceSize=96m
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - AUTH_COOKIE_DOMAIN=.dramadjbo.com
      - AUTH_COOKIE_SECURE=true
      - SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/application.yml
    ports:
      - "6010:6010"
    restart: unless-stopped

  frontend-admin:
    image: 127.0.0.1:8082/frontend-micro-drama-admin:latest
    container_name: frontend-admin
    mem_limit: 64m
    memswap_limit: 64m
    restart: unless-stopped

  frontend-mini-app:
    image: 127.0.0.1:8082/frontend-micro-drama-mini-app:latest
    container_name: frontend-mini-app
    mem_limit: 64m
    memswap_limit: 64m
    restart: unless-stopped
EOF

echo ">>> 限制基础设施容器（无需改它们 compose 文件）"
limit_one() {
  local name="$1" mem="$2"
  if docker inspect "$name" &>/dev/null; then
    docker update --memory="$mem" --memory-swap="$mem" "$name"
    echo "  OK  $name -> $mem"
  else
    echo "  SKIP $name 不存在"
  fi
}
limit_one nexus "$NEXUS_MEM"
limit_one kafka-server "$KAFKA_MEM"
limit_one postgres "$POSTGRES_MEM"
limit_one redis "$REDIS_MEM"
limit_one nginx 64m
limit_one deploy-webhook 64m

echo ""
echo ">>> 完成。请执行以下命令重启业务："
echo "  cd $SERVICES_DIR && docker compose up -d"
echo ""
echo ">>> 验证："
echo "  free -h"
echo "  docker stats --no-stream --format 'table {{.Name}}\t{{.MemUsage}}' | sort -k2 -hr"
