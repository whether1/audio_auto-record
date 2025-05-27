package com.chenxu.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chenxu.audio.RecordingService  // <== 这里导入

class RecordingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, RecordingService::class.java)
        context.startForegroundService(serviceIntent)
    }
}
