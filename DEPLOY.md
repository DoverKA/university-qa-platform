# 部署说明

## 1. AI 配置

当前项目已切换为 DeepSeek OpenAI 兼容接口，默认配置如下：

- API 地址：`https://api.deepseek.com/chat/completions`
- 默认模型：`deepseek-chat`
- 可选模型：`deepseek-reasoner`

生产环境至少需要设置：

- `AI_API_KEY`
- `JWT_SECRET`
- 数据库连接相关环境变量

## 2. 服务器部署方式

推荐使用 Docker Compose：

```bash
docker compose up -d --build
```

应用启动后默认端口：

- Web/API: `8080`
- MySQL: `3306`

## 3. 首次上线前需要修改

请至少替换以下占位值：

- `compose.yaml` 中的数据库密码
- `compose.yaml` 中的 `JWT_SECRET`
- `compose.yaml` 中的 `AI_API_KEY`

## 4. 如果服务器已安装 MySQL

可以只启动应用容器，并将以下变量改为现有数据库：

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## 5. 建议

- 生产环境建议配一个反向代理（Nginx 或 Caddy）
- 建议绑定域名并开启 HTTPS
- 上线后建议限制数据库对公网开放
