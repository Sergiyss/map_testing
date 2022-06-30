package com.example.maptesting.room_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

/*
*
* @Entity класс - таблица в которой задаються её поля.
* Чтобы задать название поля можно указать @ColumnInfo(name = "title"), если его не будет,
* то автоматчески будет указано название переменной, так же с типом, но для его явного указания
* используется @ColumnInfo(typeAffinity = TEXT)
*
*/


@Entity(tableName = "car")
data class Car(
    @PrimaryKey(autoGenerate = true)
    val cid: Int? = 0,
    val title: String,
    val snippet: String,
    val icon: Int,
    var latitude: Double,
    var longitude: Double,
    var distance: Float
)