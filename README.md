# Ling Agent - Android Client

<div align="center">

🤖 智能 AI 对话助手 Android 应用

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Latest-orange.svg)](https://m3.material.io/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-brightgreen.svg)](https://developer.android.com)

</div>

---

## 📱 项目介绍

基于 Jetpack Compose 和 Material Design 3 设计规范构建的现代化 Android 对话应用，提供流畅的用户体验和美观的卡片式界面。

### 主要特性

- 🎨 **Material 3 设计** - 遵循最新的 Material Design 3 设计规范
- 💬 **实时对话** - 流畅的消息发送和接收体验
- 📝 **会话管理** - 侧边抽屉管理历史会话
- 🎯 **快速开始** - 示例问题快速启动对话
- 🔄 **优雅动画** - 流畅的页面转场和交互动画

---

## 🛠️ 技术栈

- **Kotlin** 2.2.10
- **Jetpack Compose** - 声明式 UI 框架
- **Material 3** - Google 设计系统
- **Navigation Compose** - 页面导航管理
- **Gradle** 9.3.1

---

## 🚀 快速开始

### 环境要求

- JDK 11+
- Android Studio Hedgehog (2023.1.1) 或更高版本
- Android SDK 36

### 构建运行

```bash
# 克隆项目
git clone https://github.com/guyi-a/Ling-App.git
cd Ling-App

# 编译项目
./gradlew build

# 安装到设备
./gradlew installDebug
```

或者直接在 Android Studio 中打开项目并运行。

---

## 📁 项目结构

```
app/src/main/java/com/guyi/demo1/
├── MainActivity.kt                    # 应用入口
├── ui/
│   ├── theme/                        # 主题配置
│   ├── navigation/
│   │   └── NavGraph.kt               # 导航路由
│   ├── screen/
│   │   ├── auth/                     # 登录页面
│   │   ├── home/                     # 欢迎页面
│   │   └── chat/                     # 聊天页面
│   └── components/                   # 通用组件
└── ...
```

---

## 🎨 设计规范

### UI 风格

- **卡片式设计** - 消息和 UI 元素使用卡片包装
- **圆角设计** - 12-24dp 圆角，视觉柔和
- **层次分明** - 使用 elevation 和颜色区分层级

### 颜色主题

- Primary - 主要操作和强调
- PrimaryContainer - 用户消息背景
- Surface - 卡片背景
- SurfaceVariant - AI 消息背景

---

## 📄 许可证

MIT License

---

## 🔗 相关链接

- [Jetpack Compose 文档](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Kotlin 官方文档](https://kotlinlang.org/docs/home.html)

---

<div align="center">

Made with ❤️ using Jetpack Compose

</div>
