package com.guyi.demo1

import android.app.Application
import com.guyi.demo1.data.AppContainer

/**
 * 应用程序类
 */
class LingAgentApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
