package com.ellison.jetpackdemo

import android.app.Application
import android.util.Log
import com.ellison.jetpackdemo.dagger2.DaggerManualAppComponent
import com.ellison.jetpackdemo.dagger2.ManualAppComponent
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    val manualAppComponent: ManualAppComponent by lazy {
        DaggerManualAppComponent.factory().create("Dagger Learner")
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        super.onCreate()
    }

    override fun onTerminate() {
        Log.d(TAG, "onTerminate()")
        super.onTerminate()
    }

    companion object {
        @JvmField
        val TAG = MyApplication::class.java.simpleName
    }
}
