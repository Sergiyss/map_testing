package com.example.maptesting.room_database

import androidx.lifecycle.LiveData
import androidx.room.*

/*
*
* Интерфейс запросов к БД. Лучше использовать разные инфтерфейсы для разных табоиц.
* @Insert or @Insert(parameters) - вставка, под ней пишется функция, которая принимает данные.
* @Update or @Update(parameters) - обновление, обновляет по ключу и вроде как обновляет все данные,
* а не только те, которые передаются.
* @Update or @Update(parameters) - удаление, удаляет по ключу.
* Эти Методы также могут и возвращать что-то, например коливество добавленных записей
*
*/


@Dao
interface CarDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg car: Car)

    @Query("SELECT * FROM car ORDER BY title ASC")
    fun getAllCars(): LiveData<List<Car>>

    @Query("DELETE FROM car")
    suspend fun deleteAllCars()

    @Query("SELECT * FROM car WHERE title = :title")
    fun getCarByTitle(vararg title: String): LiveData<Car>

    @Update
    suspend fun updateCar(vararg car: Car)

}