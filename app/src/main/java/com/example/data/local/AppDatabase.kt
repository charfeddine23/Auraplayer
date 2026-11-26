package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlayerAmbientSettingsEntity::class,
        VideoBookmarkEntity::class,
        WatchSettingsEntity::class,
        HomeMimicEntity::class,
        CyberAvatarStateEntity::class,
        FolderCoverCacheEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerAmbientSettingsDao(): PlayerAmbientSettingsDao
    abstract fun videoBookmarkDao(): VideoBookmarkDao
    abstract fun watchSettingsDao(): WatchSettingsDao
    abstract fun homeMimicDao(): HomeMimicDao
    abstract fun cyberAvatarDao(): CyberAvatarDao
    abstract fun folderCoverCacheDao(): FolderCoverCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player_ambient_settings ADD COLUMN doubleTapSeekSeconds INTEGER NOT NULL DEFAULT 10")
                db.execSQL("ALTER TABLE player_ambient_settings ADD COLUMN controlsAutoHideSeconds INTEGER NOT NULL DEFAULT 10")
                db.execSQL("ALTER TABLE player_ambient_settings ADD COLUMN enableSwipeGestures INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE player_ambient_settings ADD COLUMN seekbarThickness TEXT NOT NULL DEFAULT 'BALANCED'")
                db.execSQL("ALTER TABLE player_ambient_settings ADD COLUMN seekbarGlowIntensity TEXT NOT NULL DEFAULT 'NORMAL'")
                db.execSQL("ALTER TABLE player_ambient_settings ADD COLUMN showBookmarkMarkersOnSeekbar INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE player_ambient_settings ADD COLUMN liveSeekPreview INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS home_mimic_state (
                        id INTEGER NOT NULL PRIMARY KEY,
                        equippedMimicId TEXT NOT NULL DEFAULT 'streak_flame',
                        isMimicEnabled INTEGER NOT NULL DEFAULT 1,
                        updatedAtMs INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cyber_avatar_state (
                        id INTEGER NOT NULL PRIMARY KEY,
                        equippedAvatarId TEXT,
                        unlockedAvatarIds TEXT NOT NULL DEFAULT '',
                        isAuraFrameEnabled INTEGER NOT NULL DEFAULT 1,
                        updatedAtMs INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE watch_settings ADD COLUMN showNewVideoBadge INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE watch_settings ADD COLUMN newVideoBadgeDays INTEGER NOT NULL DEFAULT 7")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player_ambient_settings ADD COLUMN seekbarBelowControls INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS folder_cover_cache (
                        folderKey TEXT NOT NULL PRIMARY KEY,
                        displayName TEXT NOT NULL,
                        sourceUrl TEXT,
                        localFilePath TEXT,
                        sourceProvider TEXT NOT NULL,
                        width INTEGER NOT NULL,
                        height INTEGER NOT NULL,
                        isMusicFolder INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        fetchedAtMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE watch_settings ADD COLUMN autoFetchOnlineFolderCovers INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_pulse_player_db"
                )
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8
                    )
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
