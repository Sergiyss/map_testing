package com.example.maptesting.view_modal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DeliveryAddressSearchViewModal  : ViewModel() {

    /** ШПАРГАЛКА **/
    private val _sumTranslate = MutableLiveData<String>().apply { value = "0.0" }

    val sumTranslate : LiveData<String> = _sumTranslate


    fun setContent(){
        _sumTranslate.value = ""
    }

    /** ---- **/
}