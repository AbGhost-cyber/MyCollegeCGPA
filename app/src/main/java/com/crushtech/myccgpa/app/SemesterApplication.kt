package com.crushtech.myccgpa.app

import android.app.Application
import android.content.Intent
import com.crushtech.myccgpa.MainActivity
import com.crushtech.myccgpa.utils.Constants
import com.crushtech.myccgpa.utils.Constants.ONESIGNAL_APP_ID
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SemesterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        //remove this before pushing to store
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler(NotificationReceiver(this))
    }

    inner class NotificationReceiver(val app: SemesterApplication) :
        OneSignal.OSNotificationOpenedHandler {
        override fun notificationOpened(result: OSNotificationOpenedResult?) {
            Intent(app.applicationContext, MainActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                it.action = Constants.ACTION_SHOW_SEM_REQ_FRAGMENT
                app.startActivity(it)
            }
        }

    }
}