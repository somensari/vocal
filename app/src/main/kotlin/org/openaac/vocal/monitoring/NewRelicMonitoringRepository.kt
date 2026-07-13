package org.openaac.vocal.monitoring

import org.openaac.vocal.core.domain.repository.MonitoringRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewRelicMonitoringRepository @Inject constructor() : MonitoringRepository {

    override fun recordHandledException(
        throwable: Throwable,
        attributes: Map<String, Any>,
    ) {
        NewRelicMonitoring.recordHandledException(throwable, attributes)
    }

    override fun recordBreadcrumb(
        name: String,
        attributes: Map<String, Any>,
    ) {
        NewRelicMonitoring.recordBreadcrumb(name, attributes)
    }

    override fun recordCustomEvent(
        eventName: String,
        attributes: Map<String, Any>,
    ) {
        NewRelicMonitoring.recordCustomEvent(eventName, attributes)
    }

    override fun startInteraction(name: String): String? =
        NewRelicMonitoring.startInteraction(name)

    override fun endInteraction(interactionId: String?) {
        NewRelicMonitoring.endInteraction(interactionId)
    }

    override fun setInteractionName(name: String) {
        NewRelicMonitoring.setInteractionName(name)
    }

    override fun setSessionAttribute(name: String, value: String) {
        NewRelicMonitoring.setSessionAttribute(name, value)
    }

    override fun setSessionAttribute(name: String, value: Double) {
        NewRelicMonitoring.setSessionAttribute(name, value)
    }

    override fun setSessionAttribute(name: String, value: Boolean) {
        NewRelicMonitoring.setSessionAttribute(name, value)
    }

    override fun recordMetric(name: String, category: String, value: Double) {
        NewRelicMonitoring.recordMetric(name, category, value)
    }

    override fun incrementSessionAttribute(name: String) {
        NewRelicMonitoring.incrementSessionAttribute(name)
    }
}
