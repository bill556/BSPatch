package com.study.bspatch.main

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.study.basemvp.R
import com.study.bspatch.PatchUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.content.Intent
import android.net.Uri

import androidx.core.content.FileProvider

import android.os.Build
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private val INSTALL_PACKAGES_REQUESTCODE = 10011

    private lateinit var mTvText: TextView
    private lateinit var mButton: Button
    private lateinit var mButtonToast: Button
    private lateinit var mButtonInstall: Button
    private lateinit var laun: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTvText = findViewById(R.id.tv_myText)
        mButton = findViewById(R.id.btn_update)
        mButtonToast = findViewById(R.id.btn_version)
        mButtonInstall = findViewById(R.id.btn_install)
        init()
    }


    private fun init() {

        laun = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkAndroidO()
        }

        // 用于创建cache目录，方便将patch文件放在Android/data/com.study.bspatch/cache目录
        try {
            File(externalCacheDir!!.absolutePath)
        } catch (ignore: Exception) {
        }


        // 可以修改这里文本，然后打包两个不同的安装包
        mButtonToast.setOnClickListener {
//            Toast.makeText(this, "我现在是旧版本", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "我现在是新版本了哦哈哈哈哈哈", Toast.LENGTH_SHORT).show()
        }


        mButton.setOnClickListener {
            lifecycleScope.launch {
                mTvText.text = "开始合并安装包..."
                val last = System.currentTimeMillis()

                val isSuccess = withContext(Dispatchers.IO) {
                    PatchUtils.bsPatch(
                        newFile = "$externalCacheDir/output.apk",
                        oldFile = applicationInfo.sourceDir,
                        patch = "$externalCacheDir/patch"
                    )
                }
                if (isSuccess) {
                    val cost = System.currentTimeMillis() - last
                    mTvText.text = "合并完成，耗时: $cost ms"
                } else {
                    mTvText.text = "合并失败"
                }
            }
        }


        mButtonInstall.setOnClickListener {
            checkAndroidO()
        }
    }

    //安装程序
    private fun installApk() {
        val intentUpdate = Intent("android.intent.action.VIEW")
        intentUpdate.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //对Android N及以上的版本做判断
            val apkUriN: Uri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName.toString() + ".FileProvider",
                File("$externalCacheDir/output.apk")
            )
            intentUpdate.addCategory("android.intent.category.DEFAULT")
            intentUpdate.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加Flag 表示我们需要什么权限
            intentUpdate.setDataAndType(apkUriN, "application/vnd.android.package-archive")
        } else {
            val apkUri: Uri = Uri.fromFile(File("$externalCacheDir/output.apk"))
            intentUpdate.setDataAndType(apkUri, "application/vnd.android.package-archive")
        }
        startActivity(intentUpdate)
    }



    private fun checkAndroidO() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //系统 Android O及以上版本
            //是否需要处理未知应用来源权限。 true为用户信任安装包安装 false 则需要获取授权
            val canRequestPackageInstalls = packageManager.canRequestPackageInstalls()
            if (canRequestPackageInstalls) {
                installApk()
            } else {
                //请求安装未知应用来源的权限
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.REQUEST_INSTALL_PACKAGES),
                    INSTALL_PACKAGES_REQUESTCODE
                )
            }
        } else {  //直接安装流程
            installApk()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            INSTALL_PACKAGES_REQUESTCODE -> if (grantResults.isNotEmpty() && grantResults[0] === PackageManager.PERMISSION_GRANTED) {  //如果已经有这个权限 则直接安装 否则跳转到授权界面
                installApk()
            } else {
                val packageURI = Uri.parse("package:$packageName") //获取包名，直接跳转到对应App授权界面
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
                laun.launch(intent)
            }
        }

    }
}