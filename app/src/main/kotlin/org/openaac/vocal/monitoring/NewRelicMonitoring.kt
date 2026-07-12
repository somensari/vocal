package org.openaac.vocal.monitoring

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.newrelic.agent.android.FeatureFlag
import com.newrelic.agent.android.NewRelic
import org.openaac.vocal.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

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
    private val started = AtomicBoolean(false)

    /**
     * Starts the agent when instrumentation is enabled for this build.
     * Safe to call when disabled — no-ops without requiring a token.
     * Idempotent if invoked more than once.
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

        if (NewRelic.isStarted() || !started.compareAndSet(false, true)) {
            Log.d(Tag, "New Relic agent already started")
            return
        }

        val versionName = application.versionName()
        val versionCode = application.versionCode()

        try {
            // Crash / ANR and handled exceptions
            NewRelic.enableFeature(FeatureFlag.CrashReporting)
            NewRelic.enableFeature(FeatureFlag.HandledExceptions)
            NewRelic.enableFeature(FeatureFlag.NativeReporting)
            NewRelic.enableFeature(FeatureFlag.ApplicationExitReporting)

            // Analytics, breadcrumbs, custom events
            NewRelic.enableFeature(FeatureFlag.AnalyticsEvents)
            NewRelic.enableFeature(FeatureFlag.EventPersistence)

            // App start + interactions (Compose needs Jetpack)
            NewRelic.enableFeature(FeatureFlag.AppStartMetrics)
            NewRelic.enableFeature(FeatureFlag.InteractionTracing)
            NewRelic.enableFeature(FeatureFlag.DefaultInteractions)
            NewRelic.enableFeature(FeatureFlag.Jetpack)

            // Persist / harvest when the device is offline or the app is backgrounded —
            // critical for an offline-first AAC app that rarely opens the network.
            NewRelic.enableFeature(FeatureFlag.OfflineStorage)
            NewRelic.enableFeature(FeatureFlag.BackgroundReporting)

            // Agent harvest traffic (and any future cloud features)
            NewRelic.enableFeature(FeatureFlag.NetworkRequests)
            NewRelic.enableFeature(FeatureFlag.NetworkErrorRequests)
            NewRelic.disableFeature(FeatureFlag.HttpResponseBodyCapture)

            NewRelic.withApplicationToken(token)
                .withApplicationVersion(versionName)
                .withApplicationBuild(versionCode.toString())
                .withLaunchActivityName("MainActivity")
                .withLoggingEnabled(BuildConfig.DEBUG)
                .withCrashReportingEnabled(true)
                .start(application)

            // Harvest analytics more often (minimum allowed by the agent is 60s).
            NewRelic.setMaxEventBufferTime(60)
            NewRelic.setMaxEventPoolSize(1000)

            NewRelic.setAttribute("vocal.build_type", BuildConfig.BUILD_TYPE)
            NewRelic.setAttribute("vocal.version_name", versionName)
            NewRelic.setAttribute("vocal.version_code", versionCode.toDouble())
            NewRelic.setAttribute("vocal.monitoring", "new_relic")

            NewRelic.recordBreadcrumb(
                "agent_started",
                mapOf(
                    "build_type" to BuildConfig.BUILD_TYPE,
                    "version_name" to versionName,
                ),
            )
            Log.i(Tag, "New Relic agent started")
        } catch (t: Throwable) {
            started.set(false)
            Log.e(Tag, "New Relic agent failed to start", t)
        }
    }

    fun isEnabled(): Boolean =
        BuildConfig.NEW_RELIC_ENABLED &&
            BuildConfig.NEW_RELIC_APPLICATION_TOKEN.isNotBlank() &&
            (started.get() || NewRelic.isStarted())

    /**
     * Reports a handled exception when the agent is enabled.
     *
     * Do not include AAC phrase text, icons, or recorded audio in [attributes].
     */
    fun recordHandledException(
        throwable: Throwable,
        attributes: Map<String, Any> = emptyMap(),
    ) {
        if (!isEnabled()) return
        if (attributes.isEmpty()) {
            NewRelic.recordHandledException(throwable)
        } else {
            NewRelic.recordHandledException(throwable, attributes.toMutableMap())
        }
    }

    fun recordBreadcrumb(
        name: String,
        attributes: Map<String, Any> = emptyMap(),
    ) {
        if (!isEnabled()) return
        if (attributes.isEmpty()) {
            NewRelic.recordBreadcrumb(name)
        } else {
            NewRelic.recordBreadcrumb(name, attributes.toMutableMap())
        }
    }

    fun recordCustomEvent(
        eventName: String,
        attributes: Map<String, Any> = emptyMap(),
    ) {
        if (!isEnabled()) return
        NewRelic.recordCustomEvent(
            MonitoringEventType,
            eventName,
            attributes.toMutableMap(),
        )
    }

    fun startInteraction(name: String): String? {
        if (!isEnabled()) return null
        return NewRelic.startInteraction(name)
    }

    fun endInteraction(interactionId: String?) {
        if (!isEnabled() || interactionId.isNullOrBlank()) return
        NewRelic.endInteraction(interactionId)
    }

    fun setSessionAttribute(name: String, value: String) {
        if (!isEnabled()) return
        NewRelic.setAttribute(name, value)
    }

    fun setSessionAttribute(name: String, value: Double) {
        if (!isEnabled()) return
        NewRelic.setAttribute(name, value)
    }

    fun setSessionAttribute(name: String, value: Boolean) {
        if (!isEnabled()) return
        NewRelic.setAttribute(name, value)
    }

    fun recordMetric(name: String, category: String, value: Double = 1.0) {
        if (!isEnabled()) return
        NewRelic.recordMetric(name, category, value)
    }

    fun incrementSessionAttribute(name: String) {
        if (!isEnabled()) return
        NewRelic.incrementAttribute(name)
    }

    private fun Application.versionName(): String =
        runCatching {
            packageManager.getPackageInfoCompat(packageName).versionName
        }.getOrNull().orEmpty().ifBlank { "unknown" }

    private fun Application.versionCode(): Long =
        runCatching {
            val info = packageManager.getPackageInfoCompat(packageName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
        }.getOrDefault(0L)

    private fun PackageManager.getPackageInfoCompat(packageName: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            getPackageInfo(packageName, 0)
        }

    /** Must not start with Mobile / NewRelic / NR_ (New Relic reserved prefixes). */
    private const val MonitoringEventType = "VocalApp"
}
