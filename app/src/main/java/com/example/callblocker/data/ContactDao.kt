package com.example.callblocker.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.callblocker.util.Constants.CONTACTS_TABLE_NAME

@Dao
interface ContactDao {
    @Query("SELECT * FROM $CONTACTS_TABLE_NAME")
    fun getContacts() : LiveData<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact : Contact)

    @Delete
    suspend fun deleteContact(contact : Contact)
}