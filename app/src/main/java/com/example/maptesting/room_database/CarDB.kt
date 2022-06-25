package com.example.maptesting.room_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/*
*
* Класс БД. Если есть изменения внутри неё, то нужно обязательно менять версию: 1, 2, 3 ...
*
*/

@Database(entities = [Car::class], version = 1, exportSchema = false)
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
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}