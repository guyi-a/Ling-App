package com.guyi.demo1.ui.screen.workspace

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guyi.demo1.data.model.WorkspaceFile
import com.guyi.demo1.data.repository.WorkspaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WorkspaceUiState(
    val uploads: List<WorkspaceFile> = emptyList(),
    val outputs: List<WorkspaceFile> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isDownloading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class WorkspaceViewModel(
    private val workspaceRepository: WorkspaceRepository,
    private val context: Context,
    private val sessionId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    init {
        loadFiles()
    }

    fun loadFiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = workspaceRepository.getFiles(sessionId)
            result.onSuccess { response ->
                val uploads = response.files.filter { it.folder == "uploads" }
                val outputs = response.files.filter { it.folder == "outputs" }
                _uiState.value = _uiState.value.copy(
                    uploads = uploads,
                    outputs = outputs,
                    isLoading = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载文件失败: ${e.message}"
                )
            }
        }
    }

    fun uploadFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)
            val result = workspaceRepository.uploadFile(sessionId, uri)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    successMessage = "上传成功: ${response.filename}"
                )
                loadFiles()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = "上传失败: ${e.message}"
                )
            }
        }
    }

    fun downloadFile(file: WorkspaceFile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloading = true, error = null)
            val result = workspaceRepository.downloadFile(sessionId, file.folder, file.name)
            result.onSuccess { responseBody ->
                withContext(Dispatchers.IO) {
                    try {
                        saveToDownloads(file.name, responseBody.bytes())
                        _uiState.value = _uiState.value.copy(
                            isDownloading = false,
                            successMessage = "已保存到下载目录: ${file.name}"
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isDownloading = false,
                            error = "保存文件失败: ${e.message}"
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    error = "下载失败: ${e.message}"
                )
            }
        }
    }

    fun deleteFile(file: WorkspaceFile) {
        viewModelScope.launch {
            val result = workspaceRepository.deleteFile(sessionId, file.folder, file.name)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    successMessage = "已删除: ${file.name}"
                )
                loadFiles()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = "删除失败: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun saveToDownloads(fileName: String, bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用 MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { os ->
                    os.write(bytes)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        } else {
            // Android 9 及以下
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(downloadsDir, fileName)
            file.writeBytes(bytes)
        }
    }
}

class WorkspaceViewModelFactory(
    private val workspaceRepository: WorkspaceRepository,
    private val context: Context,
    private val sessionId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkspaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkspaceViewModel(workspaceRepository, context, sessionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
