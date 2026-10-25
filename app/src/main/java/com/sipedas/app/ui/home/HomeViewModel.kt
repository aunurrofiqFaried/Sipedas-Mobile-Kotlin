package com.sipedas.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sipedas.app.data.api.ApiClient
import com.sipedas.app.data.api.ApiService
import com.sipedas.app.data.api.toUserMessage
import com.sipedas.app.data.model.JadwalHariIniResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface HomeState {
    data object Loading : HomeState
    data object Unauthorized : HomeState
    data class Error(val message: String) : HomeState
    data class Success(val data: JadwalHariIniResponse) : HomeState
}

class HomeViewModel(private val api: ApiService) : ViewModel() {

    var state by mutableStateOf<HomeState>(HomeState.Loading)
        private set

    var refreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { ambil() }
    }

    fun muatUlang() {
        if (refreshing) return

        viewModelScope.launch {
            refreshing = true
            ambil()
            refreshing = false
        }
    }

    fun cobaLagi() {
        state = HomeState.Loading
        viewModelScope.launch { ambil() }
    }

    private suspend fun ambil() {
        runCatching { api.jadwalHariIni() }
            .onSuccess { state = HomeState.Success(it) }
            .onFailure { e ->
                state = if (e is HttpException && e.code() == 401) {
                    HomeState.Unauthorized // token kedaluwarsa atau dicabut
                } else {
                    HomeState.Error(e.toUserMessage())
                }
            }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(ApiClient.service) }
        }
    }
}