package org.openaac.vocal

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.openaac.vocal.monitoring.NewRelicMonitoring

@HiltAndroidApp
class VocalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Silent maintainer telemetry only — no caregiver / AAC-user UI.
        NewRelicMonitoring.start(this)
    }
}
