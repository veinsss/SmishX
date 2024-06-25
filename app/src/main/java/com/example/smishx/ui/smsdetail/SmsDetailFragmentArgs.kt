package com.example.smishx.ui.smsdetail

import android.os.Bundle
import androidx.navigation.NavArgs

data class SmsDetailFragmentArgs(
    val timeSent: String,
    val number: String,
    val message: String,
    val link: String
) : NavArgs {
    fun toBundle(): Bundle {
        val result = Bundle()
        result.putString("timeSent", this.timeSent)
        result.putString("number", this.number)
        result.putString("message", this.message)
        result.putString("link", this.link)
        return result
    }

    companion object {
        @JvmStatic
        fun fromBundle(bundle: Bundle): SmsDetailFragmentArgs {
            bundle.classLoader = SmsDetailFragmentArgs::class.java.classLoader
            val timeSent = bundle.getString("timeSent") ?: throw IllegalArgumentException("Argument 'timeSent' is missing")
            val number = bundle.getString("number") ?: throw IllegalArgumentException("Argument 'number' is missing")
            val message = bundle.getString("message") ?: throw IllegalArgumentException("Argument 'message' is missing")
            val link = bundle.getString("link") ?: throw IllegalArgumentException("Argument 'link' is missing")
            return SmsDetailFragmentArgs(timeSent, number, message, link)
        }
    }
}
