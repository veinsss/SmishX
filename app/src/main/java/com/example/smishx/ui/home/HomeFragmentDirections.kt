package com.example.smishx.ui.home

import android.os.Bundle
import androidx.navigation.NavDirections
import com.example.smishx.R

class HomeFragmentDirections private constructor() {
    private data class ActionNavigationHomeToSmsDetailFragment(
        val timeSent: String,
        val number: String,
        val message: String,
        val link: String
    ) : NavDirections {
        override val actionId: Int
            get() = R.id.action_navigation_home_to_smsDetailFragment

        override val arguments: Bundle
            get() {
                val result = Bundle()
                result.putString("timeSent", this.timeSent)
                result.putString("number", this.number)
                result.putString("message", this.message)
                result.putString("link", this.link)
                return result
            }
    }

    companion object {
        fun actionNavigationHomeToSmsDetailFragment(
            timeSent: String,
            number: String,
            message: String,
            link: String
        ): NavDirections = ActionNavigationHomeToSmsDetailFragment(timeSent, number, message, link)
    }
}
