# 养娃宝 — 记录 · 分享 · 成长

<p align="center">
  <img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square" alt="PRs Welcome">
</p>

## 来自一位程序员奶爸的话

宝宝的出生，让我第一次真正理解了什么叫"捧在手心怕碎了，含在嘴里怕化了"。

从第一次抱起那个小小软软的身体，到看着他第一次翻身、第一次爬行、第一次踉踉跄跄朝我走来……每一个瞬间都珍贵到舍不得遗忘。作为一个程序员，我习惯了用代码解决生活中的问题，于是就有了 **养娃宝** —— 一个专为记录宝宝成长而生的社区。

这里面没有商业考量，没有产品规划，有的只是一个新手爸爸对宝宝**无限的爱意**，以及想要把这份喜悦分享给全世界的冲动。

> 代码会老去，框架会过时，但那些关于宝宝的记忆，值得被温柔地珍藏。


## 功能概览

| 模块 | 说明 |
|------|------|
| 日记记录 | 图文记录宝宝日常，最多 9 张照片，朋友圈式网格展示 |
| 成长追踪 | 身高体重曲线、喂养记录、睡眠记录、成长里程碑 |
| 经验分享 | 社区帖子，支持点赞、评论、收藏、关注 |
| 宝宝档案 | 多宝宝管理，头像、生日、性别等信息 |
| AI 医生 | 基于 DeepSeek，支持公开/匿名提问，儿科育儿问题随时问 |
| 个人中心 | 头像上传、个性签名、数据统计 |

## 技术栈

| 层面 | 技术选型 |
|------|----------|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router + ECharts |
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus + Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| 存储 | MinIO |
| AI | DeepSeek API |

## 快速开始

### 环境准备

- Java 17+
- Node.js 18+
- MySQL 8.0
- Redis 7
- MinIO

### 启动

```bash
# 1. 确保 MySQL / Redis / MinIO 已运行

# 2. 启动后端（端口 8080）
cd backend
mvn spring-boot:run -DskipTests

# 3. 启动前端（端口 3000）
cd frontend
npm install
npm run dev
```

### 配置项

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: root
    password: <你的MySQL密码>

deepseek:
  api-key: <你的DeepSeek API Key>

minio:
  access-key: minioadmin
  secret-key: minioadmin
```

## 项目结构

```
baby-diary/
├── backend/src/main/java/com/babydiary/
│   ├── config/        # Spring Security、CORS、MyBatis-Plus 配置
│   ├── controller/    # REST 接口层
│   ├── service/       # 业务逻辑层（含 DeepSeek AI 服务）
│   ├── mapper/        # MyBatis-Plus 数据访问层
│   ├── entity/        # 数据库实体
│   ├── dto/           # 请求参数对象
│   └── vo/            # 响应数据对象
├── frontend/src/
│   ├── api/           # 后端接口封装
│   ├── components/    # 公共组件（Logo、布局）
│   ├── views/         # 页面（登录、日记、AI 医生等）
│   ├── router/        # 前端路由
│   └── stores/        # Pinia 状态管理
└── docker-compose.yml # Docker 中间件编排
```

## 一起完善它

养娃宝还远远不够完美——功能有待丰富，UI 有待打磨，代码有待优化。如果你也是一位宝爸宝妈程序员，或者对这个项目感兴趣，欢迎：

- ⭐ Star 项目，让更多父母看到它
- 🐛 提交 Issue，反馈 Bug 或提出建议
- 🎨 提交 PR，一起完善功能

无论是一行代码的修正，还是一个模块的重构，每一份贡献都意味着有更多的宝宝成长瞬间被更好地记录下来。

> **让代码有温度，让成长被看见。**

## License

MIT © WilliamWX
