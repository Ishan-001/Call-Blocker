package com.example.callblocker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.callblocker.data.Contact
import com.example.callblocker.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository : ContactRepository
) : ViewModel() {
    val contacts : LiveData<List<Contact>> = repository.blockedContacts

    fun blockContact(contact : Contact) = viewModelScope.launch(Dispatchers.IO){
        repository.blockContact(contact)
    }

    fun deleteContact(contact : Contact) = viewModelScope.launch(Dispatchers.IO){
        repository.deleteContact(contact)
    }
}