# Ling-Agent Android App 设计文档（简化版）

## 📋 项目概述

### 定位
**纯前端** Android 客户端，连接 Ling-Agent 后端，**不负责数据持久化**。

### 核心原则
- 所有业务数据存储在**后端**（Ling-Agent）
- App 只做**轻量级缓存**（加速显示 + 离线查看）
- 用户数据的唯一真实来源（Single Source of Truth）= **后端数据库**

---

## 🏗️ 技术架构

### 整体架构：MVVM

```
┌─────────────────────────────────────────┐
│         UI Layer (Compose)              │
│    (界面展示 + 用户交互)                 │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│       ViewModel Layer                   │
│    (状态管理 + 业务逻辑调用)             │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│       Repository Layer                  │
│    (网络请求 + 本地缓存协调)             │
└─────┬───────────────────────────┬───────┘
      │                           │
┌─────▼────────┐         ┌────────▼───────┐
│ Remote API   │         │ Local Cache    │
│ (Retrofit)   │         │ (轻量级存储)    │
└──────────────┘         └────────────────┘
```

---

## 📦 简化模块设计

### 1. 网络层（核心）

#### Retrofit API 接口
```kotlin
interface LingAgentApi {
    // 认证
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // 聊天（流式通过 SSE，非流式备用）
    @POST("/api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    // 历史记录
    @GET("/api/chat/{sessionId}/history")
    suspend fun getHistory(@Path("sessionId") id: String): HistoryResponse

    // 会话管理
    @GET("/api/session")
    suspend fun getSessions(): List<SessionResponse>

    @POST("/api/session")
    suspend fun createSession(@Body request: CreateSessionRequest): SessionResponse

    // 工具审批
    @POST("/api/chat/approve")
    suspend fun approveTool(@Body request: ApprovalRequest): ApprovalResponse

    // 停止生成
    @POST("/api/chat/{sessionId}/stop")
    suspend fun stopGeneration(@Path("sessionId") id: String)

    // 工作区
    @GET("/api/workspace/{sessionId}/files")
    suspend fun listFiles(@Path("sessionId") id: String): FileListResponse

    @Multipart
    @POST("/api/workspace/{sessionId}/upload")
    suspend fun uploadFile(
        @Path("sessionId") id: String,
        @Part file: MultipartBody.Part
    ): UploadResponse
}
```

#### SSE 客户端（流式对话）
```kotlin
class SseClient(private val okHttpClient: OkHttpClient) {
    fun streamChat(token: String, request: ChatRequest): Flow<SseEvent> {
        // 建立 SSE 连接，实时接收 token/tool/approval 事件
    }
}
```

---

### 2. 本地存储（最小化设计）

#### 仅存储 3 类数据：

##### (1) Token 存储（必需）
```kotlin
// 使用 DataStore 保存 JWT
class TokenManager(private val dataStore: DataStore<Preferences>) {
    suspend fun saveToken(token: String)
    fun getToken(): Flow<String?>
    suspend fun clearToken()
}
```

##### (2) 用户设置（必需）
```kotlin
// 使用 DataStore 保存设置
class SettingsManager(private val dataStore: DataStore<Preferences>) {
    suspend fun setDarkMode(enabled: Boolean)
    fun getDarkMode(): Flow<Boolean>

    suspend fun setServerUrl(url: String)
    fun getServerUrl(): Flow<String>
}
```

##### (3) 消息缓存（可选，仅用于加速显示）
```kotlin
// 使用 Room 缓存最近的消息（可选功能）
@Entity(tableName = "cached_messages")
data class CachedMessage(
    @PrimaryKey val messageId: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val timestamp: Long
)

@Dao
interface MessageCacheDao {
    @Query("SELECT * FROM cached_messages WHERE sessionId = :id ORDER BY timestamp")
    fun getMessages(id: String): Flow<List<CachedMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheMessages(messages: List<CachedMessage>)
}
```

**注意**：缓存是**可选**的，如果不需要离线查看，可以完全不用数据库！

---

### 3. Repository 层（数据协调）

```kotlin
class ChatRepository(
    private val api: LingAgentApi,
    private val sseClient: SseClient,
    private val messageCache: MessageCacheDao? = null // 可选
) {
    // 获取历史消息
    suspend fun getHistory(sessionId: String): Result<List<Message>> {
        return try {
            // 1. 先从缓存读取（如果有缓存）
            messageCache?.getMessages(sessionId)?.first()?.let { cached ->
                if (cached.isNotEmpty()) {
                    emit(cached.toDomainModel()) // 快速显示缓存
                }
            }

            // 2. 从服务器获取最新数据
            val response = api.getHistory(sessionId)
            val messages = response.messages.map { it.toDomainModel() }

            // 3. 更新缓存（可选）
            messageCache?.cacheMessages(messages.toEntityModel())

            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 流式发送消息
    fun streamMessage(text: String, sessionId: String?): Flow<SseEvent> {
        return sseClient.streamChat(
            token = tokenManager.getToken(),
            request = ChatRequest(message = text, sessionId = sessionId)
        )
    }
}
```

---

## 🎨 UI 层设计

### 主要界面

#### 1. 登录界面 (LoginScreen)
```
功能：
- 用户名/密码输入
- 调用 /api/auth/login
- 保存 JWT Token 到 DataStore
```

#### 2. 聊天界面 (ChatScreen)
```
功能：
- 展示消息列表（从后端获取）
- 发送消息（SSE 流式接收响应）
- 显示工具执行状态
- 弹出审批对话框
```

#### 3. 会话列表 (SessionListScreen)
```
功能：
- 展示所有会话（从后端获取）
- 新建会话
- 删除会话
```

#### 4. 工作区 (WorkspaceScreen)
```
功能：
- 查看当前会话的文件（从后端获取）
- 上传文件到后端
- 下载文件到本地
```

---

## 📊 数据流示例

### 示例 1：用户发送消息

```
1. 用户在 ChatScreen 输入 "帮我分析数据"
   ↓
2. ChatViewModel.sendMessage()
   ↓
3. ChatRepository.streamMessage()
   → 调用 SSE Client
   ↓
4. 后端接收消息 → Agent 处理 → 流式返回
   ↓
5. App 接收 SSE 事件:
   - event: token → 追加文字到 UI
   - event: tool_start → 显示 "正在执行工具..."
   - event: done → 完成
   ↓
6. 完整消息已经保存在**后端数据库**
   App 不需要保存（或者只缓存一份副本）
```

### 示例 2：查看历史消息

```
1. 用户进入某个会话
   ↓
2. ChatViewModel.loadHistory(sessionId)
   ↓
3. ChatRepository.getHistory()
   → 调用 GET /api/chat/{sessionId}/history
   ↓
4. 后端从数据库读取消息 → 返回给 App
   ↓
5. App 显示在 UI
   （可选：同时缓存到 Room，下次更快显示）
```

---

## 🔧 技术栈（简化版）

### 必需依赖
```kotlin
dependencies {
    // UI
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // 网络
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.launchdarkly:okhttp-eventsource:4.1.1") // SSE

    // 依赖注入
    implementation("com.google.dagger:hilt-android:2.51.1")

    // 本地存储（Token + 设置）
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // 图片加载
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Markdown 渲染
    implementation("com.github.jeziellago:compose-markdown:0.5.4")
}
```

### 可选依赖（如果要做消息缓存）
```kotlin
dependencies {
    // Room（仅用于缓存）
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}
```

---

## 📁 项目结构（简化版）

```
com.guyi.demo1/
├── data/
│   ├── remote/                  # 网络层
│   │   ├── api/
│   │   │   ├── LingAgentApi.kt
│   │   │   └── SseClient.kt
│   │   └── dto/                 # API 数据模型
│   │       ├── LoginRequest.kt
│   │       ├── ChatRequest.kt
│   │       └── ChatResponse.kt
│   │
│   ├── local/                   # 本地存储（最小化）
│   │   ├── TokenManager.kt      # JWT 存储
│   │   └── SettingsManager.kt   # 用户设置
│   │
│   └── repository/              # 数据协调
│       ├── AuthRepository.kt
│       ├── ChatRepository.kt
│       └── SessionRepository.kt
│
├── domain/                      # 领域模型
│   └── model/
│       ├── User.kt
│       ├── Message.kt
│       └── Session.kt
│
├── ui/                          # UI 层
│   ├── screen/
│   │   ├── login/
│   │   │   ├── LoginScreen.kt
│   │   │   └── LoginViewModel.kt
│   │   ├── chat/
│   │   │   ├── ChatScreen.kt
│   │   │   ├── ChatViewModel.kt
│   │   │   └── component/
│   │   │       ├── MessageList.kt
│   │   │       └── ChatInputBar.kt
│   │   └── session/
│   │       ├── SessionListScreen.kt
│   │       └── SessionListViewModel.kt
│   │
│   ├── theme/
│   └── component/               # 通用组件
│
└── MainActivity.kt
```

---

## ✅ 总结：App 的职责边界

### App 应该做的 ✅
- 展示 UI
- 发送网络请求
- 接收 SSE 流式响应
- 缓存 Token（避免重复登录）
- 缓存最近消息（可选，加速显示）

### App 不应该做 ❌
- 持久化业务数据（用户、会话、消息）
- 复杂的数据查询逻辑
- 数据同步逻辑
- 作为"数据库"使用

---

## 🚀 开发优先级

### Phase 1: 核心功能（纯网络版）
1. 登录界面 + Token 存储
2. 聊天界面 + SSE 流式输出
3. 会话列表

**此时完全不需要 Room 数据库！**

### Phase 2: 优化体验（可选）
4. 添加消息缓存（Room）
5. 离线查看历史消息

---

**这样理解对吗？你觉得这个简化版的设计如何？**
