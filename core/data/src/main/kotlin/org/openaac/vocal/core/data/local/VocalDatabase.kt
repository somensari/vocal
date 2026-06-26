package org.openaac.vocal.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity

@Database(
    entities = [BoardEntity::class, PhraseEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class VocalDatabase : RoomDatabase() {
    abstract fun boardDao(): BoardDao
    abstract fun phraseDao(): PhraseDao
}
