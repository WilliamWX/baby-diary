# 养娃宝 — 宝宝成长记录与分享社区

> 本项目所有代码由 **AI（Claude Code）** 生成，从零开始全自动编写完成。

## 项目简介

养娃宝是一个面向新手父母的宝宝成长记录与育儿交流平台，支持日记记录、成长追踪、社区分享、AI 医生等功能。

## 技术栈

| 层面 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router + ECharts |
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus + Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| 存储 | MinIO（图片） |
| AI | DeepSeek API |

## 功能清单

- 用户注册/登录（JWT 认证）
- 宝宝档案管理
- 日记记录（图片上传，朋友圈式网格展示，最多 9 张）
- 成长追踪（身高体重、喂养、睡眠、里程碑）
- 经验分享（社区帖子、点赞、评论、收藏）
- 用户关注、头像上传、个性签名
- **AI 医生**（基于 DeepSeek，支持匿名提问）

## 本地运行

### 环境要求

- Java 17+
- Node.js 18+
- MySQL 8.0
- Redis 7
- MinIO

### 启动步骤

```bash
# 1. 启动 MySQL / Redis / MinIO

# 2. 启动后端（默认端口 8080）
cd backend
mvn spring-boot:run -DskipTests

# 3. 启动前端（默认端口 3000）
cd frontend
npm install
npm run dev
```

### 配置

编辑 `backend/src/main/resources/application.yml`：

- 数据库连接信息（MySQL 用户名/密码）
- DeepSeek API Key（AI 医生功能需要）
- MinIO 连接信息

## 项目结构

```
baby-diary/
├── backend/                  # Spring Boot 后端
│   └── src/main/java/com/babydiary/
│       ├── config/           # 安全、CORS、MyBatis 等配置
│       ├── controller/       # REST 控制器
│       ├── service/          # 业务逻辑层
│       ├── mapper/           # MyBatis-Plus 数据访问层
│       ├── entity/           # 数据库实体
│       ├── dto/              # 请求 DTO
│       └── vo/               # 响应 VO
├── frontend/                 # Vue 3 前端
│   └── src/
│       ├── api/              # API 请求层
│       ├── components/       # 公共组件
│       ├── views/            # 页面视图
│       ├── router/           # 路由配置
│       └── stores/           # Pinia 状态管理
└── docker-compose.yml        # Docker 中间件编排
```

## 声明

本项目代码 **100% 由 AI（Claude Code）自动生成**，旨在探索和展示 AI 辅助编程的能力边界。所有功能代码（包括后端 Java、前端 Vue、数据库设计、UI 布局等）均由 AI 根据自然语言指令编写完成，无人工编码介入。

## License

MIT
