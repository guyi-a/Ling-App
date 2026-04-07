package com.guyi.demo1.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guyi.demo1.data.model.User
import com.guyi.demo1.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 登录视图模型
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * 登录
     */
    fun login(username: String, password: String) {
        // 表单验证
        if (username.isBlank()) {
            _uiState.value = LoginUiState.Error("用户名不能为空")
            return
        }
        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("密码不能为空")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            authRepository.login(username, password).fold(
                onSuccess = { user ->
                    _uiState.value = LoginUiState.Success(user)
                },
                onFailure = { error ->
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "登录失败，请重试"
                    )
                }
            )
        }
    }

    /**
     * 注册
     */
    fun register(username: String, password: String) {
        // 表单验证
        if (username.isBlank()) {
            _uiState.value = LoginUiState.Error("用户名不能为空")
            return
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState.Error("密码至少 6 个字符")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            authRepository.register(username, password).fold(
                onSuccess = { user ->
                    _uiState.value = LoginUiState.Success(user)
                },
                onFailure = { error ->
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "注册失败，请重试"
                    )
                }
            )
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

/**
 * 登录 UI 状态
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
