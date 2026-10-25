package com.sipedas.app.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sipedas.app.SipedasApp
import com.sipedas.app.data.AuthRepository
import com.sipedas.app.data.api.ApiClient
import com.sipedas.app.data.api.toUserMessage
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {

    var state by mutableStateOf(LoginUiState())
        private set

    fun onUsernameChange(value: String) {
        state = state.copy(username = value, error = null)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value, error = null)
    }

    // Kalau berhasil, MainViewModel melihat sesi baru dan layar pindah otomatis
    fun login() {
        if (state.loading) return

        if (state.username.isBlank() || state.password.isBlank()) {
            state = state.copy(error = "Username dan password wajib diisi.")
            return
        }

        state = state.copy(loading = true, error = null)

        viewModelScope.launch {
            repo.login(state.username, state.password)
                .onSuccess { state = LoginUiState() }
                .onFailure { state = state.copy(loading = false, error = it.toUserMessage()) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SipedasApp
                LoginViewModel(AuthRepository(ApiClient.service, app.sessionStore))
            }
        }
    }
}