package com.sipedas.app.ui.notifikasi

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.sipedas.app.R

object PemutarNotifikasi {

    private const val BATAS_MS = 4000L

    private var pemutar: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    fun putar(context: Context) {
        berhenti()

        runCatching {
            val afd = context.resources.openRawResourceFd(R.raw.notif_sukses)
            val mp = MediaPlayer()

            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mp.setOnCompletionListener { berhenti() }
            mp.setOnErrorListener { _, _, _ ->
                berhenti()
                true
            }

            mp.prepare()
            mp.start()
            pemutar = mp

            // Potong kalau ringtone lebih panjang dari batas
            handler.postDelayed({ berhenti() }, BATAS_MS)
        }
    }

    fun berhenti() {
        handler.removeCallbacksAndMessages(null)

        pemutar?.let {
            runCatching { it.stop() }
            it.release()
        }
        pemutar = null
    }
}