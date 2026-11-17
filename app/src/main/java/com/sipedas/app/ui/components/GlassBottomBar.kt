package com.sipedas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sipedas.app.ui.theme.Orange500
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown

data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val bisaGanti: Boolean = false,
)

@Composable
fun GlassBottomBar(
    items: List<TabItem>,
    selectedRoute: String?,
    onSelect: (String) -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.94f), Color.White.copy(alpha = 0.80f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(Color.White, Orange500.copy(alpha = 0.30f))),
                shape = shape,
            )
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { item ->
            val aktif = item.route == selectedRoute
            val warna = if (aktif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (aktif) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { onSelect(item.route) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    // Segitiga punya ruang kosong di kanannya, jadi kelompok digeser sedikit
                    // agar bagian yang tampak benar-benar di tengah
                    modifier = if (item.bisaGanti) Modifier.offset(x = 3.dp) else Modifier,
                ) {
                    Icon(item.icon, contentDescription = item.label, tint = warna)
                    if (item.bisaGanti) {
                        // Penanda bahwa tab ini bisa diketuk lagi untuk memilih beranda
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = warna,
                            modifier = Modifier
                                .offset(x = (-3).dp)
                                .size(18.dp),
                        )
                    }
                }
            }
        }
    }
}