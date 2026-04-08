# Ling Agent - Android Client

<div align="center">

智能 AI 对话助手 Android 应用

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Latest-orange.svg)](https://m3.material.io/)
[![Version](https://img.shields.io/badge/Version-1.1.0-brightgreen.svg)]()

</div>

---

## 项目介绍

Ling-App 是 [Ling-Agent](https://github.com/guyi-a/Ling-Agent) AI 助手的 Android 原生客户端，基于 Jetpack Compose + Material 3 构建，提供完整的移动端 AI 对话体验。

### 主要特性

- **流式对话** - SSE 实时流式输出，逐 token 显示 AI 回复
- **Markdown 渲染** - AI 消息支持 Markdown 格式（代码块、列表、表格等）
- **消息操作** - 长按消息可复制、删除、重新生成、编辑
- **多会话管理** - 侧边抽屉管理历史会话，支持搜索和按时间分组
- **工作区** - 文件上传、下载、预览，工具审批
- **个人资料** - 头像上传、用户名编辑、账号安全信息
- **完整设置** - 5 种主题颜色、4 档字体大小、深色模式、修改密码
- **Material 3** - 现代卡片式设计，多主题配色方案

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 |
| 网络 | Retrofit + kotlinx.serialization + OkHttp |
| 流式 | OkHttp EventSource (SSE) |
| 图片 | Coil (AsyncImage) |
| 本地存储 | DataStore Preferences |
| Markdown | compose-markdown (jeziellago) |
| 导航 | Navigation Compose |

---

## 快速开始

### 环境要求

- JDK 11+
- Android Studio Hedgehog (2023.1.1) 或更高版本
- Android SDK 36
- Ling-Agent 后端服务运行中（端口 9000）

### 构建运行

```bash
git clone https://github.com/guyi-a/Ling-App.git
cd Ling-App
./gradlew installDebug
```

或在 Android Studio 中打开项目并运行。

### 后端连接

- 模拟器自动连接 `http://10.0.2.2:9000`
- 真机需修改 `RetrofitClient.kt` 中的 `BASE_URL` 为局域网 IP

---

## 功能截图

| 欢迎页 | 对话页 | 设置页 |
|--------|--------|--------|
| 示例问题快速开始 | 流式 Markdown 渲染 | 主题/字体/深色模式 |

| 个人资料 | 侧边抽屉 | 工作区 |
|----------|----------|--------|
| 头像上传、编辑用户名 | 会话搜索、按时间分组 | 文件管理 |

---

## 项目结构

```
app/src/main/java/com/guyi/demo1/
├── MainActivity.kt                # 入口：主题设置应用
├── LingAgentApplication.kt        # Application：DI 容器初始化
├── data/
│   ├── api/                       # Retrofit API 接口
│   ├── local/                     # DataStore（Token、主题设置）
│   ├── network/                   # Retrofit/OkHttp/SSE 配置
│   ├── repository/                # 数据仓库层
│   └── AppContainer.kt            # 依赖注入容器
├── ui/
│   ├── screen/
│   │   ├── auth/                  # 登录、注册
│   │   ├── home/                  # 欢迎页
│   │   ├── chat/                  # 聊天（ChatScreen + ChatViewModel）
│   │   ├── profile/               # 个人资料、修改密码、账号安全
│   │   ├── settings/              # 设置（主题、字体、通知、密码、关于）
│   │   └── workspace/             # 工作区文件管理
│   ├── components/                # 通用组件
│   ├── navigation/                # NavGraph 路由
│   └── theme/                     # 多主题配色 + 字体缩放
```

---

## 后端 API

完整对接 Ling-Agent 后端所有 API：

- **认证**: 登录 / 注册 / 修改密码
- **用户**: 获取信息 / 更新资料 / 上传头像
- **会话**: 创建 / 列表 / 删除
- **聊天**: SSE 流式对话 / 停止生成 / 工具审批
- **消息**: 历史记录 / 删除 / 批量删除（编辑后）
- **工作区**: 上传 / 列表 / 下载 / 删除

详见后端 API 文档：`http://localhost:9000/docs`

---

## 许可证

MIT License

---

## 相关链接

- [Ling-Agent 后端](https://github.com/guyi-a/Ling-Agent)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)

---

<div align="center">

Made with Jetpack Compose

</div>
