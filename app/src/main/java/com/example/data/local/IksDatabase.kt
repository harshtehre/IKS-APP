package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProgressEntity::class,
        NoteEntity::class,
        BookmarkEntity::class,
        QuizAttemptEntity::class,
        ChatMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class IksDatabase : RoomDatabase() {
    abstract fun iksDao(): IksDao

    companion object {
        @Volatile
        private var INSTANCE: IksDatabase? = null

        fun getDatabase(context: Context): IksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IksDatabase::class.java,
                    "iksphere_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
