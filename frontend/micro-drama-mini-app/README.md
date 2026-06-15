# micro-drama-mini-app

微短剧用户端：Next.js App Router，面向 **Telegram Mini App** 与 **可被 Google 索引的短剧详情页**。

## 功能

| Tab | 路由 | 说明 |
| --- | --- | --- |
| 剧集 | `/dramas` | 短剧列表、搜索 |
| 播放 | `/play` | 竖滑播放已解锁剧集 |
| 我的 | `/profile` | 头像、余额、支付/观看记录 |

- 短剧详情 `/dramas/[id]`：SSR + JSON-LD + Open Graph，利于 SEO
- 解锁支付：余额 / Web2（占位）/ Web3（`backend-java/micro-drama-payment` → gRPC `backend-go/micro-drama-chain`）
- 播放鉴权：对接 `backend-go/micro-drama-video` `/v1/video/play`
- 内容数据：对接 `backend-java/micro-drama-content` `/microDramas/*`

## 后端依赖

| 服务 | 默认端口 | 环境变量 |
| --- | --- | --- |
| user-api | 6003 | `USER_API_TARGET` / 生产默认 `https://api.dramadjbo.com/user-api` |
| content-api | 6002 | `CONTENT_API_TARGET` / 生产默认 `https://api.dramadjbo.com/content-api` |
| video-api | 8080 | `VIDEO_API_TARGET` |
| payment-api | 8081 | `PAYMENT_API_TARGET` |

content-api 需 JWT。在 Telegram 小程序内打开时会自动调用 `/auth/telegram` 登录并签发 token。

## 快速开始

```bash
cd frontend/micro-drama-mini-app
cp .env.example .env.local
npm install
npm run dev
```

访问 http://localhost:3000

## Telegram Mini App

1. 在 BotFather 创建 Bot 并配置 Web App URL（指向部署域名）
2. 后端 `micro-drama-user` 配置 `TELEGRAM_BOT_TOKEN`（与 Bot 一致）
3. 页面加载 `telegram-web-app.js`，启动时自动用 `initData` 调用 `/auth/telegram` 登录
4. 生产环境设置 `NEXT_PUBLIC_SITE_URL` 为公网 HTTPS 域名

## SEO

- `/dramas/[id]` 服务端渲染 + `generateMetadata`
- `/sitemap.xml`、`/robots.txt` 自动生成
- 设置 `NEXT_PUBLIC_SITE_URL=https://your-domain.com`

## 目录结构

```
src/
├── app/(main)/          # 三 Tab 主界面
├── components/          # UI 组件
├── lib/api/             # content / video / payment / user
├── lib/stores/          # Zustand 状态
└── lib/telegram/        # Telegram WebApp 适配
```

## 待对接（user 服务）

个人中心余额、支付记录、观看记录当前部分使用 localStorage，登录与用户资料已对接 `backend-java/micro-drama-user`。
