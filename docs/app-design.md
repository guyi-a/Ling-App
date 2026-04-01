# Ling-Agent Android App 设计文档

## 📋 项目概述

### 项目名称
**Ling Agent** - 智能 AI 助手移动端

### 项目定位
一个连接 Ling-Agent 后端的 Android 客户端，提供流畅的 AI 对话体验、工具审批、文件管理等功能。

### 目标用户
- 需要 AI 辅助完成数据分析、报告生成的专业人士
- 希望通过移动端随时访问 AI 助手的用户
- 需要审批和监控 AI 工具执行的用户

---

## 🏗️ 技术架构

### 整体架构：Clean Architecture + MVVM

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (UI - Jetpack Compose + ViewModel)     │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│          Domain Layer                   │
│     (Use Cases + Domain Models)         │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│           Data Layer                    │
│  (Repository + Remote/Local DataSource) │
└─────────────────────────────────────────┘
```

### 技术栈选型

#### 核心框架
- **UI**: Jetpack Compose + Material3
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **异步处理**: Kotlin Coroutines + Flow
- **导航**: Compose Navigation

#### 网络层
- **HTTP 客户端**: Retrofit + OkHttp
- **SSE 支持**: OkHttp EventSource (okhttp-eventsource)
- **序列化**: Kotlinx Serialization

#### 数据持久化
- **Token 存储**: DataStore (Preferences)
- **消息缓存**: Room Database
- **文件缓存**: 本地文件系统

#### UI 增强
- **Markdown 渲染**: Compose-Markdown 或 Markwon
- **代码高亮**: Custom Composable + Syntax Highlighter
- **图片加载**: Coil

---

## 📦 模块设计

### 1. 认证模块 (Auth Module)

#### 功能
- 用户登录/注册
- JWT Token 管理
- 自动 Token 刷新
- 登出

#### 界面
```
LoginScreen
├── 用户名输入框
├── 密码输入框
├── 登录按钮
└── 注册跳转

RegisterScreen
├── 用户名输入框
├── 密码输入框
├── 确认密码输入框
└── 注册按钮
```

#### 数据流
```
LoginScreen → LoginViewModel → AuthUseCase
  → AuthRepository → RemoteDataSource (API)
  → DataStore (保存 Token)
```

---

### 2. 聊天模块 (Chat Module)

#### 功能
- **流式对话**: SSE 实时接收 AI 响应
- **消息展示**: 支持文本、Markdown、代码块
- **历史记录**: 本地缓存 + 云端同步
- **停止生成**: 中断 AI 响应
- **消息重试**: 失败消息可重新发送

#### 界面组件

##### ChatScreen (主聊天界面)
```
┌─────────────────────────────────┐
│  TopBar: 会话标题 | 菜单按钮     │
├─────────────────────────────────┤
│                                 │
│  MessageList (LazyColumn)       │
│  ├── UserMessage                │
│  │   └── 文本内容               │
│  ├── AssistantMessage           │
│  │   ├── Markdown 渲染          │
│  │   ├── 代码块高亮             │
│  │   └── 流式动画               │
│  └── ToolExecutionCard          │
│      ├── 工具名称                │
│      ├── 执行状态                │
│      └── 审批按钮 (如需要)       │
│                                 │
├─────────────────────────────────┤
│  InputBar                       │
│  ├── 文本输入框                 │
│  ├── 发送按钮                   │
│  └── 附件按钮 (未来扩展)         │
└─────────────────────────────────┘
```

##### MessageItem (消息卡片)
```kotlin
@Composable
fun UserMessageItem(message: Message)
@Composable
fun AssistantMessageItem(message: Message, isStreaming: Boolean)
@Composable
fun ToolExecutionItem(tool: ToolExecution)
```

#### SSE 流式处理流程

```
用户发送消息
  ↓
ChatViewModel.sendMessage()
  ↓
建立 SSE 连接 (/api/chat/stream)
  ↓
接收事件流:
  - event: session     → 更新 session_id
  - event: token       → 追加文字到 UI
  - event: model_start → 显示 AI 思考动画
  - event: tool_start  → 显示工具执行卡片
  - event: approval_required → 弹出审批对话框
  - event: tool_end    → 更新工具状态
  - event: done        → 保存完整消息到数据库
  - event: error       → 显示错误提示
```

#### ViewModel 设计

```kotlin
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val stopGenerationUseCase: StopGenerationUseCase,
    private val approveToolUseCase: ApproveToolUseCase
) : ViewModel() {

    // 当前会话 ID
    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    // 消息列表
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // 正在流式输出的消息
    private val _streamingMessage = MutableStateFlow<String>("")
    val streamingMessage: StateFlow<String> = _streamingMessage.asStateFlow()

    // 是否正在生成
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // 待审批的工具
    private val _pendingApproval = MutableStateFlow<ToolApprovalRequest?>(null)
    val pendingApproval: StateFlow<ToolApprovalRequest?> = _pendingApproval.asStateFlow()

    fun sendMessage(text: String) { ... }
    fun stopGeneration() { ... }
    fun approveTool(requestId: String, approved: Boolean) { ... }
    fun loadHistory(sessionId: String) { ... }
}
```

---

### 3. 会话管理模块 (Session Module)

#### 功能
- 会话列表展示
- 创建新会话
- 删除会话
- 会话搜索
- 会话重命名

#### 界面

##### SessionListScreen
```
┌─────────────────────────────────┐
│  TopBar: "对话历史"              │
│  [搜索框] [新建按钮]             │
├─────────────────────────────────┤
│  SessionList (LazyColumn)       │
│  ├── SessionCard                │
│  │   ├── 标题                   │
│  │   ├── 最后一条消息预览       │
│  │   ├── 时间戳                 │
│  │   └── 删除按钮               │
│  ├── SessionCard                │
│  └── ...                        │
└─────────────────────────────────┘
```

#### 数据模型

```kotlin
data class Session(
    val sessionId: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String,
    val messageCount: Int,
    val lastMessage: String?
)
```

---

### 4. 工具审批模块 (Approval Module)

#### 功能
- 实时接收工具审批请求
- 展示工具详情（名称、参数）
- 允许/拒绝操作
- 60 秒倒计时

#### 界面

##### ApprovalDialog (审批对话框)
```
┌─────────────────────────────────┐
│  ⚠️ 工具审批请求                │
├─────────────────────────────────┤
│  工具名称: python_repl           │
│                                 │
│  执行内容:                       │
│  ┌─────────────────────────┐   │
│  │ import pandas as pd     │   │
│  │ df.describe()           │   │
│  └─────────────────────────┘   │
│                                 │
│  ⏱️ 剩余时间: 45 秒             │
│                                 │
│  [拒绝]              [允许]     │
└─────────────────────────────────┘
```

#### ViewModel

```kotlin
class ApprovalViewModel @Inject constructor(
    private val approveToolUseCase: ApproveToolUseCase
) : ViewModel() {

    private val _currentRequest = MutableStateFlow<ToolApprovalRequest?>(null)
    val currentRequest: StateFlow<ToolApprovalRequest?> = _currentRequest.asStateFlow()

    private val _timeLeft = MutableStateFlow(60)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    fun approve(requestId: String) { ... }
    fun reject(requestId: String) { ... }
}
```

---

### 5. 工作区模块 (Workspace Module)

#### 功能
- 查看会话工作区文件
- 上传文件
- 下载文件
- 预览文件（图片、PDF、文本）
- 删除文件

#### 界面

##### WorkspaceScreen
```
┌─────────────────────────────────┐
│  TopBar: "工作区" | [上传]       │
├─────────────────────────────────┤
│  Tabs: [上传] [输出]             │
├─────────────────────────────────┤
│  FileList (LazyColumn)          │
│  ├── FileItem                   │
│  │   ├── 📄 Icon                │
│  │   ├── 文件名                 │
│  │   ├── 大小                   │
│  │   ├── 时间                   │
│  │   └── [预览] [下载] [删除]   │
│  └── ...                        │
└─────────────────────────────────┘
```

#### 文件类型支持
- **图片**: PNG, JPG (Coil 加载)
- **PDF**: 系统 Intent 打开
- **文本**: CSV, TXT (内置查看器)
- **其他**: 下载后打开

---

### 6. 用户设置模块 (Settings Module)

#### 功能
- 查看用户信息
- 修改密码
- 服务器地址配置
- 主题切换（亮色/暗色）
- 清除缓存
- 关于页面

#### 界面

##### SettingsScreen
```
┌─────────────────────────────────┐
│  TopBar: "设置"                  │
├─────────────────────────────────┤
│  用户信息                        │
│  ├── 头像                        │
│  ├── 用户名                      │
│  └── 最后活跃时间                │
│                                 │
│  通用设置                        │
│  ├── 🌙 深色模式 [开关]         │
│  ├── 🔔 消息通知 [开关]         │
│  └── 🌐 服务器地址              │
│                                 │
│  账号管理                        │
│  ├── 🔑 修改密码                │
│  ├── 🗑️ 清除缓存                │
│  └── 🚪 登出                    │
│                                 │
│  关于                           │
│  ├── ℹ️ 版本信息                │
│  └── 📄 开源许可                │
└─────────────────────────────────┘
```

---

## 🎨 UI/UX 设计

### 设计原则
1. **简洁优先**: 减少视觉噪音，突出核心内容
2. **响应迅速**: 流式输出提供即时反馈
3. **清晰层次**: 用户消息 vs AI 消息明确区分
4. **安全可控**: 工具审批流程透明易懂

### 配色方案

#### 亮色主题
```kotlin
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF625B71),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    // ...
)
```

#### 暗色主题
```kotlin
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    secondary = Color(0xFFCCC2DC),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    // ...
)
```

### 消息气泡设计

```
用户消息:
┌─────────────────────────┐
│  Hello, AI!             │  ← 右对齐，紫色背景
└─────────────────────────┘

AI 消息:
┌─────────────────────────┐
│  你好！有什么可以帮你？  │  ← 左对齐，灰色背景
│                         │
│  ```python              │  ← 代码块特殊样式
│  print("Hello")         │
│  ```                    │
└─────────────────────────┘
```

---

## 🔌 API 集成方案

### API 基础配置

```kotlin
object ApiConfig {
    const val BASE_URL = "http://localhost:9000" // 可配置
    const val TIMEOUT_CONNECT = 30L
    const val TIMEOUT_READ = 60L
    const val TIMEOUT_WRITE = 60L
}
```

### Retrofit 接口定义

```kotlin
interface LingAgentApi {

    // 认证
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // 会话
    @GET("/api/session")
    suspend fun getSessions(): List<Session>

    @POST("/api/session")
    suspend fun createSession(@Body request: CreateSessionRequest): Session

    @DELETE("/api/session/{id}")
    suspend fun deleteSession(@Path("id") sessionId: String)

    // 聊天（非流式，备用）
    @POST("/api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    // 工具审批
    @POST("/api/chat/approve")
    suspend fun approveTool(@Body request: ApprovalRequest): ApprovalResponse

    // 停止生成
    @POST("/api/chat/{sessionId}/stop")
    suspend fun stopGeneration(@Path("sessionId") sessionId: String)

    // 历史记录
    @GET("/api/chat/{sessionId}/history")
    suspend fun getHistory(
        @Path("sessionId") sessionId: String,
        @Query("limit") limit: Int = 50
    ): HistoryResponse

    // 工作区
    @GET("/api/workspace/{sessionId}/files")
    suspend fun listFiles(@Path("sessionId") sessionId: String): FileListResponse

    @Multipart
    @POST("/api/workspace/{sessionId}/upload")
    suspend fun uploadFile(
        @Path("sessionId") sessionId: String,
        @Part file: MultipartBody.Part
    ): UploadResponse

    @GET("/api/workspace/{sessionId}/download/{filename}")
    suspend fun downloadFile(
        @Path("sessionId") sessionId: String,
        @Path("filename") filename: String
    ): ResponseBody
}
```

### SSE 流式客户端

```kotlin
class SseClient(private val okHttpClient: OkHttpClient) {

    fun streamChat(
        token: String,
        request: ChatRequest
    ): Flow<SseEvent> = callbackFlow {
        val eventSource = EventSources.createFactory(okHttpClient)
            .newEventSource(
                request = Request.Builder()
                    .url("${ApiConfig.BASE_URL}/api/chat/stream")
                    .post(request.toJsonBody())
                    .header("Authorization", "Bearer $token")
                    .build(),
                listener = object : EventSourceListener() {
                    override fun onEvent(
                        eventSource: EventSource,
                        id: String?,
                        type: String?,
                        data: String
                    ) {
                        val event = when (type) {
                            "session" -> SseEvent.Session(Json.decodeFromString(data))
                            "token" -> SseEvent.Token(Json.decodeFromString(data))
                            "model_start" -> SseEvent.ModelStart
                            "tool_start" -> SseEvent.ToolStart(Json.decodeFromString(data))
                            "tool_end" -> SseEvent.ToolEnd(Json.decodeFromString(data))
                            "approval_required" -> SseEvent.ApprovalRequired(Json.decodeFromString(data))
                            "done" -> SseEvent.Done
                            "error" -> SseEvent.Error(Json.decodeFromString(data))
                            else -> return
                        }
                        trySend(event)
                    }

                    override fun onFailure(
                        eventSource: EventSource,
                        t: Throwable?,
                        response: Response?
                    ) {
                        close(t)
                    }
                }
            )

        awaitClose { eventSource.cancel() }
    }
}
```

---

## 📊 数据库设计

### Room Entities

```kotlin
// 缓存的消息
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val sessionId: String,
    val role: String, // "user" | "assistant"
    val content: String,
    val createdAt: Long,
    val isLocal: Boolean = false // 是否仅本地（未发送成功）
)

// 缓存的会话
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)
```

### DAO

```kotlin
@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: String)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)
}
```

---

## 🔐 安全设计

### Token 管理

```kotlin
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")

    suspend fun saveToken(token: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data.map { it[TOKEN_KEY] }
    }

    suspend fun clearToken() {
        dataStore.edit { it.remove(TOKEN_KEY) }
    }
}
```

### 自动 Token 刷新

```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.getToken().first() }

        val request = chain.request().newBuilder().apply {
            token?.let { addHeader("Authorization", "Bearer $it") }
        }.build()

        val response = chain.proceed(request)

        // 如果返回 401，清除 token 并跳转登录
        if (response.code == 401) {
            runBlocking { tokenManager.clearToken() }
            // 发送广播通知登出
        }

        return response
    }
}
```

---

## 🚀 开发计划

### Phase 1: 基础框架（1-2 周）
- [x] 项目初始化
- [ ] 网络层搭建（Retrofit + OkHttp）
- [ ] 数据库搭建（Room）
- [ ] 依赖注入配置（Hilt）
- [ ] 导航配置（Compose Navigation）
- [ ] 主题配置（Material3 + 暗色模式）

### Phase 2: 认证模块（3-5 天）
- [ ] 登录界面
- [ ] 注册界面
- [ ] Token 存储与自动注入
- [ ] 登出功能

### Phase 3: 聊天核心（1.5-2 周）
- [ ] 聊天界面布局
- [ ] 消息列表展示
- [ ] 输入框与发送
- [ ] SSE 流式接收
- [ ] Markdown 渲染
- [ ] 代码高亮
- [ ] 停止生成功能

### Phase 4: 会话管理（3-5 天）
- [ ] 会话列表界面
- [ ] 新建/删除会话
- [ ] 会话搜索
- [ ] 会话重命名

### Phase 5: 工具审批（3-5 天）
- [ ] 审批对话框
- [ ] 倒计时逻辑
- [ ] 允许/拒绝 API 调用
- [ ] 超时处理

### Phase 6: 工作区管理（5-7 天）
- [ ] 文件列表界面
- [ ] 文件上传
- [ ] 文件下载
- [ ] 文件预览（图片/PDF/文本）
- [ ] 文件删除

### Phase 7: 优化与测试（1 周）
- [ ] 性能优化
- [ ] 错误处理完善
- [ ] UI/UX 细节打磨
- [ ] 单元测试
- [ ] 集成测试

---

## 📝 注意事项

### 性能优化
1. **消息分页**: 历史消息分页加载，避免一次加载过多
2. **图片压缩**: 上传前压缩图片
3. **缓存策略**: 合理使用 Room 缓存，减少网络请求
4. **LazyColumn**: 使用虚拟滚动，提升长列表性能

### 错误处理
1. **网络异常**: 友好的错误提示 + 重试按钮
2. **Token 过期**: 自动跳转登录
3. **SSE 断开**: 自动重连机制
4. **文件上传失败**: 显示失败原因，支持重试

### 用户体验
1. **加载状态**: 所有异步操作显示 Loading 指示器
2. **空状态**: 空会话/空消息时显示友好提示
3. **流式动画**: AI 响应时显示打字机效果
4. **震动反馈**: 审批请求到达时震动提醒

---

## 🔄 未来扩展

### 多模态输入
- 支持语音输入
- 支持图片上传并分析
- 支持拍照后识别

### 离线功能
- 离线查看历史消息
- 消息队列（联网后自动发送）

### 高级功能
- Agent 技能市场
- 自定义提示词模板
- 多 Agent 协作
- 数据可视化增强

---

## 📚 参考资料

- [Jetpack Compose 官方文档](https://developer.android.com/jetpack/compose)
- [Hilt 依赖注入](https://developer.android.com/training/dependency-injection/hilt-android)
- [OkHttp SSE](https://github.com/launchdarkly/okhttp-eventsource)
- [Room 数据库](https://developer.android.com/training/data-storage/room)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)

---

## 📞 联系方式

如有问题或建议，请联系开发团队。

**最后更新**: 2026-04-01
