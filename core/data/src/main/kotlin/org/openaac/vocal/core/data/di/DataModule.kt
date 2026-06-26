package org.openaac.vocal.core.data.di

import android.content.Context
import androidx.room.Room
import org.openaac.vocal.core.data.local.VocalDatabase
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.data.repository.BoardRepositoryImpl
import org.openaac.vocal.core.data.repository.PhraseRepositoryImpl
import org.openaac.vocal.core.data.preferences.UserPreferencesRepositoryImpl
import org.openaac.vocal.core.data.speech.SpeechRepositoryImpl
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import org.openaac.vocal.core.domain.repository.SpeechRepository
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VocalDatabase =
        Room.databaseBuilder(
            context,
            VocalDatabase::class.java,
            "vocal.db",
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideBoardDao(database: VocalDatabase): BoardDao = database.boardDao()

    @Provides
    fun providePhraseDao(database: VocalDatabase): PhraseDao = database.phraseDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBoardRepository(impl: BoardRepositoryImpl): BoardRepository

    @Binds
    @Singleton
    abstract fun bindPhraseRepository(impl: PhraseRepositoryImpl): PhraseRepository

    @Binds
    @Singleton
    abstract fun bindSpeechRepository(impl: SpeechRepositoryImpl): SpeechRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository
}
