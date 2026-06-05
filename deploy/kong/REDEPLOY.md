# Kong 全新重部署（丢弃旧配置）

适用于 `~/kong/docker-compose-kong.yml` + 仓库 `kong.yml`，与 `~/services` 下 `backend-*` 同网 `app-network`。

## 0. 准备文件

```bash
mkdir -p ~/kong
cd ~/kong

# 从仓库复制（或 git pull 后拷贝）
cp /path/to/micro-drama/deploy/kong/kong.yml ./kong.yml
cp /path/to/micro-drama/deploy/kong/docker-compose-kong.example.yml ./docker-compose-kong.yml

# JWT 与所有 Java/Go 服务 /config 里 jwt.secret 一致
cat > .env <<'EOF'
JWT_SECRET=在这里填写与线上一致的密钥
EOF
chmod 600 .env
```

确认业务服务已用 **backend-*** 命名且在 **app-network**：

```bash
docker network inspect app-network | grep backend-admin
```

## 1. 停掉并清空旧 Kong（丢弃旧配置）

```bash
cd ~/kong
docker compose -f docker-compose-kong.yml down

# 删除 Kong 数据库卷（旧路由/插件一并清空）
sudo rm -rf ./postgres-kong-data
# 若你原来卷在上一级目录：
# sudo rm -rf ../postgres-kong-data
```

## 2. 启动 Postgres + 迁移 + Kong

```bash
cd ~/kong
docker compose -f docker-compose-kong.yml up -d postgres-kong
sleep 5
docker compose -f docker-compose-kong.yml up -d kong-migrations
docker compose -f docker-compose-kong.yml logs kong-migrations
# 看到 bootstrap finished 或 already bootstrapped（空库应为 bootstrap）

docker compose -f docker-compose-kong.yml up -d kong
docker compose -f docker-compose-kong.yml ps
docker exec kong kong health
```

## 3. 导入 kong.yml

**方式 A — compose 里 deck-sync 服务（推荐）**

`docker-compose-kong.example.yml` 已含 `deck-sync`，首次 up 时执行：

```bash
docker compose -f docker-compose-kong.yml --profile sync run --rm deck-sync
# 或：docker compose -f docker-compose-kong.yml --profile sync up deck-sync && docker logs kong-deck-sync
# 应看到 sync 成功
```

**方式 B — 宿主机 decK**

```bash
deck gateway sync -s ~/kong/kong.yml --kong-addr http://127.0.0.1:3001
```

## 4. 验证

```bash
# 路由列表
curl -s http://127.0.0.1:3001/routes | head -c 2000

# 免登
curl -s -o /dev/null -w "%{http_code}\n" \
  "http://127.0.0.1:3000/admin-api/oauth2/authorize-url"

# 需登录（应 401）
curl -s -o /dev/null -w "%{http_code}\n" \
  "http://127.0.0.1:3000/content-api/actuator/health"
```

## 5. 对外域名

将 Nginx/Caddy 反代到 `127.0.0.1:3000`（Kong Proxy），不要直接把 6001–6007 暴露公网。

## 6. 安全建议

- `3001` Admin、`3002` Manager 不要对公网开放；用 SSH 隧道或内网访问。
- `JWT_SECRET` 只放在 `~/kong/.env`，权限 `600`。
- 修改 `KONG_ADMIN_GUI_API_URL` 为你实际访问 Manager 的地址。

## 7. 以后只改路由

改 `~/kong/kong.yml` 后执行：

```bash
cd ~/kong
docker compose -f docker-compose-kong.yml run --rm deck-sync
# 或 deck gateway sync -s kong.yml --kong-addr http://127.0.0.1:3001
```

无需删库，除非要完全重置。

## 8. 故障排查

### GET 受保护接口 500（JWT pre-function）

`kong.yml` 使用 Kong 内置 `resty.openssl` 做 HS256 校验，**无需** `lua-resty-jwt` 自定义镜像。

在 `kong` 服务环境变量配置：

```yaml
KONG_UNTRUSTED_LUA: "on"
KONG_NGINX_MAIN_ENV: JWT_SECRET
```

> `KONG_UNTRUSTED_LUA_SANDBOX_ENVIRONMENT` **不能**用来开放 `os.getenv`（它只暴露 `kong.request` 等全局对象）。
> strict 沙箱下没有 `os.getenv`，须 `KONG_UNTRUSTED_LUA=on` 并把 `JWT_SECRET` 注入 nginx（`KONG_NGINX_MAIN_ENV`）。

更新 `kong.yml` 后 deck sync，并 `docker compose -f docker-compose-kong.yml up -d kong`。

若仍 500，查看 `docker logs kong`；若 401 检查 `JWT_SECRET` 是否与各服务 `jwt.secret` 一致。

### GET 受保护接口 401（`x-kong-response-latency: 0`）

密钥**字符串相同**仍 401 时，常见原因是 **jjwt 0.9.1 把 `jwt.secret` 当 Base64 解码后再 HMAC**，而 Kong pre-function 用**原始字符串**验签，签名对不上（`reason: sig`）。

**方案 A（推荐）**：重新构建并部署 Java 服务（`JwtTokenService` 已改为 UTF-8 字节密钥，与 Kong / Go 一致），用户需重新登录拿新 token。

**方案 B（暂不重建 Java）**：在 Kong pre-function 里对 `JWT_SECRET` 先 `ngx.decode_base64` 再 HMAC（与当前线上 jjwt 行为一致）。Java 按方案 A 上线后须改回原始字符串验签。
