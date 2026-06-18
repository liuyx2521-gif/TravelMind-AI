# TravelMind AI KISS 实现说明

## 原则

最简单可运行版本优先，功能不缩水，代码不堆层。

- CRUD 直接使用 MyBatis Plus Mapper。
- 不为每个接口机械生成 DTO、VO、Converter。
- Controller 可以承载少量清晰业务逻辑。
- 只有 AI、JWT、上传这类独立能力单独成类。
- Java 21 优先使用 record、var、Stream、文本块。
- 可选优化写在后面，不阻塞 MVP。

## 当前实现

后端核心包：

```text
com.travelmind
├── ai          DeepSeek 调用与 Prompt
├── common      Result、分页、异常处理
├── config      MyBatis Plus 分页
├── controller  所有 REST API
├── mapper      MyBatis Plus BaseMapper
├── model       数据库实体
└── security    JWT、Spring Security、当前用户
```

前端核心目录：

```text
src
├── api.ts
├── router.ts
├── stores
├── components/AmapView.vue
└── views
```

## API

认证：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `PUT /api/users/me`

景点：

- `GET /api/attractions`
- `GET /api/attractions/{id}`
- `GET /api/attractions/hot`
- `GET /api/check-points?attractionId=1`

酒店：

- `GET /api/hotels?city=三亚&sort=price`

游记：

- `GET /api/notes`
- `GET /api/notes/{id}`
- `POST /api/notes`
- `POST /api/notes/{id}/like`
- `GET /api/notes/{id}/comments`
- `POST /api/notes/{id}/comments`

收藏和历史：

- `GET /api/favorites`
- `POST /api/favorites`
- `DELETE /api/favorites`
- `GET /api/history`
- `POST /api/history`
- `DELETE /api/history`

行程：

- `GET /api/plans`
- `GET /api/plans/{id}`
- `POST /api/plans`
- `PUT /api/plans/{id}`
- `DELETE /api/plans/{id}`
- `POST /api/plans/generate`

AI：

- `POST /api/ai/chat`
- `POST /api/ai/chat/stream`
- `POST /api/ai/plan`
- `POST /api/ai/budget`
- `POST /api/ai/hotels`
- `POST /api/ai/attractions`
- `GET /api/ai/conversations`
- `GET /api/ai/conversations/{id}/messages`

文件：

- `POST /api/files/upload`

## 可选优化

- 用户返回对象可改成轻量 `UserProfile` record，避免手动 `password=null`。
- AI SSE 可解析 DeepSeek data chunk，而不是把原始 chunk 直接返回。
- 收藏列表可联表返回标题和封面。
- 游记可增加图片表，支持多图。
- 高德地图可增加路线规划。
- Redis 可加入热门景点、热门游记缓存。
- 生产环境关闭公开 Knife4j 或增加 Basic Auth。
