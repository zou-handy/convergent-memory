# Convergent Memory v0.2

> 跨 AI 共享的个人长期记忆 REST API · 云便签式体验 · Markdown 真身 + DB 索引

## 项目简介

所有 AI 助手（ChatGPT、Claude、豆包等）都会失忆：开新对话就忘了上次。
本项目做一个**云端记忆库 REST API**，所有 AI 都能往里读写，从此它们记住的是**同一个我**。

### 核心设计

1. **Markdown 是真身，数据库是索引** — 即使 API 挂了，用 Obsidian 直接看记忆
2. **三层记忆模型** — INBOX（原始散记）→ CONTEXT（主题聚合）→ CORE（高频核心）
3. **两阶段写**（preview + apply）— AI 出草案，人确认才落盘，agent 不能偷偷改记忆
4. **多用户隔离** — 每人一个 token，各看各的记忆，互不干扰
5. **云便签式体验** — 一个 URL 就是一个记忆库入口，AI 一行 GET 创建账号

## 技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.2.5 |
| ORM | Spring Data JPA + Hibernate | 6.4.4 |
| 安全 | Spring Security（Bearer Token + Session） | 6.2.4 |
| 数据库 | H2 Database（嵌入式） | 2.2.224 |
| AI Agent | AgentScope Java SDK | 2.0.0-RC4 |
| API 文档 | SpringDoc OpenAPI（Swagger UI） | 2.5.0 |
| 反向代理 | Nginx | 1.18 |
| 部署 | DigitalOcean Droplet（Ubuntu, nyc1） | - |

## 目录结构

```
convergent-memory/
├── pom.xml                          # Maven 项目配置
└── src/main/
    ├── java/com/convergentmemory/
    │   ├── ConvergentMemoryApplication.java    # 启动类
    │   ├── agent/
    │   │   └── ConvergerAgent.java             # 收敛 Agent（规则版 + LLM 版）
    │   ├── config/
    │   │   ├── ApiKeyAuthFilter.java           # Bearer Token 鉴权过滤器
    │   │   ├── SecurityConfig.java             # Spring Security 配置
    │   │   ├── JacksonConfig.java              # JSON 中文不转义
    │   │   └── OpenApiConfig.java              # Swagger 配置
    │   ├── controller/
    │   │   ├── AuthController.java             # 注册/登录/快速创建
    │   │   ├── ConvergeController.java         # 收敛预览/应用
    │   │   ├── MemoryController.java           # 记忆 CRUD + 搜索
    │   │   └── PublicController.java           # 主页/用户页/Agent手册
    │   ├── dto/                                # 数据传输对象
    │   ├── entity/                             # JPA 实体（4 张表）
    │   ├── repository/                         # 数据访问层
    │   └── service/                            # 业务逻辑层
    └── resources/
        └── application.yml                     # 配置文件
```

## 运行方式

### 环境要求
- JDK 17+
- Maven 3.6+

### 本地运行
```bash
mvn spring-boot:run
```
默认启动在 http://localhost:8081

### 线上地址
- 主页：http://134.209.66.112/
- API 文档：http://134.209.66.112/swagger-ui.html
- 健康检查：http://134.209.66.112/api/memory/health

## API 接口

| 方法 | 路径 | 鉴权 | 功能 |
|---|---|---|---|
| GET | `/api/auth/quick/{username}` | 无 | 一行创建账号（返回链接+Token） |
| POST | `/api/auth/register` | 无 | 注册 |
| POST | `/api/auth/login` | 无 | 登录 |
| POST | `/api/memory/add` | Bearer | 写一条记忆 |
| GET | `/api/memory/search?q=` | Bearer | 搜索记忆 |
| POST | `/api/converge/preview` | Bearer | 收敛草案（不落盘） |
| POST | `/api/converge/apply/{id}` | Bearer | 应用草案（落盘） |
| GET | `/u/{username}?token=` | Token | 个人记忆库页面 |
| GET | `/u/{username}/agent-guide?token=` | Token | AI 助手使用手册 |

## 学术参考

- **MRAgent**：三层记忆模型 + cueTags 召回线索
- **Qwen-AgentWorld 1.0**：humanDiff 可控扰动信号

## 开发团队

- [填你的名字] — 编码
- [队友1] — 项目经理
- [队友2] — 测试

## 指导老师

[填老师名字]
