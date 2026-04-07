package com.guyi.demo1.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guyi.demo1.data.model.User
import com.guyi.demo1.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 注册 UI 状态
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: User) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

/**
 * 注册页面 ViewModel
 */
class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * 用户注册
     */
    fun register(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            val result = authRepository.register(username, password)

            _uiState.value = if (result.isSuccess) {
                RegisterUiState.Success(result.getOrThrow())
            } else {
                RegisterUiState.Error(result.exceptionOrNull()?.message ?: "注册失败")
            }
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

/**
 * ViewModel 工厂
 */
class RegisterViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
