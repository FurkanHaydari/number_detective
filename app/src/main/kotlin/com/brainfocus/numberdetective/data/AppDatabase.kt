package com.brainfocus.numberdetective.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.brainfocus.numberdetective.data.converters.DateConverter
import com.brainfocus.numberdetective.data.dao.GameResultDao
import com.brainfocus.numberdetective.data.dao.MissionDao
import com.brainfocus.numberdetective.data.dao.PlayerDao
import com.brainfocus.numberdetective.data.entities.GameResult
import com.brainfocus.numberdetective.data.entities.Mission
import com.brainfocus.numberdetective.data.entities.Player

@Database(
    entities = [
        GameResult::class,
        Mission::class,
        Player::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameResultDao(): GameResultDao
    abstract fun missionDao(): MissionDao
    abstract fun playerDao(): PlayerDao

    companion object {
        private const val DATABASE_NAME = "number_detective.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
