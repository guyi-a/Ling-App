# Ling-App Android 客户端 - 开发文档

> 基于 Jetpack Compose + Material 3 的 AI 对话助手 Android 客户端
> 
> **最后更新**: 2026-05-08  
> **当前版本**: v2.1.0  
> **开发状态**: 核心功能已完成，应用预览功能已实现

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
WebView:   AndroidView + WebView (应用预览)
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

### 项目系统 ✨ NEW
- 项目列表（侧边抽屉）
- 项目详情页（会话列表、项目信息）
- **应用预览功能**：
  - 检测工作区中的 `index.html`（静态预览）
  - 查询运行中的 dev server（动态预览）
  - WebView 全屏预览生成的 Web 应用
  - 支持两种预览模式：
    - `/api/preview/{port}/` - 反向代理到运行中的 dev server
    - `/api/workspace/{sid}/download?path=index.html&inline=true` - 静态文件预览

### 工作区 ✨ 重构
- **树形文件浏览器**（替代原有的 uploads/outputs 平铺列表）
- 面包屑导航（支持点击跳转到任意父目录）
- 文件上传 / 下载 / 删除
- 目录导航（点击目录进入子目录）
- 文件操作菜单（下载、删除）
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
│   │   ├── ProjectApi.kt         # 项目列表/详情
│   │   ├── WorkspaceApi.kt       # 工作区文件（树形 API）
│   │   └── DevApi.kt             # ✨ NEW: Dev server 进程查询
│   ├── local/
│   │   ├── TokenManager.kt       # JWT Token + userId/username 存储
│   │   └── ThemeManager.kt       # 主题/字体/设置项持久化
│   ├── network/
│   │   ├── RetrofitClient.kt     # Retrofit + OkHttp 配置
│   │   ├── SSEManager.kt         # SSE 流式连接管理
│   │   └── ApiConfig.kt          # BASE_URL 配置（10.0.2.2:9000）
│   ├── model/
│   │   ├── DevProcess.kt         # ✨ NEW: Dev server 进程模型
│   │   ├── WorkspaceFile.kt      # 工作区文件（含 TreeEntry/TreeResponse）
│   │   ├── Project.kt            # 项目模型
│   │   └── ...                   # 其他数据模型
│   ├── repository/               # 数据仓库层
│   │   ├── AuthRepository.kt
│   │   ├── SessionRepository.kt
│   │   ├── MessageRepository.kt
│   │   ├── ProjectRepository.kt
│   │   ├── WorkspaceRepository.kt # 支持树形 API（getTree/downloadByPath/deleteByPath）
│   │   └── DevRepository.kt      # ✨ NEW: 查询运行中的应用
│   └── AppContainer.kt           # 手动 DI 容器
├── ui/
│   ├── screen/
│   │   ├── auth/                 # LoginScreen, RegisterScreen
│   │   ├── home/                 # WelcomeScreen（精简版）
│   │   ├── chat/                 # ChatScreen + ChatViewModel
│   │   ├── profile/              # ProfileScreen, ChangePasswordScreen, AccountSecurityScreen
│   │   ├── settings/             # SettingsScreen（完整设置页）
│   │   ├── project/              # ✨ NEW: ProjectDetailScreen（项目详情+应用预览入口）
│   │   ├── webview/              # ✨ NEW: WebViewScreen（全屏应用预览）
│   │   └── workspace/            # WorkspaceScreen + WorkspaceViewModel（树形文件浏览器）
│   ├── components/               # 通用组件
│   │   ├── ChatInputBar.kt       # 聊天输入框
│   │   ├── NavigationDrawer.kt   # 侧边抽屉（会话列表 + 项目列表）
│   │   ├── ConfirmDialog.kt      # 确认/删除/登出/信息/警告对话框
│   │   ├── AttachmentCard.kt     # 附件预览
│   │   ├── ToolApprovalDialog.kt # 工具审批
│   │   ├── StopGenerationButton.kt
│   │   ├── WorkspaceFilePicker.kt
│   │   ├── EmptyState.kt
│   │   ├── LoadingCard.kt
│   │   └── ErrorCard.kt
│   ├── navigation/
│   │   └── NavGraph.kt           # 路由定义与导航（含 webview 路由）
│   └── theme/
│       ├── Color.kt              # 5 组主题颜色定义
│       ├── Theme.kt              # Demo1Theme（支持 themeColor + fontSizeName）
│       └── Type.kt               # Typography + scaledTypography 字体缩放
```

---

## 路由表

```
/login                          → 登录
/register                       → 注册
/welcome                        → 欢迎页（右上角个人资料入口）
/chat                           → 新对话
/chat?message={msg}             → 带初始消息的新对话
/chat/{sessionId}               → 历史会话
/workspace/{id}?title={title}   → 工作区文件管理（树形浏览器）
/settings                       → 设置页（修改密码入口在此）
/profile                        → 个人资料（头像、用户名、账号安全）
/change_password                → 修改密码
/account_security               → 账号安全信息
/project/{projectId}            → ✨ NEW: 项目详情页（会话列表 + 应用预览入口）
/webview/{port}                 → ✨ NEW: WebView 预览（端口模式）
/webview?url={url}&title={title} → ✨ NEW: WebView 预览（URL 模式）
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
| 项目 | GET /api/projects | ✅ |
| 项目 | GET /api/projects/{id} | ✅ |
| 聊天 | POST /api/chat/stream (SSE) | ✅ |
| 聊天 | POST /api/chat/approve | ✅ |
| 聊天 | POST /api/chat/{id}/stop | ✅ |
| 消息 | GET /api/messages/session/{id}/history | ✅ |
| 消息 | DELETE /api/messages/{id} | ✅ |
| 消息 | DELETE /api/messages/session/{id}/after/{id} | ✅ |
| 工作区 | GET /api/workspace/{sid}/tree?path={path} | ✅ |
| 工作区 | GET /api/workspace/{sid}/download?path={path} | ✅ |
| 工作区 | DELETE /api/workspace/{sid}/files?path={path} | ✅ |
| 工作区 | POST /api/workspace/{sid}/files | ✅ |
| Dev | GET /api/dev/{sid}/processes | ✅ |
| 预览 | GET /api/preview/{port}/ | ✅（反向代理） |

---

## 应用预览功能详解 ✨

### 功能背景
AI 生成的 Web 应用（HTML/JS 游戏、FastAPI 应用等）需要在 Android 端预览。后端通过 `process_manager` 启动 dev server 并分配端口，Web 端通过 `/api/preview/{port}/` 反向代理访问。

### 实现方案
1. **ProjectDetailScreen** 在加载项目详情时：
   - 查询工作区是否有 `index.html`（通过 `/api/workspace/{sid}/tree?path=.`）
   - 查询运行中的 dev server（通过 `/api/dev/{sid}/processes`）
   - 如果有任一条件满足，显示"打开应用"按钮

2. **预览优先级**：
   - 优先使用运行中的 dev server：`/api/preview/{port}/`
   - 否则使用静态文件预览：`/api/workspace/{sid}/download?path=index.html&inline=true`

3. **WebViewScreen** 全屏预览：
   - 接受 `port: Int?` 或 `url: String?` 参数
   - 注入 Authorization header（从 TokenManager 获取）
   - 启用 JavaScript、DOM Storage
   - 显示加载进度条、刷新按钮
   - 动态更新页面标题（通过 WebChromeClient）

### 技术细节
- **WebView 认证**：通过 `loadUrl(url, headers)` 传递 `Authorization: Bearer {token}`
- **端口访问**：Android 模拟器访问宿主机用 `10.0.2.2:9000`
- **URL 编码**：导航传参时使用 `URLEncoder.encode()` / `URLDecoder.decode()`
- **单文件 HTML**：大多数生成的应用是单文件 HTML（所有 JS/CSS 内联），静态预览可直接工作

### 使用流程
1. 侧边栏点击项目卡片
2. 进入 ProjectDetailScreen
3. 看到"打开应用"按钮（如果有 index.html 或运行中的 dev server）
4. 点击后在 WebViewScreen 中全屏预览

---

## 工作区树形浏览器 ✨

### 重构背景
原有的 WorkspaceScreen 是平铺的 uploads/outputs 列表，不支持目录结构。重构为树形文件浏览器。

### 新功能
- **面包屑导航**：显示当前路径，支持点击跳转到任意父目录
- **目录导航**：点击目录进入子目录
- **文件操作**：下载、删除（通过下拉菜单）
- **路径栈管理**：`pathStack` 维护导航历史，支持返回上级

### API 变化
- `GET /api/workspace/{sid}/tree?path={path}` - 获取指定路径的目录树
- `GET /api/workspace/{sid}/download?path={path}` - 下载指定路径的文件
- `DELETE /api/workspace/{sid}/files?path={path}` - 删除指定路径的文件

### ViewModel 重构
- `WorkspaceViewModel` 从 `uploads/outputs` 状态改为 `pathStack/currentEntries`
- 新增 `navigateInto(dir)`, `navigateUp()`, `navigateTo(index)` 方法

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
- 预览 URL 格式：`http://10.0.2.2:9000/api/preview/{port}/`

### 数据表关系
- `users` 表存用户资料（username, avatar, device_model 等）
- `accounts` 表存登录凭证（username, password_hash）
- 修改用户名时两张表需要同步更新（后端已处理）

### 设置持久化
- 所有设置通过 `ThemeManager`（DataStore）持久化
- `MainActivity` 读取 darkMode / themeColor / fontSize 并传给 `Demo1Theme`
- 主题颜色和字体大小立即生效，无需重启

### WebView 安全
- 启用 JavaScript（必需）
- 禁用文件访问（`settings.allowFileAccess = false`）
- 设置 WebViewClient 拦截 URL 跳转

---

## 设计规范

### 颜色
```
Primary              → 主要按钮、强调元素
PrimaryContainer     → 用户消息背景、Logo 背景
TertiaryContainer    → "打开应用"按钮背景
Surface              → 卡片背景
SurfaceVariant       → AI 消息背景、输入框
Error                → 错误、删除、危险操作
```

### 圆角
```
8.dp   → 小按钮、标签、InfoChip
12.dp  → 输入框、普通卡片
16.dp  → 主要按钮、大卡片
24.dp  → Logo、特殊元素
```

---

## 最近更新（2026-05-08）

### 新增功能
1. **应用预览系统**
   - 新增 `DevApi` / `DevRepository` 查询运行中的 dev server
   - 新增 `WebViewScreen` 全屏预览生成的 Web 应用
   - `ProjectDetailScreen` 添加"打开应用"入口
   - 支持两种预览模式：动态（dev server）+ 静态（index.html）

2. **工作区重构**
   - 从平铺列表改为树形文件浏览器
   - 新增面包屑导航、目录导航
   - 支持任意路径的文件操作

### 修复问题
- 修复 `ProjectDetailScreen.kt` 重复导入 `ApiConfig`
- 修复 `WorkspaceViewModel` 调用错误方法（`loadFiles` → `loadTree`）

### 待测试
- 重新编译 App 后测试项目详情页导航
- 测试应用预览功能（静态 + 动态）
- 测试工作区树形浏览器

---

## 开发规范

### Git 提交
- **不要**添加 `Co-Authored-By` 合作作者标记（用户偏好）
- 提交信息使用中文，简洁描述改动内容

### 代码风格
- 使用 Kotlin 官方代码风格
- Composable 函数首字母大写
- ViewModel 使用 `StateFlow` 管理状态
- Repository 返回 `Result<T>` 封装成功/失败

---

**文档维护者**: Claude Code  
**创建日期**: 2026-04-02  
**文档版本**: v3.0  
**最后更新**: 2026-05-08
