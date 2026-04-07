# Ling-App 开发计划

> 📋 从 UI 完成到全功能客户端的实施路线图
> 
> **创建日期**: 2026-04-07  
> **当前阶段**: 第一阶段 - 网络层基础搭建  
> **预计完成**: 分 5 个阶段，按优先级逐步实现

---

## 🎯 总体目标

将已完成的 UI 界面与后端 Ling-Agent API 对接，实现完整的移动端 AI 对话体验。

### 后端 API 基础信息
- **地址**: `http://localhost:9000` (开发) / `https://your-domain.com` (生产)
- **认证方式**: JWT Bearer Token
- **主要技术**: SSE 流式输出、文件上传、工具审批

---

## 📊 开发阶段划分

### ✅ 阶段 0: UI 完成（已完成）
- [x] 所有页面界面
- [x] 通用组件库
- [x] 导航系统
- [x] Material 3 设计规范

### 🚀 阶段 1: 网络层基础（当前阶段）
**目标**: 搭建完整的网络请求架构

#### 任务 1.1: 数据模型定义 ⭐ 优先
**文件位置**: `app/src/main/java/com/guyi/demo1/data/model/`

需要创建的模型类：
```kotlin
// 认证相关
- LoginRequest.kt
- LoginResponse.kt  
- RegisterRequest.kt
- User.kt

// 会话相关
- Session.kt
- SessionListResponse.kt
- CreateSessionRequest.kt

// 消息相关
- Message.kt
- MessagePart.kt  // 文本、工具调用
- SendMessageRequest.kt
- MessageHistory.kt

// 工作区相关
- WorkspaceFile.kt
- FileUploadResponse.kt
- FileListResponse.kt

// 工具审批相关
- ToolApprovalRequest.kt
- ApprovalResponse.kt

// SSE 事件
- SSEEvent.kt
- TokenEvent.kt
- ToolStartEvent.kt
- ToolEndEvent.kt
- ApprovalRequiredEvent.kt

// 通用
- ApiResponse.kt  // 统一响应包装
- ApiError.kt
```

**实现细节**:
```kotlin
// 示例：Message.kt
@Serializable
data class Message(
    val id: String,
    val messageId: String? = null,
    val role: String,  // "user" | "assistant"
    val parts: List<MessagePart>,
    val isStreaming: Boolean = false,
    val approvalRequest: ApprovalRequest? = null
)

@Serializable
data class MessagePart(
    val type: String,  // "text" | "tool"
    val content: String? = null,
    val toolName: String? = null,
    val toolStatus: String? = null  // "pending" | "done" | "rejected"
)
```

---

#### 任务 1.2: 网络层配置 ⭐ 优先
**文件位置**: `app/src/main/java/com/guyi/demo1/data/network/`

**技术选型**: 
- **Retrofit** - REST API
- **OkHttp** - HTTP 客户端 + SSE
- **Kotlinx Serialization** - JSON 序列化

**需要添加的依赖**:
```kotlin
// app/build.gradle.kts
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.9.0")
    
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // DataStore (存储 Token)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

**需要创建的类**:
```kotlin
// RetrofitClient.kt - Retrofit 单例
- baseUrl 配置
- JSON 序列化配置
- 超时配置

// AuthInterceptor.kt - JWT Token 拦截器
- 自动添加 Authorization header
- Token 过期检测
- 自动刷新 Token

// LoggingInterceptor.kt - 日志拦截器
- 请求/响应日志
- 仅 Debug 模式启用

// SSEManager.kt - SSE 流式事件管理器
- OkHttp SSE 客户端
- 事件解析（event: token/tool_start/tool_end/done）
- 错误处理和重连

// ApiConfig.kt - API 配置
object ApiConfig {
    const val BASE_URL_DEV = "http://10.0.2.2:9000"  // Android 模拟器访问本机
    const val BASE_URL_PROD = "https://your-domain.com"
    const val TIMEOUT_CONNECT = 15L
    const val TIMEOUT_READ = 30L
    const val TIMEOUT_WRITE = 30L
}
```

---

#### 任务 1.3: API 接口定义
**文件位置**: `app/src/main/java/com/guyi/demo1/data/api/`

需要创建的接口：
```kotlin
// AuthApi.kt - 认证接口
interface AuthApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse
    
    @GET("/api/users/me")
    suspend fun getCurrentUser(): User
}

// SessionApi.kt - 会话接口
interface SessionApi {
    @GET("/api/sessions/")
    suspend fun getSessions(@Query("limit") limit: Int = 100): List<Session>
    
    @POST("/api/sessions/")
    suspend fun createSession(): Session
    
    @DELETE("/api/sessions/{session_id}")
    suspend fun deleteSession(@Path("session_id") sessionId: String)
}

// ChatApi.kt - 聊天接口
interface ChatApi {
    // 注意：stream 接口不用 Retrofit，用 SSEManager
    
    @POST("/api/chat/approve")
    suspend fun approveToolUse(@Body request: ApprovalRequest): ApiResponse<Unit>
    
    @POST("/api/chat/{session_id}/stop")
    suspend fun stopGeneration(@Path("session_id") sessionId: String): ApiResponse<Unit>
}

// MessageApi.kt - 消息接口  
interface MessageApi {
    @GET("/api/messages/session/{session_id}/history")
    suspend fun getHistory(
        @Path("session_id") sessionId: String,
        @Query("limit") limit: Int = 50
    ): MessageHistory
    
    @DELETE("/api/messages/{message_id}")
    suspend fun deleteMessage(@Path("message_id") messageId: String): ApiResponse<Unit>
}

// WorkspaceApi.kt - 工作区接口
interface WorkspaceApi {
    @Multipart
    @POST("/api/workspace/{session_id}/upload")
    suspend fun uploadFile(
        @Path("session_id") sessionId: String,
        @Part file: MultipartBody.Part
    ): FileUploadResponse
    
    @GET("/api/workspace/{session_id}/files")
    suspend fun getFiles(@Path("session_id") sessionId: String): FileListResponse
    
    @GET("/api/workspace/{session_id}/files/{folder}/{filename}")
    suspend fun downloadFile(
        @Path("session_id") sessionId: String,
        @Path("folder") folder: String,
        @Path("filename") filename: String
    ): ResponseBody
    
    @DELETE("/api/workspace/{session_id}/files/{folder}/{filename}")
    suspend fun deleteFile(
        @Path("session_id") sessionId: String,
        @Path("folder") folder: String,
        @Path("filename") filename: String
    ): ApiResponse<Unit>
}
```

---

#### 任务 1.4: Token 管理
**文件位置**: `app/src/main/java/com/guyi/demo1/data/local/`

```kotlin
// TokenManager.kt - JWT Token 存储和管理
class TokenManager(context: Context) {
    private val dataStore = context.dataStore
    
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    suspend fun isTokenValid(): Boolean
}

// 使用 DataStore 存储（加密推荐使用 EncryptedSharedPreferences）
```

---

### 🔐 阶段 2: 认证功能实现

#### 任务 2.1: Repository 层
**文件位置**: `app/src/main/java/com/guyi/demo1/data/repository/`

```kotlin
// AuthRepository.kt
class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun register(username: String, password: String): Result<User>
    suspend fun getCurrentUser(): Result<User>
    suspend fun logout()
}
```

#### 任务 2.2: ViewModel
**文件位置**: `app/src/main/java/com/guyi/demo1/ui/screen/auth/`

```kotlin
// LoginViewModel.kt
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login(username: String, password: String)
    fun register(username: String, password: String)
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
```

#### 任务 2.3: 对接 LoginScreen
修改 `LoginScreen.kt`，将假数据替换为真实 API 调用。

---

### 💬 阶段 3: 聊天功能实现

#### 任务 3.1: Session Repository & ViewModel
```kotlin
// SessionRepository.kt
class SessionRepository(
    private val sessionApi: SessionApi
) {
    suspend fun getSessions(): Result<List<Session>>
    suspend fun createSession(): Result<Session>
    suspend fun deleteSession(sessionId: String): Result<Unit>
}

// SessionViewModel.kt
class SessionViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()
    
    fun loadSessions()
    fun createSession()
    fun deleteSession(sessionId: String)
}
```

#### 任务 3.2: Message Repository & ViewModel
```kotlin
// MessageRepository.kt
class MessageRepository(
    private val messageApi: MessageApi,
    private val sseManager: SSEManager
) {
    suspend fun getHistory(sessionId: String): Result<MessageHistory>
    suspend fun sendMessage(sessionId: String, message: String, attachments: List<String>?): Flow<SSEEvent>
    suspend fun stopGeneration(sessionId: String): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
}

// ChatViewModel.kt
class ChatViewModel(
    private val messageRepository: MessageRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()
    
    fun loadHistory(sessionId: String)
    fun sendMessage(message: String, attachments: List<String>?)
    fun stopGeneration()
}
```

#### 任务 3.3: SSE 流式对话实现 ⭐ 难点
**关键技术**: OkHttp EventSource / SSE 解析

```kotlin
// SSEManager.kt 核心逻辑
class SSEManager(private val client: OkHttpClient) {
    fun connect(
        url: String,
        headers: Map<String, String>,
        onEvent: (SSEEvent) -> Unit,
        onError: (Throwable) -> Unit
    ): SSEConnection
    
    fun parseEvent(eventType: String, data: String): SSEEvent?
}

// SSE 事件类型
sealed class SSEEvent {
    data class Session(val sessionId: String, val isNew: Boolean) : SSEEvent()
    data class Token(val text: String) : SSEEvent()
    data class ToolStart(val toolName: String) : SSEEvent()
    data class ToolEnd(val toolName: String) : SSEEvent()
    data class ApprovalRequired(val requestId: String, val toolName: String, val toolInput: Map<String, Any>) : SSEEvent()
    data class Done(val assistantMessageId: String) : SSEEvent()
    data class Error(val message: String) : SSEEvent()
    object Cancelled : SSEEvent()
}
```

#### 任务 3.4: 对接 ChatScreen
- 加载历史消息
- 实时流式输出
- 工具调用显示
- 停止生成功能

---

### 📂 阶段 4: 工作区功能实现

#### 任务 4.1: Workspace Repository & ViewModel
```kotlin
// WorkspaceRepository.kt
class WorkspaceRepository(
    private val workspaceApi: WorkspaceApi
) {
    suspend fun uploadFile(sessionId: String, uri: Uri): Result<WorkspaceFile>
    suspend fun getFiles(sessionId: String): Result<FileListResponse>
    suspend fun downloadFile(sessionId: String, file: WorkspaceFile): Result<File>
    suspend fun deleteFile(sessionId: String, file: WorkspaceFile): Result<Unit>
}

// WorkspaceViewModel.kt
class WorkspaceViewModel(
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {
    private val _uploadFiles = MutableStateFlow<List<WorkspaceFile>>(emptyList())
    val uploadFiles: StateFlow<List<WorkspaceFile>> = _uploadFiles.asStateFlow()
    
    private val _outputFiles = MutableStateFlow<List<WorkspaceFile>>(emptyList())
    val outputFiles: StateFlow<List<WorkspaceFile>> = _outputFiles.asStateFlow()
    
    fun loadFiles(sessionId: String)
    fun uploadFile(sessionId: String, uri: Uri)
    fun downloadFile(file: WorkspaceFile)
    fun deleteFile(file: WorkspaceFile)
}
```

#### 任务 4.2: 对接 WorkspaceScreen
- 文件列表加载
- 文件上传（选择文件 → 上传 → 刷新列表）
- 文件下载（下载到本地）
- 文件删除

---

### 🔧 阶段 5: 工具审批 & 高级功能

#### 任务 5.1: 工具审批对接
```kotlin
// ApprovalRepository.kt
class ApprovalRepository(
    private val chatApi: ChatApi
) {
    suspend fun approveToolUse(requestId: String, approved: Boolean): Result<Unit>
}

// 在 ChatViewModel 中添加
fun approveToolUse(requestId: String, approved: Boolean)
```

#### 任务 5.2: NavigationDrawer 真实数据
- 加载会话列表
- 搜索过滤
- 会话分组（今天/昨天/本周/更早）
- 删除会话

#### 任务 5.3: 缺失界面补充
- RegisterScreen.kt - 注册页面
- SettingsScreen.kt - 设置页面
- ProfileScreen.kt - 用户资料页

#### 任务 5.4: 高级功能
- [ ] Markdown 渲染（使用 `compose-markdown` 库）
- [ ] 代码高亮
- [ ] 图片预览（Coil 库）
- [ ] 消息长按菜单（复制、删除）
- [ ] 下拉刷新
- [ ] 分页加载
- [ ] 本地缓存（Room 数据库）
- [ ] 错误重试机制
- [ ] 网络状态检测

---

## 🗂️ 最终目录结构

```
app/src/main/java/com/guyi/demo1/
├── MainActivity.kt
├── data/
│   ├── model/                    # 数据模型
│   │   ├── User.kt
│   │   ├── Session.kt
│   │   ├── Message.kt
│   │   ├── WorkspaceFile.kt
│   │   └── ...
│   ├── api/                      # API 接口定义
│   │   ├── AuthApi.kt
│   │   ├── SessionApi.kt
│   │   ├── ChatApi.kt
│   │   ├── MessageApi.kt
│   │   └── WorkspaceApi.kt
│   ├── network/                  # 网络层配置
│   │   ├── RetrofitClient.kt
│   │   ├── AuthInterceptor.kt
│   │   ├── SSEManager.kt
│   │   └── ApiConfig.kt
│   ├── local/                    # 本地存储
│   │   ├── TokenManager.kt
│   │   └── PreferencesManager.kt
│   └── repository/               # Repository 层
│       ├── AuthRepository.kt
│       ├── SessionRepository.kt
│       ├── MessageRepository.kt
│       ├── WorkspaceRepository.kt
│       └── ApprovalRepository.kt
├── ui/
│   ├── screen/
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt    # ✅ UI 完成，待对接
│   │   │   ├── LoginViewModel.kt # ❌ 待创建
│   │   │   ├── RegisterScreen.kt # ❌ 待创建
│   │   │   └── RegisterViewModel.kt
│   │   ├── home/
│   │   │   └── WelcomeScreen.kt  # ✅ 完成
│   │   ├── chat/
│   │   │   ├── ChatScreen.kt     # ✅ UI 完成，待对接
│   │   │   └── ChatViewModel.kt  # ❌ 待创建
│   │   ├── workspace/
│   │   │   ├── WorkspaceScreen.kt # ✅ UI 完成，待对接
│   │   │   └── WorkspaceViewModel.kt # ❌ 待创建
│   │   └── settings/
│   │       └── SettingsScreen.kt # ❌ 待创建
│   ├── components/               # ✅ 完成
│   ├── navigation/
│   │   └── NavGraph.kt           # ✅ 完成，可能需要调整
│   └── theme/                    # ✅ 完成
└── util/                         # 工具类
    ├── DateFormatter.kt
    ├── FileUtils.kt
    └── NetworkUtils.kt
```

---

## 📝 实施原则

### 开发顺序
1. **先模型后接口** - 定义数据模型 → 定义 API 接口 → 实现 Repository
2. **先简单后复杂** - 登录 → 会话列表 → 普通聊天 → SSE 流式 → 工具审批
3. **先核心后扩展** - 基础功能稳定后再添加高级功能

### 测试策略
- 每完成一个 API 对接，立即测试
- 使用 Postman / curl 先测试后端 API
- 使用 Android 模拟器测试（注意 localhost 要用 `10.0.2.2`）
- 真机测试时确保手机和电脑在同一局域网

### 错误处理
```kotlin
// 统一错误处理
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

// 使用示例
when (val result = repository.login(username, password)) {
    is Result.Success -> _uiState.value = LoginUiState.Success(result.data)
    is Result.Error -> _uiState.value = LoginUiState.Error(result.exception.message ?: "未知错误")
}
```

### 状态管理
- 使用 `StateFlow` 管理 UI 状态
- 使用 `MutableStateFlow` 在 ViewModel 中
- 使用 `collectAsState()` 在 Composable 中

---

## ⚠️ 已知问题和注意事项

### Android 模拟器访问本机
```kotlin
// ❌ 错误
const val BASE_URL = "http://localhost:9000"

// ✅ 正确
const val BASE_URL = "http://10.0.2.2:9000"
```

### Material 3 API 兼容性
参考 CLAUDE.md 中的 "重要注意事项" 章节。

### SSE 连接超时
- 设置合理的 read timeout（建议 300 秒）
- 实现心跳机制
- 断线自动重连

### 文件上传大小限制
后端设置了 50MB 限制，客户端也需要相应限制。

---

## 🎯 成功标准

### 阶段 1 完成标准
- [ ] 所有数据模型定义完整
- [ ] Retrofit 配置成功，能发起请求
- [ ] Token 存储和读取正常
- [ ] API 接口定义完整

### 阶段 2 完成标准
- [ ] 登录功能正常工作
- [ ] Token 自动添加到请求头
- [ ] 登录后跳转到欢迎页

### 阶段 3 完成标准
- [ ] 能创建新会话
- [ ] 能发送消息并收到回复
- [ ] SSE 流式输出正常显示
- [ ] 停止生成功能正常
- [ ] 历史消息加载正常

### 阶段 4 完成标准
- [ ] 文件上传成功
- [ ] 文件列表加载正常
- [ ] 文件下载成功
- [ ] 文件删除功能正常

### 阶段 5 完成标准
- [ ] 工具审批弹窗正常显示
- [ ] 审批/拒绝功能正常
- [ ] 会话列表真实数据展示
- [ ] 所有高优先级功能完成

---

## 📅 开发时间线（预估）

| 阶段 | 任务 | 预计时间 | 状态 |
|------|------|----------|------|
| 1 | 数据模型定义 | 2 小时 | ✅ 已完成 |
| 1 | 网络层配置 | 3 小时 | ✅ 已完成 |
| 1 | API 接口定义 | 2 小时 | ⏳ 进行中 |
| 1 | Token 管理 | 1 小时 | ✅ 已完成 |
| 2 | 认证功能 | 3 小时 | ⏳ 待开始 |
| 3 | 会话管理 | 2 小时 | ⏳ 待开始 |
| 3 | 消息历史 | 2 小时 | ⏳ 待开始 |
| 3 | SSE 流式 | 4 小时 | ⏳ 待开始 |
| 4 | 工作区功能 | 3 小时 | ⏳ 待开始 |
| 5 | 工具审批 | 2 小时 | ⏳ 待开始 |
| 5 | 高级功能 | 按需 | ⏳ 待开始 |

**总计**: 约 24-30 小时

---

## 🤝 协作方式

- 在 Ling-Agent 项目的对话中讨论和执行任务
- 每完成一个小任务，在此文档中更新状态
- 遇到问题随时记录在此文档
- 重要决策记录在此文档

---

**文档创建者**: Claude Code Assistant  
**最后更新**: 2026-04-07  
**状态**: 阶段 1 进行中（数据模型、网络配置、Token 管理已完成，待创建 API 接口）
