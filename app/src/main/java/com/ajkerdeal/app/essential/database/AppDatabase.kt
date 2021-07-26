package com.ajkerdeal.app.essential.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ajkerdeal.app.essential.database.dao.NotificationDao
import com.ajkerdeal.app.essential.fcm.FCMData

@Database(entities = [FCMData::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "delivery_bondhu_db"
        ).build()
    }


}