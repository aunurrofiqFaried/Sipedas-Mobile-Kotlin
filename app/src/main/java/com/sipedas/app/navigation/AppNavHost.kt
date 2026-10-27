package com.sipedas.app.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sipedas.app.data.local.Session
import com.sipedas.app.ui.absensi.KelasAbsensiScreen
import com.sipedas.app.ui.components.GlassBottomBar
import com.sipedas.app.ui.components.PlaceholderScreen
import com.sipedas.app.ui.components.TabItem
import com.sipedas.app.ui.home.HomeScreen
import com.sipedas.app.ui.profile.ProfileScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.sipedas.app.ui.absensi.AbsensiSiswaScreen
import com.sipedas.app.ui.jurnal.JurnalFormScreen
import com.sipedas.app.ui.jurnal.JurnalScreen
import com.sipedas.app.ui.poin.CariSiswaScreen
import com.sipedas.app.ui.poin.CatatPelanggaranScreen
import com.sipedas.app.ui.poin.PoinSiswaScreen
import com.sipedas.app.ui.profile.UbahPasswordScreen
import com.sipedas.app.ui.poin.KontakWaliScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import com.sipedas.app.ui.sholat.SholatKelasScreen
import com.sipedas.app.ui.sholat.SholatSiswaScreen

object Routes {
    const val HOME = "home"
    const val ABSENSI = "absensi"
    const val JURNAL = "jurnal"
    const val POIN = "poin"
    const val PROFIL = "profil"
    const val ABSENSI_SISWA = "absensi/siswa/{kelasId}"
    const val JURNAL_ISI = "jurnal/isi/{jadwalId}"
    const val POIN_SISWA = "poin/siswa/{siswaId}"
    const val POIN_CATAT = "poin/catat/{siswaId}"
    const val UBAH_PASSWORD = "profil/password"
    const val POIN_WALI = "poin/wali/{siswaId}"
    const val SHOLAT = "sholat"
    const val SHOLAT_SISWA = "sholat/{waktu}/{kelasId}"
}

private val tabs = listOf(
    TabItem(Routes.HOME, "Beranda", Icons.Filled.Home),
    TabItem(Routes.ABSENSI, "Absensi", Icons.Filled.CheckCircle),
    TabItem(Routes.JURNAL, "Jurnal", Icons.Filled.Edit),
    TabItem(Routes.POIN, "Poin", Icons.Filled.Warning),
    TabItem(Routes.PROFIL, "Profil", Icons.Filled.Person),
)

// Durasi singkat dan tegas supaya terasa seperti geser, bukan melayang
private val geser = tween<IntOffset>(durationMillis = 230, easing = FastOutSlowInEasing)

// Pindah antar tab: ke tab yang lebih kiri = geser dari kiri. Selain itu = dari kanan.
private fun AnimatedContentTransitionScope<NavBackStackEntry>.arahTab(): AnimatedContentTransitionScope.SlideDirection {
    val dari = tabs.indexOfFirst { it.route == initialState.destination.route }
    val ke = tabs.indexOfFirst { it.route == targetState.destination.route }

    return if (dari >= 0 && ke >= 0 && ke < dari) {
        AnimatedContentTransitionScope.SlideDirection.Right
    } else {
        AnimatedContentTransitionScope.SlideDirection.Left
    }
}

// Pindah antar tab tanpa menumpuk layar di riwayat
private fun NavController.keTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavHost(session: Session, onLogout: () -> Unit) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val rute = backStack?.destination?.route
    // Hanya BK, waka kesiswaan, dan admin yang boleh menghubungi wali (server juga menolak role lain)
    val bolehHubungiWali = session.role in setOf("guru_bk", "kesiswaan", "admin")

    Scaffold(
        containerColor = Color.Transparent,
        // inset sistem sudah ditangani di MainActivity
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Layar detail (nanti: centang siswa) tidak menampilkan bilah bawah
            if (tabs.any { it.route == rute }) {
                GlassBottomBar(
                    items = tabs,
                    selectedRoute = rute,
                    onSelect = { nav.keTab(it) },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
            enterTransition = { slideIntoContainer(arahTab(), animationSpec = geser) },
            exitTransition = { slideOutOfContainer(arahTab(), animationSpec = geser) },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = geser)
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = geser)
            },
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    session = session,
                    onKeAbsensi = { nav.keTab(Routes.ABSENSI) },
                    onKeJurnal = { nav.keTab(Routes.JURNAL) },
                    onKePoin = { nav.keTab(Routes.POIN) },
                )
            }

            composable(Routes.ABSENSI) {
                KelasAbsensiScreen(
                    tampilSholat = session.isWaliKelas,
                    onBukaSholat = { nav.navigate(Routes.SHOLAT) },
                    onPilihKelas = { id, _ -> nav.navigate("absensi/siswa/$id") },
                )
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

            composable(Routes.JURNAL) {
                JurnalScreen(
                    onIsi = { id -> nav.navigate("jurnal/isi/$id") },
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

            composable(Routes.POIN) {
                CariSiswaScreen(
                    onPilih = { id -> nav.navigate("poin/siswa/$id") },
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
                route = Routes.POIN_WALI,
                arguments = listOf(navArgument("siswaId") { type = NavType.IntType }),
            ) { entry ->
                val siswaId = entry.arguments?.getInt("siswaId") ?: return@composable

                KontakWaliScreen(
                    siswaId = siswaId,
                    onBack = { nav.popBackStack() },
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

            composable(Routes.PROFIL) {
                ProfileScreen(
                    session = session,
                    onLogout = onLogout,
                    onUbahPassword = { nav.navigate(Routes.UBAH_PASSWORD) },
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
        }
    }
}