package com.sipedas.app.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun bukaWhatsApp(context: Context, link: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Tidak ada aplikasi untuk membuka WhatsApp", Toast.LENGTH_SHORT).show()
    }
}