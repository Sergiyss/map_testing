package com.example.maptesting.room_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


/*
*
* Класс БД. Если есть изменения внутри неё, то нужно обязательно менять версию: 1, 2, 3 ...
*
*/

@Database(entities = [Car::class], version = 2, exportSchema = false)
abstract class CarDB: RoomDatabase() {

    abstract fun carDao(): CarDao
    //abstract fun carDao1(): CarDao1 - если есть несколько Dao

    companion object {
        @Volatile
        private var INSTANCE: CarDB? = null

        fun getDatabase(context: Context): CarDB{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CarDB::class.java,
                    "car_database"
                )
                    .fallbackToDestructiveMigrationOnDowngrade()
                    /*.addMigrations(MIGRATION_1_2) //Миграция
                    .allowMainThreadQueries()*/
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE car ADD COLUMN icon INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE car ADD COLUMN latitude DOUBLE DEFAULT 0.0 NOT NULL")
                database.execSQL("ALTER TABLE car ADD COLUMN longitude DOUBLE DEFAULT 0.0 NOT NULL")
                database.execSQL("ALTER TABLE car ADD COLUMN distance FLOAT DEFAULT 0.0 NOT NULL")
            }
        }
    }

}