package com.example.chesstacticstrainer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.chesstacticstrainer.di.AppContainer
import com.example.chesstacticstrainer.worker.DailyPuzzleWorker

class ChessTacticsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
        DailyPuzzleWorker.schedule(this)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Daily Puzzle",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily chess puzzle reminder"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "daily_puzzle_channel"
    }
}
