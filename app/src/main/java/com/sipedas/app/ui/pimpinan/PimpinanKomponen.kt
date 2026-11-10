package com.sipedas.app.ui.pimpinan

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipedas.app.data.model.*

/* ---------- Warna ---------- */

object PimpinanWarna {
    val Hijau = listOf(Color(0xFF10B981), Color(0xFF06B6D4))
    val Oranye = listOf(Color(0xFFF97316), Color(0xFFF59E0B))
    val Merah = listOf(Color(0xFFEF4444), Color(0xFFEC4899))
    val Biru = listOf(Color(0xFF0EA5E9), Color(0xFF6366F1))

    val Abu200 = Color(0xFFE5E7EB)
    val Abu400 = Color(0xFF9CA3AF)
    val Abu500 = Color(0xFF6B7280)
    val Abu700 = Color(0xFF374151)

    val TeksHijau = Color(0xFF059669)
    val TeksMerah = Color(0xFFDC2626)
    val TeksKuning = Color(0xFFB45309)
    val TeksBiru = Color(0xFF0369A1)
    val TeksOranye = Color(0xFFEA580C)
}

fun gradPersen(p: Int?): List<Color> = when {
    p == null -> PimpinanWarna.Biru
    p >= 90 -> PimpinanWarna.Hijau
    p >= 75 -> PimpinanWarna.Oranye
    else -> PimpinanWarna.Merah
}

fun teksPersen(p: Int?): String = if (p == null) "-" else "$p%"

fun warnaSel(p: Int?): Color = when {
    p == null -> Color(0x0F000000)
    p >= 95 -> Color(0xD910B981)
    p >= 90 -> Color(0x8010B981)
    p >= 80 -> Color(0xB3F59E0B)
    p >= 70 -> Color(0xCCF97316)
    else -> Color(0xE6EF4444)
}

/* ---------- Bagian tanpa kotak: judul kecil dan garis tipis ---------- */

@Composable
fun Bagian(
    judul: String,
    modifier: Modifier = Modifier,
    aksi: (@Composable () -> Unit)? = null,
    isi: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                judul.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PimpinanWarna.Abu500,
                letterSpacing = 1.5.sp,
            )
            aksi?.invoke()
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PimpinanWarna.Abu200),
        )
        Spacer(Modifier.height(12.dp))
        isi()
    }
}

/* ---------- Penanda LIVE yang berdenyut ---------- */

@Composable
fun TitikLive() {
    val transisi = rememberInfiniteTransition(label = "live")
    val alfa by transisi.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alfa",
    )

    Box(
        Modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(PimpinanWarna.TeksHijau.copy(alpha = alfa)),
    )
}

/* ---------- Cincin persentase ---------- */

@Composable
fun Cincin(
    nilai: Int?,
    ukuran: Dp,
    tebal: Dp,
    gradasi: List<Color>,
    isi: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.size(ukuran), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val t = tebal.toPx()
            val diameter = size.minDimension - t
            val kiriAtas = Offset(t / 2, t / 2)
            val ukur = Size(diameter, diameter)

            drawArc(
                color = Color(0x14000000),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = kiriAtas,
                size = ukur,
                style = Stroke(width = t),
            )

            val sudut = 360f * (nilai ?: 0).coerceIn(0, 100) / 100f

            if (sudut > 0f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = gradasi,
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    ),
                    startAngle = -90f,
                    sweepAngle = sudut,
                    useCenter = false,
                    topLeft = kiriAtas,
                    size = ukur,
                    style = Stroke(width = t, cap = StrokeCap.Round),
                )
            }
        }
        isi()
    }
}

/* ---------- Batang progres ---------- */

@Composable
fun Batang(
    nilai: Int,
    maks: Int,
    gradasi: List<Color>,
    modifier: Modifier = Modifier,
    tinggi: Dp = 6.dp,
) {
    val fraksi = (nilai.toFloat() / maxOf(1, maks)).coerceIn(0.02f, 1f)

    Box(
        modifier
            .fillMaxWidth()
            .height(tinggi)
            .clip(RoundedCornerShape(50))
            .background(PimpinanWarna.Abu200),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraksi)
                .height(tinggi)
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(gradasi)),
        )
    }
}

/* ---------- Grafik area (tren) ---------- */

@Composable
fun GrafikArea(nilai: List<Int?>, label: List<String>, gradasi: List<Color>) {
    if (nilai.size < 2) {
        Text("Data belum cukup untuk membuat grafik tren.", fontSize = 13.sp, color = PimpinanWarna.Abu400)
        return
    }

    val data = nilai.map { it ?: 0 }
    val batasBawah = (((data.min() - 5) / 10) * 10).coerceAtLeast(0)
    val putus = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))

    Column {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(130.dp),
        ) {
            val p = 8.dp.toPx()
            val w = size.width
            val h = size.height

            fun x(i: Int) = p + i * (w - 2 * p) / (data.size - 1)
            fun y(v: Int) = h - p - (v - batasBawah).toFloat() / (100 - batasBawah) * (h - 2 * p)

            listOf(batasBawah, (batasBawah + 100) / 2, 100).forEach { v ->
                drawLine(
                    color = Color(0x14000000),
                    start = Offset(p, y(v)),
                    end = Offset(w - p, y(v)),
                    strokeWidth = 1f,
                    pathEffect = putus,
                )
            }

            val garis = Path().apply {
                data.forEachIndexed { i, v -> if (i == 0) moveTo(x(i), y(v)) else lineTo(x(i), y(v)) }
            }

            val luas = Path().apply {
                addPath(garis)
                lineTo(x(data.lastIndex), h - p)
                lineTo(x(0), h - p)
                close()
            }

            drawPath(luas, Brush.verticalGradient(listOf(gradasi[0].copy(alpha = 0.35f), Color.Transparent)))
            drawPath(
                garis,
                Brush.horizontalGradient(gradasi),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            val titik = Offset(x(data.lastIndex), y(data.last()))
            drawCircle(Color.White, 5.dp.toPx(), titik)
            drawCircle(gradasi[0], 5.dp.toPx(), titik, style = Stroke(width = 2.5.dp.toPx()))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label.first(), fontSize = 10.sp, color = PimpinanWarna.Abu400)
            Text(label[label.size / 2], fontSize = 10.sp, color = PimpinanWarna.Abu400)
            Text(label.last(), fontSize = 10.sp, color = PimpinanWarna.Abu400)
        }
    }
}

/* ---------- Angka ringkasan (tanpa kotak) ---------- */

@Composable
fun Kpi(
    label: String,
    nilai: String,
    sub: String?,
    subWarna: Color,
    gradasi: List<Color>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradasi)),
            )
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, color = PimpinanWarna.Abu500)
        }
        Text(nilai, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        if (sub != null) Text(sub, fontSize = 11.sp, color = subWarna)
    }
}

/* ---------- Peta panas kelas x tanggal ---------- */

@Composable
fun PetaPanasView(peta: PetaPanas) {
    var semua by remember { mutableStateOf(false) }

    if (peta.tanggal.isEmpty() || peta.kelas.isEmpty()) {
        Text("Belum ada data absensi untuk peta panas.", fontSize = 13.sp, color = PimpinanWarna.Abu400)
        return
    }

    val tampil = if (semua) peta.kelas else peta.kelas.take(8)

    Column {
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(76.dp))
            peta.tanggal.forEach { t ->
                Text(
                    t,
                    modifier = Modifier.weight(1f),
                    fontSize = 7.sp,
                    color = PimpinanWarna.Abu500,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        tampil.forEach { k ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    k.nama,
                    modifier = Modifier.width(76.dp),
                    fontSize = 11.sp,
                    color = PimpinanWarna.Abu700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                k.nilai.forEach { v ->
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(1.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(warnaSel(v)),
                    )
                }
            }
        }

        if (peta.kelas.size > 8) {
            Text(
                if (semua) "Tampilkan lebih sedikit" else "Tampilkan semua ${peta.kelas.size} kelas",
                modifier = Modifier
                    .clickable { semua = !semua }
                    .padding(vertical = 10.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PimpinanWarna.TeksOranye,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(
                Color(0xD910B981) to "95+",
                Color(0x8010B981) to "90-94",
                Color(0xB3F59E0B) to "80-89",
                Color(0xCCF97316) to "70-79",
                Color(0xE6EF4444) to "<70",
                Color(0x0F000000) to "kosong",
            ).forEach { (warna, teks) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(9.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(warna),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(teks, fontSize = 9.sp, color = PimpinanWarna.Abu500)
                }
            }
        }
    }
}