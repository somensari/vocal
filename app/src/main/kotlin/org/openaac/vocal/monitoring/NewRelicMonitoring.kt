package org.openaac.vocal.monitoring

import android.app.Application
import android.util.Log
import com.newrelic.agent.android.FeatureFlag
import com.newrelic.agent.android.NewRelic
import org.openaac.vocal.BuildConfig

/**
 * Silent New Relic Mobile instrumentation for maintainer crash / performance triage.
 *
 * Enabled only when [BuildConfig.NEW_RELIC_ENABLED] is true (token + enable flag via
 * `local.properties` / CI secrets — never committed). No Compose or settings UI.
 *
 * **Privacy:** Do not attach phrase text, custom icon paths, recorded audio paths, or
 * other AAC communication content as custom attributes or events.
 */
object NewRelicMonitoring {
    private const val Tag = "NewRelicMonitoring"

    /**
     * Starts the agent when instrumentation is enabled for this build.
     * Safe to call when disabled — no-ops without requiring a token.
     */
    fun start(application: Application) {
        if (!BuildConfig.NEW_RELIC_ENABLED) {
            Log.d(Tag, "New Relic agent disabled for this build")
            return
        }

        val token = BuildConfig.NEW_RELIC_APPLICATION_TOKEN
        if (token.isBlank()) {
            Log.w(Tag, "New Relic enabled but token is blank; skipping start")
            return
        }

        // Crash / ANR, handled exceptions, app start, and interaction metrics use agent defaults.
        NewRelic.enableFeature(FeatureFlag.CrashReporting)
        NewRelic.enableFeature(FeatureFlag.HandledExceptions)
        NewRelic.enableFeature(FeatureFlag.AnalyticsEvents)
        NewRelic.enableFeature(FeatureFlag.AppStartMetrics)
        NewRelic.withApplicationToken(token).start(application.applicationContext)
        Log.i(Tag, "New Relic agent started")
    }

    /**
     * Reports a handled exception when the agent is enabled.
     *
     * Do not include AAC phrase text, icons, or recorded audio in [attributes].
     */
    fun recordHandledException(
        throwable: Throwable,
        attributes: Map<String, Any>? = null,
    ) {
        if (!BuildConfig.NEW_RELIC_ENABLED) return
        if (attributes.isNullOrEmpty()) {
            NewRelic.recordHandledException(throwable)
        } else {
            NewRelic.recordHandledException(throwable, attributes)
        }
    }
}
