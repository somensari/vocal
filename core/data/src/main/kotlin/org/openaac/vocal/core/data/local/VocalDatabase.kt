package org.openaac.vocal.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity

@Database(
    entities = [BoardEntity::class, PhraseEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class VocalDatabase : RoomDatabase() {
    abstract fun boardDao(): BoardDao
    abstract fun phraseDao(): PhraseDao

    companion object {
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE boards SET columns = 4 WHERE columns = 3")
            }
        }
    }
}
