package com.sipedas.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sipedas.app.data.local.Session
import com.sipedas.app.ui.absensi.AbsensiSiswaScreen
import com.sipedas.app.ui.absensi.KelasAbsensiScreen
import com.sipedas.app.ui.components.GlassBottomBar
import com.sipedas.app.ui.components.TabItem
import com.sipedas.app.ui.home.HomeScreen
import com.sipedas.app.ui.jurnal.JurnalFormScreen
import com.sipedas.app.ui.jurnal.JurnalScreen
import com.sipedas.app.ui.poin.CariSiswaScreen
import com.sipedas.app.ui.poin.CatatPelanggaranScreen
import com.sipedas.app.ui.poin.KontakWaliScreen
import com.sipedas.app.ui.poin.PoinSiswaScreen
import com.sipedas.app.ui.profile.ProfileScreen
import com.sipedas.app.ui.profile.UbahPasswordScreen
import com.sipedas.app.ui.sholat.SholatKelasScreen
import com.sipedas.app.ui.sholat.SholatSiswaScreen
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.sipedas.app.ui.absensi.KelasBantuScreen

object Routes {
    // Satu tujuan navigasi yang berisi kelima tab (digeser dengan pager)
    const val MAIN = "main"

    // Penanda tab (bukan tujuan navigasi tersendiri)
    const val HOME = "home"
    const val ABSENSI = "absensi"
    const val JURNAL = "jurnal"
    const val POIN = "poin"
    const val PROFIL = "profil"

    // Layar detail (bilah bawah tersembunyi)
    const val ABSENSI_SISWA = "absensi/siswa/{kelasId}"
    const val JURNAL_ISI = "jurnal/isi/{jadwalId}"
    const val POIN_SISWA = "poin/siswa/{siswaId}"
    const val POIN_CATAT = "poin/catat/{siswaId}"
    const val POIN_WALI = "poin/wali/{siswaId}"
    const val UBAH_PASSWORD = "profil/password"
    const val SHOLAT = "sholat"
    const val SHOLAT_SISWA = "sholat/{waktu}/{kelasId}"
    const val ABSENSI_BANTU = "absensi/bantu"
}

private val tabs = listOf(
    TabItem(Routes.HOME, "Beranda", Icons.Filled.Home),
    TabItem(Routes.ABSENSI, "Absensi", Icons.Filled.CheckCircle),
    TabItem(Routes.JURNAL, "Jurnal", Icons.Filled.Edit),
    TabItem(Routes.POIN, "Poin", Icons.Filled.Warning),
    TabItem(Routes.PROFIL, "Profil", Icons.Filled.Person),
)

// Durasi singkat dan tegas supaya terasa seperti geser, bukan melayang
private val geser = tween<IntOffset>(durationMillis = 250, easing = FastOutSlowInEasing)

@Composable
fun AppNavHost(session: Session, onLogout: () -> Unit) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val rute = backStack?.destination?.route

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // Pindah tab: tetangga digeser dengan animasi, tab yang jauh langsung lompat
    // (supaya tab-tab di antaranya tidak ikut memuat data dari server)
    val pindahTab: (String) -> Unit = { route ->
        val tujuan = tabs.indexOfFirst { it.route == route }

        if (tujuan >= 0) {
            scope.launch {
                if (abs(tujuan - pagerState.currentPage) > 1) {
                    pagerState.scrollToPage(tujuan)
                } else {
                    pagerState.animateScrollToPage(tujuan)
                }
            }
        }
    }

    // Tombol kembali HP: dari tab mana pun, kembali ke Beranda dulu sebelum keluar aplikasi
    BackHandler(enabled = rute == Routes.MAIN && pagerState.currentPage != 0) {
        pindahTab(Routes.HOME)
    }

    // Hanya BK, waka kesiswaan, dan admin yang boleh menghubungi wali (server juga menolak role lain)
    val bolehHubungiWali = session.role in setOf("guru_bk", "kesiswaan", "admin")

    Scaffold(
        containerColor = Color.Transparent,
        // inset sistem sudah ditangani di MainActivity
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = rute == Routes.MAIN,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                GlassBottomBar(
                    items = tabs,
                    selectedRoute = tabs[pagerState.currentPage].route,
                    onSelect = pindahTab,
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.MAIN,
            modifier = Modifier.padding(padding),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = geser)
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = geser)
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = geser)
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = geser)
            },
        ) {

            composable(Routes.MAIN) {
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier.fillMaxSize(),
                ) { index ->
                    when (tabs[index].route) {
                        Routes.HOME -> HomeScreen(
                            session = session,
                            onKeAbsensi = { pindahTab(Routes.ABSENSI) },
                            onKeJurnal = { pindahTab(Routes.JURNAL) },
                            onKePoin = { pindahTab(Routes.POIN) },
                        )

                        Routes.ABSENSI -> KelasAbsensiScreen(
                            tampilSholat = session.isWaliKelas,
                            onBukaSholat = { nav.navigate(Routes.SHOLAT) },
                            onBukaBantu = { nav.navigate(Routes.ABSENSI_BANTU) },
                            onPilihKelas = { id, _ -> nav.navigate("absensi/siswa/$id") },
                        )

                        Routes.JURNAL -> JurnalScreen(
                            onIsi = { id -> nav.navigate("jurnal/isi/$id") },
                        )

                        Routes.POIN -> CariSiswaScreen(
                            onPilih = { id -> nav.navigate("poin/siswa/$id") },
                        )

                        Routes.PROFIL -> ProfileScreen(
                            session = session,
                            onLogout = onLogout,
                            onUbahPassword = { nav.navigate(Routes.UBAH_PASSWORD) },
                        )
                    }
                }
            }

            composable(
                route = Routes.ABSENSI_SISWA,
                arguments = listOf(navArgument("kelasId") { type = NavType.IntType }),
            ) { entry ->
                val kelasId = entry.arguments?.getInt("kelasId") ?: return@composable

                AbsensiSiswaScreen(
                    kelasId = kelasId,
                    onBack = { nav.popBackStack() },
                )
            }

            composable(
                route = Routes.JURNAL_ISI,
                arguments = listOf(navArgument("jadwalId") { type = NavType.IntType }),
            ) { entry ->
                val jadwalId = entry.arguments?.getInt("jadwalId") ?: return@composable

                JurnalFormScreen(
                    jadwalId = jadwalId,
                    onBack = { nav.popBackStack() },
                    onSelesai = { nav.popBackStack() },
                )
            }

            composable(
                route = Routes.POIN_SISWA,
                arguments = listOf(navArgument("siswaId") { type = NavType.IntType }),
            ) { entry ->
                val siswaId = entry.arguments?.getInt("siswaId") ?: return@composable

                PoinSiswaScreen(
                    siswaId = siswaId,
                    onBack = { nav.popBackStack() },
                    onCatat = { nav.navigate("poin/catat/$siswaId") },
                    bolehHubungiWali = bolehHubungiWali,
                    onHubungiWali = { nav.navigate("poin/wali/$siswaId") },
                )
            }

            composable(
                route = Routes.POIN_CATAT,
                arguments = listOf(navArgument("siswaId") { type = NavType.IntType }),
            ) { entry ->
                val siswaId = entry.arguments?.getInt("siswaId") ?: return@composable

                CatatPelanggaranScreen(
                    siswaId = siswaId,
                    onBack = { nav.popBackStack() },
                    onSelesai = { nav.popBackStack() },
                )
            }

            composable(
                route = Routes.POIN_WALI,
                arguments = listOf(navArgument("siswaId") { type = NavType.IntType }),
            ) { entry ->
                val siswaId = entry.arguments?.getInt("siswaId") ?: return@composable

                KontakWaliScreen(
                    siswaId = siswaId,
                    onBack = { nav.popBackStack() },
                )
            }

            composable(Routes.UBAH_PASSWORD) {
                UbahPasswordScreen(
                    onBack = { nav.popBackStack() },
                    onSelesai = { nav.popBackStack() },
                )
            }

            composable(Routes.SHOLAT) {
                SholatKelasScreen(
                    onBack = { nav.popBackStack() },
                    onPilih = { kelasId, waktu -> nav.navigate("sholat/$waktu/$kelasId") },
                )
            }

            composable(
                route = Routes.SHOLAT_SISWA,
                arguments = listOf(
                    navArgument("waktu") { type = NavType.StringType },
                    navArgument("kelasId") { type = NavType.IntType },
                ),
            ) { entry ->
                val waktu = entry.arguments?.getString("waktu") ?: return@composable
                val kelasId = entry.arguments?.getInt("kelasId") ?: return@composable

                SholatSiswaScreen(
                    waktu = waktu,
                    kelasId = kelasId,
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Routes.ABSENSI_BANTU) {
                KelasBantuScreen(
                    onBack = { nav.popBackStack() },
                    onPilihKelas = { id, _ -> nav.navigate("absensi/siswa/$id") },
                )
            }
        }
    }
}