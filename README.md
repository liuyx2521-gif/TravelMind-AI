# TravelMind AI

AI 个性化旅游推荐与智能规划平台。

这是一套“功能完整但代码尽量简单”的可运行版本：不做复杂 DDD，不堆 DTO/VO/Converter，CRUD 优先使用 MyBatis Plus，只有认证、AI、上传这些必要场景写少量业务代码。

## 功能

- 用户注册、登录、JWT 认证、个人中心、头像上传
- 景点列表、搜索、热门、地图定位、收藏、浏览历史
- 打卡点查询
- 酒店列表、价格/评分排序、收藏
- 游记发布、点赞、评论
- 收藏管理
- 浏览历史
- 行程创建、编辑、删除、列表、AI 生成
- AI 旅游顾问
- AI 行程规划
- AI 预算分析
- AI 酒店推荐
- AI 景点推荐
- 免费本地 AI：Ollama + `qwen2.5:7b-instruct`
- 可选 DeepSeek API
- MinIO 文件上传
- Vue3 + Naive UI + UnoCSS + ECharts + 高德地图
- Docker Compose 部署

## 目录

```text
backend   Spring Boot 3.3 + Java 21
frontend  Vue3 + TypeScript + Vite
deploy    MySQL/Redis/MinIO/Nginx/Docker Compose
docs      完整架构设计文档
```

## 本地启动

1. 启动基础设施：

```bash
cd deploy
docker compose up -d mysql redis minio
```

2. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

3. 启动前端：

```bash
cd frontend
npm install
npm run dev
```

4. 访问：

```text
前端：http://localhost:8080
后端 API：http://localhost:8081
接口文档：http://localhost:8081/doc.html
MinIO：http://localhost:9001
```

## 免费 AI 接入

默认使用本机 Ollama，开发阶段不需要 DeepSeek Key，不需要充值。

1. 安装 Ollama：

访问 [https://ollama.com/download](https://ollama.com/download) 下载 Windows 版本并安装。

2. 拉取免费模型：

```bash
ollama pull qwen2.5:7b-instruct
```

3. 确认 Ollama 正在运行：

```bash
ollama list
```

能看到 `qwen2.5:7b-instruct` 就可以。

4. 启动后端后访问：

```text
http://localhost:8081/api/ai/config
```

返回里如果是下面这样，说明当前走的是免费本地模型：

```json
{
  "provider": "ollama",
  "baseUrl": "http://localhost:11434",
  "model": "qwen3.5:0.8b",
  "freeByDefault": true
}
```

如果电脑内存较小，可以改成更轻的模型：

```bash
ollama pull qwen2.5:3b-instruct
```

然后把环境变量 `AI_MODEL` 改成：

```env
AI_MODEL=qwen2.5:3b-instruct
```

## 可选：切换 DeepSeek API

DeepSeek API 是按 token 计费的，不是免费接口。以后要切换时设置：

```env
AI_PROVIDER=deepseek
AI_BASE_URL=https://api.deepseek.com
AI_API_KEY=你的 DeepSeek Key
AI_MODEL=deepseek-chat
```

## 环境变量

```env
AI_PROVIDER=ollama
AI_BASE_URL=http://localhost:11434
AI_API_KEY=local
AI_MODEL=qwen3.5:0.8b
JWT_SECRET=travelmind-ai-jwt-secret-key-32bytes!!
MYSQL_PASSWORD=123456
VITE_AMAP_KEY=你的高德 Web Key
VITE_AMAP_SECURITY_CODE=你的高德安全密钥
```

## 构建验证

后端：

```bash
cd backend
mvn -DskipTests package
```

前端：

```bash
cd frontend
npm run build
```

`npm run typecheck` 是可选项。部分 UI/图表库类型声明可能需要额外安装 `katex` 类型依赖，运行构建不受影响。
