# Ling-App Android 客户端 - 开发文档

> 基于 Jetpack Compose + Material 3 的 AI 对话助手 Android 客户端
> 
> **最后更新**: 2026-04-08  
> **当前版本**: v1.1.0  
> **开发状态**: 核心功能已完成，前后端已对接

---

## 项目概述

Ling-App 是 Ling-Agent AI 助手的 Android 原生客户端，对标 Web 前端功能，提供移动端完整的对话体验。

### 后端项目
- **路径**: `/Users/guyi/Ling-Agent/agent-service`
- **技术**: FastAPI + LangGraph + 通义千问
- **端口**: `http://10.0.2.2:9000`（Android 模拟器访问本机）
- **API 文档**: `http://localhost:9000/docs`

---

## 技术栈

```
Kotlin 2.2.10 / Min SDK 24 / Target SDK 36

UI:        Jetpack Compose + Material 3
网络:      Retrofit + kotlinx.serialization + OkHttp
流式:      OkHttp EventSource (SSE)
图片:      Coil (AsyncImage)
本地存储:  DataStore Preferences
Markdown:  compose-markdown (jeziellago)
导航:      Navigation Compose
```

---

## 已完成功能

### 认证系统
- 登录 / 注册（JWT）
- Token 持久化（DataStore）
- 修改密码（成功后跳转登录页）

### 对话系统
- SSE 流式对话（token 级别）
- 多会话管理（侧边抽屉，按时间分组，搜索过滤）
- 停止生成
- 对话历史加载
- Markdown 渲染（AI 消息）
- 消息操作（长按菜单）：复制、删除、重新生成、编辑

### 工作区
- 文件上传 / 下载 / 删除
- 文件分类展示（uploads / outputs）
- 工具审批弹窗

### 用户系统
- 个人资料页（头像上传、用户名编辑）
- 头像：Coil AsyncImage + ActivityResultContracts 选图 + Multipart 上传
- 账号安全信息页（设备、注册时间、活跃时间、状态）

### 设置页（完整实现）
- **外观**: 深色模式开关、5 种主题颜色（蓝/紫/绿/橙/红）、4 档字体大小
- **聊天**: 消息通知开关
- **账号**: 修改密码入口、退出登录
- **关于**: 版本信息、隐私政策、用户协议、反馈与支持
- 所有设置项通过 DataStore 持久化

### 主题系统
- 5 种主题配色方案，即时切换
- 4 档字体缩放（小/中/大/超大），全局 Typography 缩放
- 深色模式持久化，支持跟随系统

---

## 项目结构

```
app/src/main/java/com/guyi/demo1/
├── MainActivity.kt               # 入口：读取主题设置，应用 Demo1Theme
├── LingAgentApplication.kt       # Application：初始化 AppContainer
├── data/
│   ├── api/                      # Retrofit 接口定义
│   │   ├── AuthApi.kt            # 登录/注册/修改密码
│   │   ├── ChatApi.kt            # 聊天相关
│   │   ├── UserApi.kt            # 用户信息/头像上传
│   │   ├── SessionApi.kt         # 会话 CRUD
│   │   ├── MessageApi.kt         # 消息操作
│   │   └── WorkspaceApi.kt       # 工作区文件
│   ├── local/
│   │   ├── TokenManager.kt       # JWT Token + userId/username 存储
│   │   └── ThemeManager.kt       # 主题/字体/设置项持久化
│   ├── network/
│   │   ├── RetrofitClient.kt     # Retrofit + OkHttp 配置
│   │   └── SSEManager.kt         # SSE 流式连接管理
│   ├── repository/               # 数据仓库层
│   │   ├── AuthRepository.kt
│   │   ├── SessionRepository.kt
│   │   ├── MessageRepository.kt
│   │   └── WorkspaceRepository.kt
│   └── AppContainer.kt           # 手动 DI 容器
├── ui/
│   ├── screen/
│   │   ├── auth/                 # LoginScreen, RegisterScreen
│   │   ├── home/                 # WelcomeScreen（精简版）
│   │   ├── chat/                 # ChatScreen + ChatViewModel
│   │   ├── profile/              # ProfileScreen, ChangePasswordScreen, AccountSecurityScreen
│   │   ├── settings/             # SettingsScreen（完整设置页）
│   │   └── workspace/            # WorkspaceScreen
│   ├── components/               # 通用组件
│   │   ├── ChatInputBar.kt       # 聊天输入框
│   │   ├── NavigationDrawer.kt   # 侧边抽屉（会话列表）
│   │   ├── ConfirmDialog.kt      # 确认/删除/登出/信息/警告对话框
│   │   ├── AttachmentCard.kt     # 附件预览
│   │   ├── ToolApprovalDialog.kt # 工具审批
│   │   ├── StopGenerationButton.kt
│   │   ├── WorkspaceFilePicker.kt
│   │   ├── EmptyState.kt
│   │   ├── LoadingCard.kt
│   │   └── ErrorCard.kt
│   ├── navigation/
│   │   └── NavGraph.kt           # 路由定义与导航
│   └── theme/
│       ├── Color.kt              # 5 组主题颜色定义
│       ├── Theme.kt              # Demo1Theme（支持 themeColor + fontSizeName）
│       └── Type.kt               # Typography + scaledTypography 字体缩放
```

---

## 路由表

```
/login              → 登录
/register           → 注册
/welcome            → 欢迎页（右上角个人资料入口）
/chat               → 新对话
/chat?message={msg} → 带初始消息的新对话
/chat/{sessionId}   → 历史会话
/workspace/{id}     → 工作区文件管理
/settings           → 设置页（修改密码入口在此）
/profile            → 个人资料（头像、用户名、账号安全）
/change_password    → 修改密码
/account_security   → 账号安全信息
```

---

## 后端 API 对接状态

| 模块 | API | 状态 |
|------|-----|------|
| 认证 | POST /api/auth/register | ✅ |
| 认证 | POST /api/auth/login | ✅ |
| 认证 | POST /api/auth/change-password | ✅ |
| 用户 | GET /api/users/{id} | ✅ |
| 用户 | PUT /api/users/{id} | ✅（同步 accounts 表 username） |
| 用户 | POST /api/users/{id}/avatar | ✅ |
| 用户 | GET /api/users/{id}/avatar | ✅ |
| 会话 | GET/POST/DELETE /api/sessions | ✅ |
| 聊天 | POST /api/chat/stream (SSE) | ✅ |
| 聊天 | POST /api/chat/approve | ✅ |
| 聊天 | POST /api/chat/{id}/stop | ✅ |
| 消息 | GET /api/messages/session/{id}/history | ✅ |
| 消息 | DELETE /api/messages/{id} | ✅ |
| 消息 | DELETE /api/messages/session/{id}/after/{id} | ✅ |
| 工作区 | POST/GET/DELETE /api/workspace/{id}/files | ✅ |

---

## 开发注意事项

### Compose API 兼容
```kotlin
// 使用 BasicTextField 而非 OutlinedTextField 做自定义输入框
// 输入框光标已设为透明：cursorBrush = SolidColor(Color.Transparent)
```

### 网络地址
- Android 模拟器访问本机用 `10.0.2.2`，不是 `localhost`
- 头像 URL 格式：`http://10.0.2.2:9000/api/users/{userId}/avatar`

### 数据表关系
- `users` 表存用户资料（username, avatar, device_model 等）
- `accounts` 表存登录凭证（username, password_hash）
- 修改用户名时两张表需要同步更新（后端已处理）

### 设置持久化
- 所有设置通过 `ThemeManager`（DataStore）持久化
- `MainActivity` 读取 darkMode / themeColor / fontSize 并传给 `Demo1Theme`
- 主题颜色和字体大小立即生效，无需重启

---

## 设计规范

### 颜色
```
Primary              → 主要按钮、强调元素
PrimaryContainer     → 用户消息背景、Logo 背景
Surface              → 卡片背景
SurfaceVariant       → AI 消息背景、输入框
Error                → 错误、删除、危险操作
```

### 圆角
```
8.dp   → 小按钮、标签
12.dp  → 输入框、普通卡片
16.dp  → 主要按钮、大卡片
24.dp  → Logo、特殊元素
```

---

**文档维护者**: Claude Code  
**创建日期**: 2026-04-02  
**文档版本**: v2.0
