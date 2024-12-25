package com.brainfocus.numberdetective.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.brainfocus.numberdetective.data.dao.GameResultDao
import com.brainfocus.numberdetective.data.dao.MissionDao
import com.brainfocus.numberdetective.data.entities.GameResult
import com.brainfocus.numberdetective.data.entities.Mission
import com.brainfocus.numberdetective.data.typeconverters.MissionTypeConverter

@Database(
    entities = [
        GameResult::class,
        Mission::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(MissionTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameResultDao(): GameResultDao
    abstract fun missionDao(): MissionDao

    companion object {
        private const val DATABASE_NAME = "number_detective_db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
