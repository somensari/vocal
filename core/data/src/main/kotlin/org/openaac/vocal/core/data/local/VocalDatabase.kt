package org.openaac.vocal.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.data.local.dao.PhraseGroupDao
import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.local.entity.PhraseGroupEntity

@Database(
    entities = [BoardEntity::class, PhraseGroupEntity::class, PhraseEntity::class],
    version = 5,
    exportSchema = true,
)
abstract class VocalDatabase : RoomDatabase() {
    abstract fun boardDao(): BoardDao
    abstract fun phraseDao(): PhraseDao
    abstract fun phraseGroupDao(): PhraseGroupDao

    companion object {
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE boards SET columns = 4 WHERE columns = 3")
            }
        }

        /**
         * Adds phrase groups and optional phrase→group assignment.
         * Deleting a group clears [PhraseEntity.groupId] (SET NULL), not phrases.
         */
        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `phrase_groups` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `boardId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `colorIndex` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        FOREIGN KEY(`boardId`) REFERENCES `boards`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_phrase_groups_boardId` " +
                        "ON `phrase_groups` (`boardId`)",
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `phrases_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `boardId` INTEGER NOT NULL,
                        `label` TEXT NOT NULL,
                        `spokenText` TEXT NOT NULL,
                        `row` INTEGER NOT NULL,
                        `column` INTEGER NOT NULL,
                        `iconPath` TEXT,
                        `audioPath` TEXT,
                        `groupId` INTEGER,
                        FOREIGN KEY(`boardId`) REFERENCES `boards`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`groupId`) REFERENCES `phrase_groups`(`id`)
                            ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `phrases_new` (
                        `id`, `boardId`, `label`, `spokenText`,
                        `row`, `column`, `iconPath`, `audioPath`, `groupId`
                    )
                    SELECT
                        `id`, `boardId`, `label`, `spokenText`,
                        `row`, `column`, `iconPath`, `audioPath`, NULL
                    FROM `phrases`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `phrases`")
                db.execSQL("ALTER TABLE `phrases_new` RENAME TO `phrases`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_phrases_boardId` ON `phrases` (`boardId`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_phrases_groupId` ON `phrases` (`groupId`)",
                )
            }
        }

        /**
         * Replaces sparse row/column coordinates with a single list [PhraseEntity.sortOrder].
         * Existing phrases keep relative order from their previous row-major reading order.
         */
        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `phrases_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `boardId` INTEGER NOT NULL,
                        `label` TEXT NOT NULL,
                        `spokenText` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `iconPath` TEXT,
                        `audioPath` TEXT,
                        `groupId` INTEGER,
                        FOREIGN KEY(`boardId`) REFERENCES `boards`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`groupId`) REFERENCES `phrase_groups`(`id`)
                            ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `phrases_new` (
                        `id`, `boardId`, `label`, `spokenText`,
                        `sortOrder`, `iconPath`, `audioPath`, `groupId`
                    )
                    SELECT
                        p.`id`,
                        p.`boardId`,
                        p.`label`,
                        p.`spokenText`,
                        (
                            SELECT COUNT(*)
                            FROM `phrases` AS o
                            WHERE o.`boardId` = p.`boardId`
                              AND (
                                o.`row` < p.`row`
                                OR (o.`row` = p.`row` AND o.`column` < p.`column`)
                                OR (o.`row` = p.`row` AND o.`column` = p.`column` AND o.`id` < p.`id`)
                              )
                        ),
                        p.`iconPath`,
                        p.`audioPath`,
                        p.`groupId`
                    FROM `phrases` AS p
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `phrases`")
                db.execSQL("ALTER TABLE `phrases_new` RENAME TO `phrases`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_phrases_boardId` ON `phrases` (`boardId`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_phrases_groupId` ON `phrases` (`groupId`)",
                )
            }
        }

        /**
         * Adds board [BoardEntity.seedKey] and phrase [PhraseEntity.targetBoardId] for
         * Home + folder multi-board navigation.
         */
        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `boards` ADD COLUMN `seedKey` TEXT")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `phrases_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `boardId` INTEGER NOT NULL,
                        `label` TEXT NOT NULL,
                        `spokenText` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `iconPath` TEXT,
                        `audioPath` TEXT,
                        `groupId` INTEGER,
                        `targetBoardId` INTEGER,
                        FOREIGN KEY(`boardId`) REFERENCES `boards`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`groupId`) REFERENCES `phrase_groups`(`id`)
                            ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(`targetBoardId`) REFERENCES `boards`(`id`)
                            ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `phrases_new` (
                        `id`, `boardId`, `label`, `spokenText`,
                        `sortOrder`, `iconPath`, `audioPath`, `groupId`, `targetBoardId`
                    )
                    SELECT
                        `id`, `boardId`, `label`, `spokenText`,
                        `sortOrder`, `iconPath`, `audioPath`, `groupId`, NULL
                    FROM `phrases`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `phrases`")
                db.execSQL("ALTER TABLE `phrases_new` RENAME TO `phrases`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_phrases_boardId` ON `phrases` (`boardId`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_phrases_groupId` ON `phrases` (`groupId`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_phrases_targetBoardId` " +
                        "ON `phrases` (`targetBoardId`)",
                )
            }
        }
    }
}
