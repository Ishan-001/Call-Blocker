package com.example.callblocker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.callblocker.data.Contact
import com.example.callblocker.databinding.ContactItemBinding

class ContactAdapter(
    private val contacts: ArrayList<Contact>
) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: ContactItemBinding) : RecyclerView.ViewHolder(binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            contacts[position].let {
                contactName.text = it.name
                phoneNumber.text = it.number.toString()
            }
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    fun removeItem(position: Int) : Contact {
        val item = contacts.removeAt(position)
        notifyItemRemoved(position)
        return item
    }

    fun insertItem(contact: Contact, position: Int){
        contacts.add(position, contact)
        notifyItemInserted(position)
    }
}