package com.example

import android.app.Application

/**
 * Main application class for Vibe Music.
 * Acts as the top-level application lifecycle container.
 */
class VibeMusicApp : Application() {
    companion object {
        lateinit var instance: VibeMusicApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
