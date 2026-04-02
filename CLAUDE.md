# Ling-App Android 客户端 - 开发文档

> 📱 基于 Jetpack Compose + Material 3 的 AI 对话助手 Android 客户端
> 
> **最后更新**: 2026-04-02  
> **当前版本**: 1.0.0-alpha  
> **开发状态**: UI 界面完成，待对接后端 API

---

## 📋 项目概述

### 项目简介
Ling-App 是 Ling-Agent AI 助手的 Android 客户端，提供流畅的移动端对话体验。

### 后端项目
- **路径**: `/Users/guyi/Ling-Agent`
- **技术**: FastAPI + LangGraph + 通义千问
- **端口**: `http://localhost:9000`
- **文档**: `http://localhost:9000/docs`

### 设计原则
- 🎨 **Material 3 设计规范** - 现代卡片式设计
- 💡 **界面优先** - 先完成美观的 UI，后对接 API
- 🧩 **组件化** - 高复用的通用组件库
- 📱 **响应式** - 支持暗黑模式和多种屏幕

---

## 🛠️ 技术栈

### 核心技术
```kotlin
- Kotlin: 2.2.10
- Jetpack Compose: 2024.09
- Material 3: Latest
- Min SDK: 24
- Target SDK: 36
- Gradle: 9.3.1
```

### 主要库
- **Compose UI** - 声明式 UI 框架
- **Navigation Compose** - 页面导航
- **Material Icons** - 图标库（Filled + Outlined）
- **Coroutines** - 异步处理

---

## ✅ 已完成功能

### 1️⃣ 界面层（100%）

#### 🔐 认证界面
- **LoginScreen.kt** - 登录页面
  - Material 3 卡片风格
  - 渐变背景
  - 密码可见性切换
  - 加载状态动画
  - ✨ **品牌 Logo**: AutoAwesome 图标（替换了旧的机器人）

#### 🏠 欢迎界面
- **WelcomeScreen.kt** - 首页
  - Logo 展示（AutoAwesome 星星图标）
  - 示例问题卡片（可点击快速开始）
  - 功能介绍卡片
  - 开始对话按钮
  
#### 💬 聊天界面
- **ChatScreen.kt** - 对话页面
  - 消息列表（用户/AI 气泡区分）
  - 空状态提示
  - 附件功能（添加、预览、删除）
  - 生成中状态指示器（动画圆点）
  - 停止生成按钮（浮动按钮）
  - 工具审批弹窗（演示）
  - 输入框（支持多行，最多5行）
  - 附件按钮（带数量徽章）
  - 顶部菜单：
    - 📂 查看工作区
    - 🗑️ 清空对话

#### 📂 工作区管理
- **WorkspaceScreen.kt** - 文件管理页面
  - 上传文件列表（📤 分类展示）
  - 生成文件列表（📥 分类展示）
  - 文件卡片（名称、大小、时间、类型图标）
  - 文件操作：
    - 👀 预览（图片/PDF）
    - 📥 下载
    - 🗑️ 删除
  - 云上传按钮
  - 空状态提示

#### 🗂️ 侧边抽屉
- **NavigationDrawer.kt** - 会话管理
  - 品牌头部（AutoAwesome 图标）
  - 🔍 搜索功能（实时过滤）
  - 会话分组（今天/昨天/本周/更早）
  - 新建对话按钮（圆形图标）
  - 会话卡片（标题、最后消息、时间、消息数）
  - 删除会话功能
  - 空状态提示
  - 设置入口

### 2️⃣ 通用组件库（8个组件）

#### EmptyState.kt
- 空状态展示组件
- 完整版 + 紧凑版

#### LoadingCard.kt
- 加载指示器
- 骨架屏效果（Shimmer）
- 消息骨架屏
- 头像骨架屏

#### ErrorCard.kt
- 错误提示卡片
- 简化版错误消息
- 网络错误专用
- Snackbar 支持

#### ConfirmDialog.kt
- 通用确认对话框
- 带图标版本
- 删除确认快捷封装
- 登出确认快捷封装
- 信息提示对话框
- 警告对话框

#### AttachmentCard.kt
- 附件预览卡片
- 附件列表（横向滚动）
- 消息中的附件预览
- 文件类型图标（图片/PDF/CSV/文件）
- 文件大小格式化

#### ToolApprovalDialog.kt
- 工具审批对话框
- 参数展示（可展开/收起）
- 安全警告提示
- 工具执行中状态卡片

#### StopGenerationButton.kt
- 停止生成浮动按钮（带脉冲动画）
- 紧凑版停止按钮
- 生成指示器（动画圆点）
- 流式输出文本（带闪烁光标）

### 3️⃣ 导航系统

#### NavGraph.kt - 路由定义
```kotlin
- /login          → 登录页面
- /welcome        → 欢迎页面
- /chat           → 新对话页面
- /chat?message=  → 带初始消息的对话
- /chat/{id}      → 历史会话
- /workspace/{id} → 工作区文件管理
```

#### 抽屉导航
- 侧滑打开/关闭
- 手势支持
- 路由跳转集成

---

## 🎨 设计规范

### 品牌标识
- **Logo**: AutoAwesome 图标 ✨
- **颜色**: Material 3 动态主题色
- **风格**: 现代、轻盈、科技感

### 图标风格
| 类型 | 风格 | 示例 |
|------|------|------|
| 主要操作 | Filled | FolderOpen, CloudUpload |
| 次要操作 | Outlined | Delete (轮廓版) |
| 品牌标识 | Filled | AutoAwesome |
| 文件类型 | Filled | Image, PictureAsPdf, TableChart |

### 颜色使用
```kotlin
Primary              // 主要按钮、强调元素
PrimaryContainer     // 用户消息背景、Logo 背景
Surface              // 卡片背景
SurfaceVariant       // AI 消息背景、输入框
OnSurface            // 主要文字
OnSurfaceVariant     // 次要文字
Error                // 错误、删除按钮
```

### 圆角规范
```kotlin
8.dp   // 小按钮、标签
12.dp  // 输入框、普通卡片
16.dp  // 主要按钮、大卡片
24.dp  // Logo、特殊元素
```

### 间距规范
```kotlin
4.dp       // 极小间距
8.dp       // 小间距
12-16.dp   // 中等间距
24.dp      // 页面边距
32-48.dp   // 区块间距
```

### 阴影规范
```kotlin
0.dp   // 扁平卡片
1-2.dp // 列表项
4-6.dp // 悬浮卡片
8.dp   // 重要弹窗
```

---

## 📁 项目结构

```
app/src/main/java/com/guyi/demo1/
├── MainActivity.kt                   # 应用入口
├── ui/
│   ├── components/                   # 通用组件库 ✅
│   │   ├── NavigationDrawer.kt      # 侧边抽屉（搜索、分组）
│   │   ├── EmptyState.kt            # 空状态组件
│   │   ├── LoadingCard.kt           # 加载状态（含骨架屏）
│   │   ├── ErrorCard.kt             # 错误提示
│   │   ├── ConfirmDialog.kt         # 确认对话框
│   │   ├── AttachmentCard.kt        # 附件预览
│   │   ├── ToolApprovalDialog.kt    # 工具审批
│   │   └── StopGenerationButton.kt  # 停止生成
│   ├── screen/                       # 页面
│   │   ├── auth/
│   │   │   └── LoginScreen.kt       # 登录页 ✅
│   │   ├── home/
│   │   │   └── WelcomeScreen.kt     # 欢迎页 ✅
│   │   ├── chat/
│   │   │   └── ChatScreen.kt        # 聊天页 ✅
│   │   ├── workspace/
│   │   │   └── WorkspaceScreen.kt   # 工作区 ✅
│   │   ├── settings/                # ❌ 待创建
│   │   │   └── SettingsScreen.kt
│   │   └── profile/                 # ❌ 待创建
│   │       └── ProfileScreen.kt
│   ├── navigation/
│   │   └── NavGraph.kt              # 路由配置 ✅
│   └── theme/
│       ├── Color.kt                 # 颜色定义
│       ├── Theme.kt                 # 主题配置
│       └── Type.kt                  # 字体配置
```

---

## 🔗 后端 API 对应关系

### 认证模块
| 后端 API | 客户端界面 | 状态 |
|----------|------------|------|
| `POST /api/auth/register` | LoginScreen (注册) | ❌ 待对接 |
| `POST /api/auth/login` | LoginScreen | ❌ 待对接 |
| `POST /api/auth/refresh` | Token 管理 | ❌ 待实现 |

### 用户模块
| 后端 API | 客户端界面 | 状态 |
|----------|------------|------|
| `GET /api/users/{id}` | ProfileScreen | ❌ 待创建 |
| `PUT /api/users/{id}` | ProfileScreen | ❌ 待创建 |

### 会话模块
| 后端 API | 客户端界面 | 状态 |
|----------|------------|------|
| `POST /api/sessions` | ChatScreen (新建) | ❌ 待对接 |
| `GET /api/sessions` | NavigationDrawer | ❌ 待对接 |
| `GET /api/sessions/{id}` | ChatScreen | ❌ 待对接 |
| `DELETE /api/sessions/{id}` | NavigationDrawer | ❌ 待对接 |

### 聊天模块
| 后端 API | 客户端界面 | 状态 |
|----------|------------|------|
| `POST /api/chat/stream` | ChatScreen (SSE) | ❌ 待对接 |
| `POST /api/chat/approve` | ToolApprovalDialog | ❌ 待对接 |
| `POST /api/chat/{id}/stop` | StopGenerationButton | ❌ 待对接 |
| `GET /api/chat/{id}/history` | ChatScreen | ❌ 待对接 |

### 工作区模块
| 后端 API | 客户端界面 | 状态 |
|----------|------------|------|
| `POST /api/workspace/{id}/upload` | WorkspaceScreen | ❌ 待对接 |
| `GET /api/workspace/{id}/files` | WorkspaceScreen | ❌ 待对接 |
| `GET /api/workspace/{id}/files/{folder}/{name}` | 文件预览 | ❌ 待对接 |
| `DELETE /api/workspace/{id}/files/{folder}/{name}` | WorkspaceScreen | ❌ 待对接 |

### 消息模块
| 后端 API | 客户端界面 | 状态 |
|----------|------------|------|
| `GET /api/messages/session/{id}` | ChatScreen | ❌ 待对接 |
| `GET /api/messages/session/{id}/search` | 搜索功能 | ❌ 待实现 |

---

## ❌ 待实现功能

### P1 - 网络层（高优先级）
- [ ] Retrofit / Ktor HTTP 客户端配置
- [ ] SSE (Server-Sent Events) 流式事件处理
- [ ] JWT Token 管理和拦截器
- [ ] API 数据模型类（data class）
- [ ] Repository 层
- [ ] ViewModel + StateFlow

### P2 - 核心功能对接
- [ ] 真实登录/注册 API 对接
- [ ] 流式对话接入（SSE）
- [ ] 会话列表加载
- [ ] 历史消息加载
- [ ] 文件上传/下载
- [ ] 工具审批 API 对接
- [ ] 停止生成 API 对接

### P3 - 缺失界面
- [ ] **RegisterScreen.kt** - 注册页面
- [ ] **SettingsScreen.kt** - 设置页面
  - 用户信息
  - 偏好设置（暗黑模式、字体大小）
  - 工作区设置
  - 账号安全
  - 关于信息
- [ ] **ProfileScreen.kt** - 用户资料页

### P4 - 高级功能
- [ ] 消息搜索功能
- [ ] 会话置顶
- [ ] 会话重命名
- [ ] 批量删除
- [ ] Markdown 渲染（代码高亮）
- [ ] 图片预览（全屏查看）
- [ ] PDF 预览
- [ ] 文件分享
- [ ] 消息复制
- [ ] 消息长按菜单

### P5 - 优化体验
- [ ] 错误处理和重试机制
- [ ] 网络状态检测
- [ ] 下拉刷新
- [ ] 分页加载
- [ ] 本地缓存
- [ ] 推送通知
- [ ] 离线模式

---

## 🚨 重要注意事项

### API 兼容性问题 ⚠️
在开发过程中遇到了 Material 3 API 版本兼容问题：

```kotlin
// ❌ 错误写法（较新版本 API，项目中不可用）
TextFieldDefaults.colors()
TextFieldDefaults.textFieldColors()

// ✅ 正确写法（使用 BasicTextField）
BasicTextField(
    value = text,
    onValueChange = { text = it },
    decorationBox = { innerTextField ->
        if (text.isEmpty()) {
            Text("placeholder...")
        }
        innerTextField()
    }
)
```

### Compose 使用注意
```kotlin
// ❌ AnimatedVisibility 不能在 Box 中直接使用
Box {
    AnimatedVisibility(...) { }  // 编译错误
}

// ✅ 使用简单的 if 判断
Box {
    if (visible) {
        Component()
    }
}
```

### Git 状态
- 当前分支: `main`
- 最新提交: 重构 UI 为 Material 3 卡片风格设计
- 未提交文件: 新创建的组件和界面

---

## 🎯 下一步计划

### 立即优先级
1. **创建数据模型类** - 定义所有 API 请求/响应的 data class
2. **配置 Retrofit** - 设置 HTTP 客户端和拦截器
3. **实现 JWT 管理** - Token 存储和自动刷新
4. **对接登录 API** - 实现真实的用户认证

### 短期目标
- 完成网络层基础架构
- 对接聊天 API（非流式版本）
- 加载真实的会话列表
- 加载历史消息

### 中期目标
- 实现 SSE 流式对话
- 工具审批功能完整对接
- 工作区文件操作完整实现
- 创建设置页面

### 长期目标
- 完善所有高级功能
- 性能优化
- 单元测试
- UI 测试
- 发布准备

---

## 📚 参考资源

- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [后端 API 文档](http://localhost:9000/docs)
- [后端项目 README](../Ling-Agent/README.md)

---

## 💡 开发建议

### 给未来的 Claude Code
1. **界面已完成** - UI 层已经很完善了，专注于网络层实现
2. **组件可复用** - 通用组件库已经很完整，可以直接使用
3. **设计规范** - 遵循上面的设计规范，保持视觉统一
4. **API 兼容** - 注意 Material 3 API 版本问题，参考上面的解决方案
5. **先易后难** - 建议先实现简单的 GET/POST，再处理 SSE 流式

### 代码风格
- 遵循 Kotlin 官方编码规范
- 组件命名清晰（动词 + 名词，如 LoadingCard, EmptyState）
- 注释简洁，重要的地方加 TODO 标记
- 文件头部加简要说明
- 使用 `remember` 管理状态
- 使用 `LaunchedEffect` 处理副作用

---

**文档维护者**: Claude Code Assistant  
**创建日期**: 2026-04-02  
**文档版本**: v1.0
