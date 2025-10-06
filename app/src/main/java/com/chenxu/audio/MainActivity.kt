package com.chenxu.audio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import android.widget.Button // ← 1.04 add: 导入按钮类
import com.chenxu.audio.R
//1.051
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.net.Uri
import android.content.Context



class MainActivity : AppCompatActivity() {
    private val REQUEST_PERMISSION_CODE = 200

    //1.051.1
    private fun checkManageAllFilesPermission(): Boolean {          //onactivityResult回调时用
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun requestManageAllFilesPermission() {                 //check完>跳转权限页面request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, 1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            if (checkManageAllFilesPermission()) {
                Toast.makeText(this, "读写权限设置 OK", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未授予全部文件读写权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!checkManageAllFilesPermission()) {
                requestManageAllFilesPermission()
                return
            }
        }
    }
    //1.051.1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main) //1.04 add: 设置布局



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_CODE)
        }
        checkPermissions()


        val recordButton = findViewById<Button>(R.id.record_button) //1.04 add: 获取按钮

        recordButton.setOnClickListener { //1.04 add: 点击按钮开始录音

            // 启动录音服务
            val serviceIntent = Intent(this, RecordingService::class.java)
            startForegroundService(serviceIntent)
        }





//        //1.051
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            try {
//                if (!Environment.isExternalStorageManager()) {
//                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
//                    intent.data = Uri.parse("package:$packageName")
//                    startActivity(intent)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(this, "无法跳转文件权限设置", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    1002
//                )
//            }
//        }
//
//    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            // 启动录音服务
//            val serviceIntent = Intent(this, RecordingService::class.java)
//            startForegroundService(serviceIntent)
//        } else {
//            Toast.makeText(this, "录音权限被拒绝", Toast.LENGTH_SHORT).show()
//        }
//    }
}
}
