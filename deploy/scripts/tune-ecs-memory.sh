#!/bin/bash
# 2核8G ECS 内存调优 — 个人项目
# 用法: sudo bash tune-ecs-memory.sh
set -euo pipefail

SERVICES_DIR="${SERVICES_DIR:-$HOME/services}"
TRAEFIK_DIR="${TRAEFIK_DIR:-$HOME/traefik}"
SWAP_SIZE="${SWAP_SIZE:-2G}"

echo "=== [1/6] 备份现有配置 ==="
TS=$(date +%Y%m%d_%H%M%S)
mkdir -p "$SERVICES_DIR/backup_$TS"
[[ -f "$SERVICES_DIR/docker-compose-services.yml" ]] && \
  cp "$SERVICES_DIR/docker-compose-services.yml" "$SERVICES_DIR/backup_$TS/"
[[ -f "$TRAEFIK_DIR/docker-compose-traefik.yml" ]] && \
  cp "$TRAEFIK_DIR/docker-compose-traefik.yml" "$TRAEFIK_DIR/backup_$TS/" 2>/dev/null || true

echo "=== [2/6] 添加 Swap（当前无 Swap，防 OOM）==="
if [[ $(swapon --show | wc -l) -eq 0 ]]; then
  fallocate -l "$SWAP_SIZE" /swapfile 2>/dev/null || dd if=/dev/zero of=/swapfile bs=1M count=2048 status=progress
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
  sysctl -w vm.swappiness=10
  grep -q 'vm.swappiness' /etc/sysctl.conf || echo 'vm.swappiness=10' >> /etc/sysctl.conf
  echo "Swap 已启用: $(swapon --show)"
else
  echo "Swap 已存在，跳过"
fi

echo "=== [3/6] 读取现有 JWT_SECRET ==="
JWT_SECRET=""
if docker inspect gateway-auth &>/dev/null; then
  JWT_SECRET=$(docker inspect gateway-auth --format '{{range .Config.Env}}{{println .}}{{end}}' \
    | grep '^JWT_SECRET=' | cut -d= -f2- || true)
fi
if [[ -z "$JWT_SECRET" && -f "$SERVICES_DIR/config/gateway-auth/application.yml" ]]; then
  JWT_SECRET=$(grep 'secret:' "$SERVICES_DIR/config/gateway-auth/application.yml" | head -1 | sed 's/.*secret:[[:space:]]*//' | tr -d '"' || true)
fi
if [[ -z "$JWT_SECRET" || "$JWT_SECRET" == *'替换'* || "$JWT_SECRET" == *'CHANGE'* ]]; then
  JWT_SECRET="dev-jwt-secret-change-me"
  echo "警告: 未读到有效 JWT_SECRET，使用占位符，请事后修改 compose 并重启 gateway-auth"
fi

echo "=== [4/6] 写入业务 compose（mem_limit + Java 堆）==="
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
      - JAVA_TOOL_OPTIONS=-Xms96m -Xmx280m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=postgres
      - DB_USER=postgres
      - DB_PASSWORD=djbo1616
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=djbo1616
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
      - JAVA_TOOL_OPTIONS=-Xms96m -Xmx300m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=postgres
      - DB_USER=postgres
      - DB_PASSWORD=djbo1616
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=djbo1616
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
      - JAVA_TOOL_OPTIONS=-Xms96m -Xmx300m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=postgres
      - DB_USER=postgres
      - DB_PASSWORD=djbo1616
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=djbo1616
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
      - JAVA_TOOL_OPTIONS=-Xms96m -Xmx300m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=postgres
      - DB_USER=postgres
      - DB_PASSWORD=djbo1616
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=djbo1616
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
      - JAVA_TOOL_OPTIONS=-Xms64m -Xmx240m -XX:+UseG1GC -XX:MaxMetaspaceSize=96m
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=djbo1616
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

echo "=== [5/6] 限制基础设施容器（Nexus/Kafka/DB 是大户）==="
limit_container() {
  local name="$1" mem="$2"
  if docker inspect "$name" &>/dev/null; then
    docker update --memory="$mem" --memory-swap="$mem" "$name"
    echo "  $name -> $mem"
  else
    echo "  $name 不存在，跳过"
  fi
}

# Nexus 1.3G -> 768M（仍够拉镜像）
limit_container nexus 768m
# Kafka 1.2G -> 512M（视频转码消息够用）
limit_container kafka-server 512m
limit_container postgres 256m
limit_container redis 96m
limit_container nginx 64m
limit_container deploy-webhook 64m

# Kafka JVM 堆（需重建才生效；尝试 docker update 不够时提示）
if docker inspect kafka-server &>/dev/null; then
  KAFKA_ENV=$(docker inspect kafka-server --format '{{range .Config.Env}}{{println .}}{{end}}' | grep KAFKA_HEAP_OPTS || true)
  if [[ -z "$KAFKA_ENV" ]]; then
    echo ""
    echo "  [提示] Kafka 未设 KAFKA_HEAP_OPTS，建议在 Kafka compose 中加:"
    echo "    KAFKA_HEAP_OPTS: \"-Xmx384m -Xms192m\""
    echo "    然后 docker compose up -d kafka-server"
  fi
fi

echo "=== [6/6] 重启业务 + 部署 Traefik ==="
cd "$SERVICES_DIR"
docker compose up -d

mkdir -p "$TRAEFIK_DIR/dynamic"
if [[ ! -f "$TRAEFIK_DIR/traefik.yml" ]]; then
  cat > "$TRAEFIK_DIR/traefik.yml" <<'TRAEFIKEOF'
api:
  dashboard: true
  insecure: true
entryPoints:
  web:
    address: ":8000"
providers:
  file:
    directory: /etc/traefik/dynamic
    watch: true
log:
  level: INFO
TRAEFIKEOF
fi

if [[ ! -f "$TRAEFIK_DIR/dynamic/routes.yml" ]]; then
  echo "  警告: $TRAEFIK_DIR/dynamic/routes.yml 不存在，请从仓库 deploy/traefik/dynamic/routes.yml 复制"
fi

cat > "$TRAEFIK_DIR/docker-compose-traefik.yml" <<'TRAEFIKEOF'
version: '3.8'
networks:
  default:
    name: app-network
    external: true
services:
  traefik:
    image: traefik:v3.3
    container_name: traefik
    mem_limit: 128m
    memswap_limit: 128m
    ports:
      - "3000:8000"
      - "127.0.0.1:3080:8080"
    volumes:
      - ./traefik.yml:/etc/traefik/traefik.yml:ro
      - ./dynamic:/etc/traefik/dynamic:ro
    restart: unless-stopped
TRAEFIKEOF

if [[ -f "$TRAEFIK_DIR/dynamic/routes.yml" ]]; then
  cd "$TRAEFIK_DIR"
  docker compose -f docker-compose-traefik.yml up -d
  echo "Traefik 已启动"
else
  echo "Traefik 配置未齐，跳过启动（复制 routes.yml 后执行: cd ~/traefik && docker compose -f docker-compose-traefik.yml up -d）"
fi

sleep 8
echo ""
echo "========== 调优完成，当前内存 =========="
free -h
echo ""
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}" | sort -k3 -hr
echo ""
echo "验证命令:"
echo "  curl -s http://127.0.0.1:6010/actuator/health"
echo "  curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:3000/payment-api/healthz"
echo ""
echo "若某 Java 服务频繁重启(OOM): docker inspect <容器> --format '{{.State.OOMKilled}}'"
echo "  可适当加大该服务 mem_limit 和 -Xmx（每次 +64m）"
