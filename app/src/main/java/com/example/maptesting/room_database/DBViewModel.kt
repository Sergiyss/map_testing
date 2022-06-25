package com.example.maptesting.room_database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
*
* Методы ViewModel для работы в разных поток с БД.
*
*/


class DBViewModel(app: Application): AndroidViewModel(app) {

    val getAllCars: LiveData<List<Car>>
    private val repository: CarRepo

    init {
        val userDao = CarDB.getDatabase(app).carDao()
        repository = CarRepo(userDao)
        getAllCars = repository.getAllCars
    }

    fun addCar(car: Car){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(car)
        }
    }

    fun deleteAllCars(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllCars()
        }
    }

    fun getCarByTitle(title: String): LiveData<Car>{
        return repository.getCarByTitle(title)
    }

}