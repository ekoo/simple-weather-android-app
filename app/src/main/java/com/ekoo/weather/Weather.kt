package com.ekoo.weather

import android.app.Application
import com.google.android.material.color.DynamicColors

class Weather : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}