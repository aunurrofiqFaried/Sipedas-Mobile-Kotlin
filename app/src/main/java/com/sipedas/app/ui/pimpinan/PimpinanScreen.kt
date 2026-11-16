package com.sipedas.app.ui.pimpinan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.local.Session

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PimpinanScreen(
    session: Session,
    belumDibaca: Int,
    onBukaPengumuman: () -> Unit,
    onBukaKepatuhan: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PimpinanViewModel = viewModel(
        key = "pimpinan-${session.token}",
        factory = PimpinanViewModel.Factory,
    ),
) {
    PullToRefreshBox(
        isRefreshing = viewModel.refreshing,
        onRefresh = viewModel::muatUlang,
        modifier = modifier.fillMaxSize(),
    ) {
        when (val s = viewModel.state) {
            PimpinanState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is PimpinanState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    s.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = viewModel::cobaLagi,
                    modifier = Modifier.padding(top = 16.dp),
                ) { Text("Coba lagi") }
            }

            // Sesi berakhir ditangani terpusat oleh ApiClient.onUnauthorized
            PimpinanState.Unauthorized -> Unit

            is PimpinanState.Success -> DashboardPimpinanIsi(
                data = s.data,
                onBukaKepatuhan = onBukaKepatuhan,
                nama = session.name.substringBefore(' ').substringBefore(','),
                belumDibaca = belumDibaca,
                onBukaPengumuman = onBukaPengumuman,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}