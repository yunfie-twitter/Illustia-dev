package com.yunfie.illustia.settings.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SearchHistoryEntity::class,
        FavoriteTagEntity::class,
        ViewHistoryEntity::class,
        AccountEntity::class,
        SavedIllustEntity::class,
        SavedIllustPageEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class IllustiaDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: IllustiaDatabase? = null

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE saved_illusts ADD COLUMN xRestrict INTEGER NOT NULL DEFAULT 0")
                }
            }

        fun getInstance(context: Context): IllustiaDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    IllustiaDatabase::class.java,
                    "illustia.db",
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
