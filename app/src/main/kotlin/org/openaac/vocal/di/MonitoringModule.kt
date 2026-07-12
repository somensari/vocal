package org.openaac.vocal.di

import org.openaac.vocal.core.domain.repository.MonitoringRepository
import org.openaac.vocal.monitoring.NewRelicMonitoringRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MonitoringModule {

    @Binds
    @Singleton
    abstract fun bindMonitoringRepository(
        impl: NewRelicMonitoringRepository,
    ): MonitoringRepository
}
