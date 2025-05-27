package com.chenxu.audio

import android.app.*
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer
import android.util.Log
import android.os.Handler   //★整点处理
import android.os.Looper    //★整点处理


class RecordingService : Service() {
    private var recorder: MediaRecorder? = null
    private var timer: Timer? = null
    private var handler: Handler? = null // ★ 用于首次延迟

    override fun onCreate() {
        super.onCreate()

        Log.i("RecordingService", "Service created")
        startForeground(1, createNotification())

        handler = Handler(Looper.getMainLooper())

        // 计算当前时间到下一个整点的延迟毫秒数
        val now = Calendar.getInstance()
        val nextHour = (now.clone() as Calendar).apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.HOUR_OF_DAY, 1)
        }
        val delayMillis = nextHour.timeInMillis - now.timeInMillis
        Log.i("RecordingService", "First segment delay = $delayMillis ms")

        // ★ 先录制直到整点
        recordSegment(delayMillis)

        // ★ 然后从整点开始每小时录制一次
        handler?.postDelayed({
            timer = fixedRateTimer("recorder", false, 0L, 60 * 60 * 1000) {
                recordSegment(60 * 60 * 1000)
            }
        }, delayMillis)
    }

    private fun recordSegment(durationMillis: Long) {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("RecordingService", "Failed to stop previous recorder: ${e.message}")
        }

        val externalFilesDir = getExternalFilesDir(null)
        if (externalFilesDir == null) {
            Log.e("RecordingService", "External storage directory is null!")
            return
        }

        val outputDir = File(externalFilesDir, "recordings")
        if (!outputDir.exists()) {
            val created = outputDir.mkdirs()
            Log.i("RecordingService", "Created 'recordings' directory: $created")
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outputFile = File(outputDir, "rec_$timestamp.m4a")

        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(48000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            Log.i("RecordingService", "Started new recording: ${outputFile.absolutePath}")

            // ★ 设置录音自动停止
            handler?.postDelayed({
                try {
                    recorder?.apply {
                        stop()
                        release()
                        recorder = null
                    }
                    Log.i("RecordingService", "Recording stopped: ${outputFile.absolutePath}")
                } catch (e: Exception) {
                    Log.e("RecordingService", "Error stopping recording: ${e.message}")
                }
            }, durationMillis)

        } catch (e: Exception) {
            Log.e("RecordingService", "Failed to start recorder: ${e.message}")
        }
    }

    private fun createNotification(): Notification {
        val channelId = "RecordingServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Recording Service",
                NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        timer?.cancel()
        handler?.removeCallbacksAndMessages(null)
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            Log.e("RecordingService", "Failed to stop recorder on destroy: ${e.message}")
        }
        Log.i("RecordingService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
