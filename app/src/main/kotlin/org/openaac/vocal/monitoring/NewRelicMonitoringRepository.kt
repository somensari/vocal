package org.openaac.vocal.monitoring

import com.newrelic.agent.android.NewRelic
import org.openaac.vocal.BuildConfig
import org.openaac.vocal.core.domain.repository.MonitoringRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewRelicMonitoringRepository @Inject constructor() : MonitoringRepository {

    override fun recordHandledException(throwable: Throwable) {
        if (isEnabled()) {
            NewRelic.recordHandledException(throwable)
        }
    }

    private fun isEnabled(): Boolean =
        BuildConfig.NEW_RELIC_ENABLED && BuildConfig.NEW_RELIC_APPLICATION_TOKEN.isNotBlank()
}
