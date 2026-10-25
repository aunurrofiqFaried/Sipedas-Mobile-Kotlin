package com.sipedas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.SystemBarStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.sipedas.app.ui.components.AppBackground
import com.sipedas.app.data.local.SessionState
import com.sipedas.app.ui.MainViewModel
import com.sipedas.app.ui.login.LoginScreen
import com.sipedas.app.ui.theme.SipedasTheme
import com.sipedas.app.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        setContent {
            SipedasTheme {
                AppBackground {
                    Surface(modifier = Modifier.fillMaxSize().systemBarsPadding(),
                        color = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        ) {
                        val vm: MainViewModel = viewModel(factory = MainViewModel.Factory)
                        val state by vm.session.collectAsState()

                        when (val s = state) {
                            SessionState.Loading -> Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) { CircularProgressIndicator() }

                            SessionState.LoggedOut -> LoginScreen()

                            is SessionState.LoggedIn -> AppNavHost(
                                session = s.session,
                                onLogout = vm::logout,
                            )
                        }
                    }
                }
            }
        }
    }
}