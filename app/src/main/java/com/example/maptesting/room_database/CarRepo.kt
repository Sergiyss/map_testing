package com.example.maptesting.room_database

import androidx.lifecycle.LiveData

/*
*
* Класс для более удобного обращения к методам БД.
*
*/


class CarRepo(private val carDao: CarDao) {

    val getAllCars = carDao.getAllCars()

    suspend fun insert(car: Car){
        carDao.insert(car)
    }

    suspend fun deleteAllCars() {
        carDao.deleteAllCars()
    }

    fun getCarByTitle(title: String): LiveData<Car>{
        return carDao.getCarByTitle(title)
    }

}