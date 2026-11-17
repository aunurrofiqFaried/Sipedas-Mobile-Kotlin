package com.sipedas.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sipedas.app.data.model.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Session(
    val token: String,
    val name: String,
    val username: String,
    val role: String,
    val isWaliKelas: Boolean,
    // Peran utama + tambahan; kosong untuk sesi lama (sebelum fitur peran ganda)
    val roles: List<String> = emptyList(),
    val kaprodiKode: String? = null,
    val kaprodiNama: String? = null,
)

// Tiga keadaan aplikasi: sedang memeriksa, belum login, sudah login
sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val session: Session) : SessionState
}

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {

    private val kToken = stringPreferencesKey("token")
    private val kName = stringPreferencesKey("name")
    private val kUsername = stringPreferencesKey("username")
    private val kRole = stringPreferencesKey("role")
    private val kWali = booleanPreferencesKey("is_wali_kelas")
    private val kRoles = stringPreferencesKey("roles")
    private val kKaprodiKode = stringPreferencesKey("kaprodi_kode")
    private val kKaprodiNama = stringPreferencesKey("kaprodi_nama")

    val session: Flow<Session?> = context.dataStore.data.map { p ->
        val token = p[kToken] ?: return@map null
        Session(
            token = token,
            name = p[kName] ?: "",
            username = p[kUsername] ?: "",
            role = p[kRole] ?: "",
            isWaliKelas = p[kWali] ?: false,
            roles = (p[kRoles] ?: "").split(",").filter { it.isNotBlank() },
            kaprodiKode = p[kKaprodiKode],
            kaprodiNama = p[kKaprodiNama],
        )
    }

    suspend fun save(token: String, user: UserDto) {
        context.dataStore.edit { p ->
            p[kToken] = token
            p[kName] = user.name
            p[kUsername] = user.username
            p[kRole] = user.role
            p[kWali] = user.isWaliKelas
            p[kRoles] = (user.roles ?: listOf(user.role)).joinToString(",")

            val kaprodi = user.kaprodiJurusan
            if (kaprodi != null) {
                p[kKaprodiKode] = kaprodi.kode
                p[kKaprodiNama] = kaprodi.nama
            } else {
                p.remove(kKaprodiKode)
                p.remove(kKaprodiNama)
            }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}