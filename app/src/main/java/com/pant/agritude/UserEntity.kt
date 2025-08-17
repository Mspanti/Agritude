package com.pant.agritude

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val name: String = "",
    val location:String="",
    val state: String? = null,
    val district: String? = null,
    val townOrVillage: String? = null,
    val farmSize: Double = 0.0,
    val cropType: String? = null,
    val landType: String? = null
)
