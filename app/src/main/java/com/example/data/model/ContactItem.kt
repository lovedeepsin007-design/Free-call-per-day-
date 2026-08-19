package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val countryCode: String = "+1",
    val avatarColorHex: Long = 0xFF10B981,
    val isFavorite: Boolean = false,
    val category: String = "Personal",
    val email: String = ""
)
