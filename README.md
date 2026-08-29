# Convergent Memory v0.2 · Java Server

> 跨 AI 共享的个人长期记忆 REST API · Spring Boot · Markdown 真身 + H2 索引

> [!IMPORTANT]
> **当前状态：源码已保存，原部署已离线。** 这个分支是曾经成功运行、部署到 DigitalOcean，并在 Java 课程中完成演示的服务端版本。原服务器已经过期，README 中过去使用的公网 IP 不再作为可用演示地址。仓库保存了完整 Java 源码，但不包含服务器本地的 H2 数据库文件和 `/root/memory-vault` 运行数据。

## 项目结论

源码没有丢失。本分支保留：

- `pom.xml`
- 31 个 Java 源文件
- Spring Boot 启动类
- 用户注册、登录和 Token 鉴权
- 记忆 CRUD 与搜索接口
- 收敛 preview / apply 流程
- JPA 实体与 Repository
- H2 数据库配置
- Swagger / OpenAPI 配置
- AgentScope Java SDK 接入
- 面向 AI 助手的使用说明页面

丢失风险主要在**旧服务器运行数据**，而不是项目代码。原配置把数据写在：

```text
/root/convergent-memory/data/index
/root/memory-vault
```

如果 DigitalOcean 没有保留 Snapshot、Volume 或单独备份，原来的用户、Token、记忆索引和 Markdown 记忆内容应视为不可恢复。

## 项目简介

不同 AI 助手通常各自保存上下文，无法自然共享同一份长期记忆。本项目提供一个多用户 REST API，让 ChatGPT、Claude、豆包或其他 Agent 通过同一组接口读写一个可人工查看的记忆库。

核心设计：

1. **Markdown 是真身，数据库是索引**：API 不可用时，记忆仍可作为普通文件阅读。
2. **三层记忆模型**：INBOX（原始散记）→ CONTEXT（主题聚合）→ CORE（高频核心）。
3. **两阶段写入**：先 preview，再由用户确认 apply，避免 Agent 静默改写长期记忆。
4. **多用户隔离**：每个用户拥有独立 Token 与记忆空间。
5. **云便签式入口**：通过 URL 和 REST API 为 AI 助手提供统一读写方式。

## 曾经验证过的内容

- Spring Boot 服务能够启动并对外提供 HTTP 接口。
- Nginx 反向代理到 Java 服务。
- 多用户注册、登录和 Bearer Token 鉴权。
- 记忆新增、读取、搜索和收敛流程。
- H2 持久化索引与 Markdown 文件写入。
- Swagger UI 接口说明。
- DigitalOcean 线上部署。
- Java 课程现场演示。

原部署地址曾为 `134.209.66.112`，现在已经离线，不应继续作为可访问链接展示。

## 技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.2.5 |
| ORM | Spring Data JPA + Hibernate | 6.4.4 |
| 安全 | Spring Security · Bearer Token · BCrypt | 6.2.4 |
| 数据库 | H2 Embedded Database | 2.2.224 |
| AI Agent | AgentScope Java SDK | 2.0.0-RC4 |
| API 文档 | SpringDoc OpenAPI / Swagger UI | 2.5.0 |
| 原部署 | Ubuntu · Nginx · DigitalOcean Droplet | 已离线 |

## 目录结构

```text
convergent-memory/
├── pom.xml
└── src/main/
    ├── java/com/convergentmemory/
    │   ├── ConvergentMemoryApplication.java
    │   ├── agent/
    │   │   └── ConvergerAgent.java
    │   ├── config/
    │   │   ├── ApiKeyAuthFilter.java
    │   │   ├── SecurityConfig.java
    │   │   ├── JacksonConfig.java
    │   │   └── OpenApiConfig.java
    │   ├── controller/
    │   │   ├── AuthController.java
    │   │   ├── ConvergeController.java
    │   │   ├── MemoryController.java
    │   │   └── PublicController.java
    │   ├── dto/
    │   ├── entity/
    │   ├── repository/
    │   └── service/
    └── resources/
        └── application.yml
```

## API 概览

| 方法 | 路径 | 鉴权 | 功能 |
|---|---|---|---|
| GET | `/api/auth/quick/{username}` | 无 | 快速创建账号并返回 Token |
| POST | `/api/auth/register` | 无 | 注册 |
| POST | `/api/auth/login` | 无 | 登录 |
| POST | `/api/memory/add` | Bearer | 写入记忆 |
| GET | `/api/memory/search?q=` | Bearer | 搜索记忆 |
| POST | `/api/converge/preview` | Bearer | 生成收敛草案，不落盘 |
| POST | `/api/converge/apply/{id}` | Bearer | 确认并应用收敛结果 |
| GET | `/u/{username}?token=` | Token | 个人记忆库页面 |
| GET | `/u/{username}/agent-guide?token=` | Token | AI 助手使用手册 |

## 本地恢复运行

### 环境要求

- JDK 17+
- Maven 3.6+

仓库中的 `application.yml` 保留了原 Linux 服务器路径。不要直接依赖 `/root/...`，本地启动时建议使用环境变量覆盖为项目内相对路径。

### macOS / Linux

```bash
export SPRING_DATASOURCE_URL='jdbc:h2:file:./data/index;MODE=MySQL;DB_CLOSE_DELAY=-1'
export MEMORY_VAULT_PATH='./data/memory-vault'
export MEMORY_API_KEY='replace-with-a-local-secret'
# 需要测试 LLM 收敛时再设置：
# export DASHSCOPE_API_KEY='your-key'

mvn spring-boot:run
```

### Windows PowerShell

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:h2:file:./data/index;MODE=MySQL;DB_CLOSE_DELAY=-1'
$env:MEMORY_VAULT_PATH = './data/memory-vault'
$env:MEMORY_API_KEY = 'replace-with-a-local-secret'
# 需要测试 LLM 收敛时再设置：
# $env:DASHSCOPE_API_KEY = 'your-key'

mvn spring-boot:run
```

默认端口为：

```text
http://localhost:8081
```

> 这个源码快照此前在线运行成功，但尚未在 2026 年的新机器上完成一次从零依赖安装与回归测试。重新作为作品集发布前，应先执行编译、接口测试和本地数据路径验证。

## 作品集定位

这个项目不需要为了作品集重新购买服务器长期托管。更合理的处理方式是：

- 保留它作为一项**已经完成并演示过的 Java 服务端课程项目**。
- 明确标注“原演示服务器已离线”。
- 在本地完成一次可复现启动和 3–5 个核心接口测试。
- 补充 Swagger 截图、架构图或课堂演示照片/视频。
- 后续将本分支拆成独立仓库，例如 `convergent-memory-api`，避免与 `master` 分支的 Markdown 实验方案混在一起。

## 学术与设计参考

- MRAgent：三层记忆模型与召回线索。
- Qwen-AgentWorld 1.0：可控扰动与人类反馈思路。

---

**维护状态：Archived / Portfolio Project**  
源码保留，原部署离线；未来只做可复现性、文档和安全配置方面的整理。
