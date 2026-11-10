package com.sipedas.app.ui.pimpinan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.sipedas.app.ui.components.ScreenHeader
import com.sipedas.app.ui.pengumuman.LoncengPengumuman

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
    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ScreenHeader(title = "Pimpinan")
            LoncengPengumuman(
                belumDibaca = belumDibaca,
                onClick = onBukaPengumuman,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
            )
        }

        PullToRefreshBox(
            isRefreshing = viewModel.refreshing,
            onRefresh = viewModel::muatUlang,
            modifier = Modifier.fillMaxSize(),
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
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        s.message,
                        style = MaterialTheme.typography.bodyMedium,
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
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}