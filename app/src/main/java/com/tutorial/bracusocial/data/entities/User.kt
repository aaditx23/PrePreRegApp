package com.tutorial.bracusocial.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class User(
    @PrimaryKey(autoGenerate = false)
    val id:Int,
    val studentID: Int,
    val name: String,
    val password: String,
    var courses:  MutableMap<String, MutableMap<String, MutableList<Int>>>,
    var friends: MutableList<Int>

)


