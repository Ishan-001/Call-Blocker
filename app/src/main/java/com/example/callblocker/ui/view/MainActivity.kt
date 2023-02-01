package com.example.callblocker.ui.view

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.callblocker.R
import com.example.callblocker.data.Contact
import com.example.callblocker.databinding.ActivityMainBinding
import com.example.callblocker.ui.adapter.ContactAdapter
import com.example.callblocker.ui.viewmodel.MainViewModel
import com.example.callblocker.util.Utils.hideKeyboard
import com.example.callblocker.util.Utils.observeOnce
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val CALL_PERMISSION_REQUEST_CODE = 200
        private const val CONTACTS_PERMISSION_REQUEST_CODE = 201
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var contactsAdapter: ContactAdapter

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkForPermissions()
        setupRecyclerView()
        setUpClickListeners()
    }

    private fun checkForPermissions() {
        val permissions = arrayOf(
            READ_PHONE_STATE,
            CALL_PHONE
        )

        if (ContextCompat.checkSelfPermission(applicationContext, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(applicationContext, permissions[1]) != PackageManager.PERMISSION_GRANTED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(permissions, CALL_PERMISSION_REQUEST_CODE)
    }

    private fun setUpClickListeners() {
        val context = this
        binding.apply {
            floatingButton.setOnClickListener(context)
            addContactButton.setOnClickListener(context)
            selectContactButton.setOnClickListener(context)
            newContactButton.setOnClickListener(context)
            overlay.setOnClickListener(context)
        }
    }

    private fun setupRecyclerView() {
        viewModel.contacts.observe(this) {
            contactsAdapter = ContactAdapter(it as ArrayList)
            binding.contactsRecyclerView.adapter = contactsAdapter
            contactsAdapter.notifyDataSetChanged()

            binding.prompt.isVisible = it.isEmpty()
        }

        binding.contactsRecyclerView.apply{
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(
                this@MainActivity,
                LinearLayoutManager.VERTICAL))
            ItemTouchHelper(swipeCallback).attachToRecyclerView(this)
            setHasFixedSize(true)
        }
    }

    private val swipeCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            val item = contactsAdapter.removeItem(position)
            viewModel.deleteContact(item)

            Snackbar.make(
                viewHolder.itemView,
                getString(R.string.contact_unblocked),
                Snackbar.LENGTH_SHORT
            )
                .setAction(android.R.string.cancel) {
                    contactsAdapter.insertItem(item, position)
                }
                .show()
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            RecyclerViewSwipeDecorator.Builder(
                c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
            )
                .addSwipeRightBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.red))
                .addSwipeRightActionIcon(R.drawable.ic_delete)
                .addSwipeLeftLabel(getString(R.string.unblock))
                .create()
                .decorate()

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.floating_button -> {
                binding.apply {
                    overlay.visibility = View.VISIBLE
                    floatingButtonActions.visibility = View.VISIBLE
                }
            }
            R.id.new_contact_button -> {
                binding.apply {
                    floatingButtonActions.visibility = View.GONE
                    addContactView.visibility = View.VISIBLE
                }
            }
            R.id.add_contact_button -> {
                val name = binding.inputName.text
                val phone = binding.inputNumber.text

                if (name.isNullOrEmpty() || phone.isNullOrEmpty())
                    Snackbar.make(binding.root, getString(R.string.input_error), Snackbar.LENGTH_SHORT).show()
                else {
                    hideKeyboard()
                    // check if number is already blocked
                    if(isNumberBlocked(phone.toString()))
                        Snackbar.make(binding.root, getString(R.string.error_already_blocked), Snackbar.LENGTH_SHORT).show()
                    else
                        blockContact(Contact(phone.toString(), name.toString()))
                }
            }
            R.id.select_contact_button -> {
                if(hasContactsPermission()){
                    launchContactsIntent()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(arrayOf(READ_CONTACTS), CONTACTS_PERMISSION_REQUEST_CODE)
                    }
                }
            }
            R.id.overlay -> {
                binding.apply {
                    addContactView.visibility = View.GONE
                    floatingButtonActions.visibility = View.GONE
                    overlay.visibility = View.GONE
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            launchContactsIntent()
    }

    private fun launchContactsIntent() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            getContactFromResult(result.data)
        }
    }

    private fun hasContactsPermission() : Boolean {
        return (ContextCompat.checkSelfPermission(applicationContext, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("Range", "Recycle")
    private fun getContactFromResult(data: Intent?) {
        lateinit var number: String
        lateinit var name: String

        val contactData = data?.data
        val cursor = contentResolver.query(contactData!!, null, null, null, null)
        if (cursor?.moveToFirst() == true) {
            val id: String = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val hasPhone: String =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
            if (hasPhone.equals("1", ignoreCase = true)) {
                val phones = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                    null, null
                )
                phones?.moveToFirst()
                number = phones?.getString(phones.getColumnIndex("data1")).toString()
            }
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
        }

        blockContact(Contact(number, name))
    }

    private fun blockContact(contact: Contact) {
        viewModel.blockContact(contact)
        contactsAdapter.insertItem(contact, 0)
        Snackbar.make(binding.root, getString(R.string.success_blocked), Snackbar.LENGTH_SHORT).show()

        binding.apply {
            overlay.visibility = View.GONE
            addContactView.visibility = View.GONE
            floatingButtonActions.visibility = View.GONE
            inputName.text = null
            inputNumber.text = null
        }
    }

    private fun isNumberBlocked(number: String): Boolean {
        viewModel.contacts.value?.forEach {
            if (it.number == number)
                return true
        }
        return false
    }


}