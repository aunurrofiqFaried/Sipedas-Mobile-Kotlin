package com.sipedas.app.ui.pimpinan

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
import com.sipedas.app.data.model.DetailKepatuhan
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface DetailKepatuhanState {
    data object Loading : DetailKepatuhanState
    data object Unauthorized : DetailKepatuhanState
    data class Error(val message: String) : DetailKepatuhanState
    data class Success(val data: DetailKepatuhan) : DetailKepatuhanState
}

class DetailKepatuhanViewModel(
    private val api: ApiService,
    private val guruId: Int,
    private val dari: String?,
    private val sampai: String?,
) : ViewModel() {

    var state by mutableStateOf<DetailKepatuhanState>(DetailKepatuhanState.Loading)
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
        state = DetailKepatuhanState.Loading
        viewModelScope.launch { ambil() }
    }

    private suspend fun ambil() {
        runCatching { api.detailKepatuhan(guruId, dari, sampai) }
            .onSuccess { state = DetailKepatuhanState.Success(it) }
            .onFailure { e ->
                state = if (e is HttpException && e.code() == 401) {
                    DetailKepatuhanState.Unauthorized
                } else {
                    DetailKepatuhanState.Error(e.toUserMessage())
                }
            }
    }

    companion object {
        fun factory(guruId: Int, dari: String?, sampai: String?) = viewModelFactory {
            initializer { DetailKepatuhanViewModel(ApiClient.service, guruId, dari, sampai) }
        }
    }
}