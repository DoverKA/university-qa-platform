# 大学问答平台 (University Q&A Platform)

一个基于 Spring Boot 和原生 JavaScript 的大学问答平台，支持学生提问、教师回答、AI 智能辅助等功能。

## 📋 项目简介

这是一个为大学师生设计的在线问答平台，旨在帮助学生解决学习中的问题，教师可以提供专业解答，同时集成了 AI 智能助手功能，为问题提供初步的 AI 回答。

## ✨ 主要功能

### 👤 用户系统
- 用户注册与登录
- 支持学生和教师两种角色
- 基于 JWT 的身份认证
- 用户会话持久化

### 📚 课程管理
- 教师可以创建和管理课程
- 课程信息包括：课程名称、专业、学期、描述
- 按专业和学期筛选课程
- 查看课程相关问题

### ❓ 问题管理
- 学生可以发布问题
- 问题必须关联到具体课程
- 支持按课程、学生、解决状态筛选问题
- 问题详情展示
- 标记问题为已解决

### 💬 回答系统
- 任何用户都可以回答问题
- 提问者或教师可以采纳最佳回答
- 按问题查看所有回答
- 支持删除回答

### 🤖 AI 智能助手
- 集成 Claude AI 模型
- 自动为问题生成 AI 回答
- 支持重新生成 AI 回答
- AI 回答与人工回答分开展示

### 📊 数据统计
- 仪表盘展示平台概况
- 课程总数统计
- 问题总数统计
- 已解决/未解决问题统计
- 最近问题展示

## 🛠️ 技术栈

### 后端
- **Java 11**
- **Spring Boot 2.7.18**
- **Spring Data JPA** - 数据持久化
- **Spring Security** - 安全认证
- **MySQL** - 数据库
- **JWT (jjwt 0.11.5)** - Token 认证
- **Lombok** - 简化 Java 代码
- **Maven** - 项目构建

### 前端
- **原生 JavaScript** (无框架)
- **HTML5/CSS3** - 界面设计
- **Fetch API** - HTTP 请求
- **LocalStorage** - 客户端存储

### 数据库
- **MySQL 5.7+**
- **JPA/Hibernate** - ORM 框架
- **自动建表** - DDL Auto Update

## 📁 项目结构

```
demo/
├── src/
│   ├── main/
│   │   ├── java/org/wy/demo/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/       # 控制器层
│   │   │   ├── entity/          # 实体类
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   └── security/        # 安全相关
│   │   └── resources/
│   │       ├── static/          # 前端静态文件
│   │       │   ├── index.html   # 主应用页面
│   │       │   └── ask.html    # AI 问答测试页面
│   │       └── application.yml  # 配置文件
│   └── test/                  # 测试代码
├── target/                    # 编译输出
├── pom.xml                    # Maven 配置
└── README.md                  # 项目说明
```

## 🚀 快速开始

### 环境要求
- JDK 11+
- Maven 3.6+
- MySQL 5.7+
- 现代浏览器（Chrome、Firefox、Edge 等）

### 数据库配置

1. 创建 MySQL 数据库：
```sql
CREATE DATABASE web CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置数据库连接（`src/main/resources/application.yml`）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/web?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: 123456
```

### 启动后端

1. 克隆项目：
```bash
git clone <repository-url>
cd demo
```

2. 编译项目：
```bash
mvn clean install
```

3. 启动应用：
```bash
mvn spring-boot:run
```

或者在 IDEA 中直接运行 `DemoApplication.main()` 方法。

### 访问前端

后端启动后，访问：
- 主应用：http://localhost:8080/index.html
- AI 测试：http://localhost:8080/ask.html

## 📡 API 接口

### 用户相关
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user` - 获取所有用户

### 课程相关
- `GET /api/courses` - 获取所有课程
- `POST /api/courses` - 创建课程（教师）

### 问题相关
- `GET /api/questions` - 获取所有问题
- `POST /api/questions` - 提交问题（学生）
- `GET /api/questions/{id}` - 获取问题详情
- `GET /api/questions/course/{courseId}` - 按课程查询问题
- `GET /api/questions/student/{studentId}` - 按学生查询问题
- `PUT /api/questions/{questionId}/solve` - 标记问题已解决
- `PUT /api/questions/{questionId}/ai-answer` - 生成 AI 回答

### 回答相关
- `GET /api/answers/question/{questionId}` - 获取问题的回答
- `POST /api/answers` - 提交回答
- `PUT /api/answers/{id}/accept` - 采纳最佳回答
- `DELETE /api/answers/{id}` - 删除回答

## 🔐 安全特性

- JWT Token 认证
- 密码加密存储
- CORS 跨域支持
- 角色权限控制（学生/教师）
- 请求拦截器

## 🎨 界面特点

- 🌙 完全中文化界面
- 📱 响应式设计，支持移动端
- 🎨 现代化 UI 设计
- 🔄 实时数据更新
- 💾 本地存储用户会话
- ⚡ 快速响应的交互体验

## 📝 使用说明

### 学生使用流程
1. 注册学生账户
2. 登录系统
3. 浏览课程
4. 提交问题（选择相关课程）
5. 等待 AI 或教师回答
6. 查看并采纳最佳回答

### 教师使用流程
1. 注册教师账户
2. 登录系统
3. 创建课程（课程名称、专业、学期）
4. 浏览学生问题
5. 回答学生问题
6. 标记问题为已解决

## 🔧 配置说明

### AI 配置（可选）

如需使用 AI 功能，在 `application.yml` 中配置：

```yaml
ai:
  api:
    key: your_anthropic_api_key_here
    url: https://api.anthropic.com/v1/messages
  model: claude-sonnet-4-20250514
```

### JWT 配置

```yaml
jwt:
  secret: universityQASecretKey2024ChangeThisInProduction
  expiration: 86400000  # 24小时
```

## 📊 数据库设计

### 主要表结构
- `user` - 用户表
- `course` - 课程表
- `question` - 问题表
- `answer` - 回答表

### 关系设计
- 用户与问题：一对多
- 课程与问题：一对多
- 问题与回答：一对多
- 教师与课程：一对多

## 🐛 已知问题

1. AI 功能需要配置 API Key 才能正常工作
2. 首次启动需要 MySQL 数据库已创建
3. 默认使用内存数据库，生产环境建议使用 MySQL

## 🚀 未来规划

- [ ] 添加文件上传功能
- [ ] 实现问题点赞功能
- [ ] 添加用户个人资料页面
- [ ] 实现消息通知系统
- [ ] 添加搜索历史记录
- [ ] 支持多语言切换
- [ ] 添加单元测试和集成测试
- [ ] Docker 容器化部署

## 📄 许可证

本项目仅供学习和研究使用。

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 开发者

- 项目名称：大学问答平台 (University Q&A Platform)
- 技术栈：Spring Boot + JavaScript
- 版本：1.0.0

## 📞 联系方式

如有问题或建议，欢迎通过 GitHub Issues 联系。

---

**注意**：本项目为教学演示项目，请勿在生产环境中直接使用。