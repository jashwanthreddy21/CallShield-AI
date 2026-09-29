package com.example

import android.app.Application
import com.example.data.local.CallShieldDatabase
import com.example.data.repository.CallShieldRepository
import com.example.services.notifications.CallNotificationManager

class CallShieldApplication : Application() {

    lateinit var database: CallShieldDatabase
        private set

    lateinit var repository: CallShieldRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = CallShieldDatabase.getDatabase(this)
        repository = CallShieldRepository(database, this)
        CallNotificationManager.initNotificationChannels(this)
    }
}
