package com.example.callblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.callblocker.util.Constants.CONTACTS_TABLE_NAME

@Entity(tableName = CONTACTS_TABLE_NAME)
data class Contact(
    @PrimaryKey
    val number: String,
    val name: String
)
