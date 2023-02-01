package com.example.callblocker.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.Toast
import com.android.internal.telephony.ITelephony
import com.example.callblocker.repository.ContactRepository
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Method
import javax.inject.Inject


@AndroidEntryPoint
class IncomingCallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ContactRepository

    @SuppressLint("SoonBlockedPrivateApi")
    override fun onReceive(context: Context, intent: Intent) {
        val telephonyService: ITelephony
        try {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING, ignoreCase = true)) {
                val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Toast.makeText(context, "call from: $number", Toast.LENGTH_LONG).show()

                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                try {
                    val m: Method = tm.javaClass.getDeclaredMethod("getITelephony")
                    m.isAccessible = true
                    telephonyService = m.invoke(tm) as ITelephony
                    if (number != null && isNumberBlocked(number)) {
                        telephonyService.endCall()
                        Toast.makeText(context, "Ending the call from: $number", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isNumberBlocked(number: String): Boolean {
        repository.blockedContacts.value?.forEach {
            if (it.number == number)
                return true
        }
        return false
    }
}