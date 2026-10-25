package com.sipedas.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.sipedas.app.R

val GoogleSans = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = GoogleSans),
    displayMedium = base.displayMedium.copy(fontFamily = GoogleSans),
    displaySmall = base.displaySmall.copy(fontFamily = GoogleSans),
    headlineLarge = base.headlineLarge.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Medium),
    bodyLarge = base.bodyLarge.copy(fontFamily = GoogleSans),
    bodyMedium = base.bodyMedium.copy(fontFamily = GoogleSans),
    bodySmall = base.bodySmall.copy(fontFamily = GoogleSans),
    labelLarge = base.labelLarge.copy(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Medium),
)