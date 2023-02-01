package com.example.callblocker.repository

import com.example.callblocker.data.Contact
import com.example.callblocker.data.ContactDao
import javax.inject.Inject

class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {
    val blockedContacts = contactDao.getContacts()
    suspend fun blockContact(contact : Contact) = contactDao.insertContact(contact)
    suspend fun deleteContact(contact : Contact) = contactDao.deleteContact(contact)
}