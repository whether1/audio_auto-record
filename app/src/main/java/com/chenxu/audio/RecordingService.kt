package com.chenxu.audio

import android.app.Service
import android.media.MediaRecorder
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
//1.05
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

import android.os.Handler
import android.os.Looper
import android.content.pm.ServiceInfo
//1.051
import android.os.Environment



class RecordingService : Service() {
    private var recorder: MediaRecorder? = null
    private val CHANNEL_ID = "audio_record_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mic_record_channel",
                "麦克风录音服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于录音的前台服务通知"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // 构建通知
        val notification = NotificationCompat.Builder(this, "mic_record_channel")
            .setContentTitle("录音进行中")
            .setContentText("应用正在使用麦克风")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        startRecording()
    }

        private fun startRecording() {
//            val outputDir = getExternalFilesDir(null)
//            if (outputDir == null) {
//                Handler(Looper.getMainLooper()).post {
//                    Toast.makeText(this, "存储目录不存在", Toast.LENGTH_SHORT).show()
//                }
//                return
//            }
//
//            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            val outputFile = File(outputDir, "$timeStamp.m4a").absolutePath

            // 获取 /sdcard/aaa 目录    1.051
            val outputDir = File(Environment.getExternalStorageDirectory(), "aaa")

// 如果目录不存在就创建它
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

// 如果仍然无法创建目录，提示错误
            if (!outputDir.exists()) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "无法访问 /sdcard/aaa 目录", Toast.LENGTH_SHORT).show()
                }
                return;
            }

// 生成带时间戳的文件路径
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outputFile = File(outputDir, "$timeStamp.m4a").absolutePath


            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(48000)
                setOutputFile(outputFile)
                prepare()
                start()
            }

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "录音已开始", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
        }

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

}