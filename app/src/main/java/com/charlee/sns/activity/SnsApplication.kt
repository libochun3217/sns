package com.charlee.sns.activity

import android.app.Application
import com.charlee.sns.manager.SnsEnvController
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

class SnsApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val networkParams = NetworkParams()
        SnsEnvController.getInstance().appContext = this
        SnsEnvController.getInstance().networkParams = networkParams

        // 为了确保SNS模块数据支持功能初始化没有被fragment初始化异常打断，必须将其放在view初始化之前
        SnsEnvController.getInstance().init()


        // 初始化Universal Image Loader
        // 用于图片异步加载
        val defaultOptions = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build()
        val config = ImageLoaderConfiguration.Builder(this)
            .defaultDisplayImageOptions(defaultOptions)
            .diskCacheSize(10 * 1024 * 1024) // 10MB
            .build()
        ImageLoader.getInstance().init(config)
    }
}