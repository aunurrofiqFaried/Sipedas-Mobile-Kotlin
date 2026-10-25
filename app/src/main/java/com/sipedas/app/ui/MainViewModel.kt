package com.sipedas.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sipedas.app.SipedasApp
import com.sipedas.app.data.AuthRepository
import com.sipedas.app.data.api.ApiClient
import com.sipedas.app.data.local.SessionState
import com.sipedas.app.data.local.SessionStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    store: SessionStore,
    private val repo: AuthRepository,
) : ViewModel() {

    val session: StateFlow<SessionState> = store.session
        .map { s -> if (s == null) SessionState.LoggedOut else SessionState.LoggedIn(s) }
        .onEach { st -> ApiClient.token = (st as? SessionState.LoggedIn)?.session?.token }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    init {
        ApiClient.onUnauthorized = {
            viewModelScope.launch { repo.hapusSesiLokal() }
        }
    }

    fun logout() {
        viewModelScope.launch { repo.logout() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SipedasApp
                MainViewModel(app.sessionStore, AuthRepository(ApiClient.service, app.sessionStore))
            }
        }
    }
}