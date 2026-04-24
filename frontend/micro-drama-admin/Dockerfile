# 依赖 CI/本地已生成的 dist（与 workflow 中 pnpm build 配合）
FROM nginx:1.25-alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
