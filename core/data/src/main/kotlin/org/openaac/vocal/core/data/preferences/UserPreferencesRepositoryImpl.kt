package org.openaac.vocal.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vocal_user_preferences",
)

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private val speechRateKey = floatPreferencesKey("speech_rate")

    override val speechRate: Flow<Float> =
        context.userPreferencesDataStore.data.map { prefs ->
            prefs[speechRateKey] ?: DEFAULT_SPEECH_RATE
        }

    override suspend fun setSpeechRate(rate: Float) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[speechRateKey] = rate.coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
        }
    }

    companion object {
        const val DEFAULT_SPEECH_RATE = 1.0f
        const val MIN_SPEECH_RATE = 0.5f
        const val MAX_SPEECH_RATE = 2.0f
    }
}
