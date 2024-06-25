package com.example.smishx.ui.home

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.smishx.R
import com.example.smishx.UrlBlockerAccessibilityService
import com.example.smishx.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val SMS_PERMISSION_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Check and request permissions when the fragment is loaded
        checkAndRequestPermissions()

        // Add OnClickListener to the Material Button to list SMS messages
        binding.roundButton.setOnClickListener {
            fetchSMSMessages()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_SMS), SMS_PERMISSION_CODE)
        } else {
            checkAccessibilityService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                checkAccessibilityService()
            } else {
                Toast.makeText(requireContext(), "SMS Read Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAccessibilityService() {
        if (!isAccessibilityServiceEnabled(requireContext(), UrlBlockerAccessibilityService::class.java)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(requireContext(), "Please enable the URL Blocker Accessibility Service", Toast.LENGTH_LONG).show()
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(service.name, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun fetchSMSMessages() {
        val smsList = mutableListOf<Triple<String, String, Long>>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE)
        val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS)
            val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY)
            val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.Inbox.DATE)
            while (it.moveToNext()) {
                val address = it.getString(addressIndex)
                val body = it.getString(bodyIndex)
                val date = it.getLong(dateIndex)
                smsList.add(Triple(address, body, date))
            }
        }

        displaySMSMessages(smsList)
    }

    private fun displaySMSMessages(smsList: List<Triple<String, String, Long>>) {
        // Update UI to show the SMS messages and hide the button
        binding.scrollView.visibility = View.VISIBLE
        binding.roundButton.visibility = View.GONE

        val smsContainer: LinearLayout = binding.smsContainer
        smsContainer.removeAllViews()

        for (sms in smsList) {
            val smsView = LayoutInflater.from(context).inflate(R.layout.sms_item, smsContainer, false)
            val addressTextView = smsView.findViewById<TextView>(R.id.smsNumber)
            val bodyTextView = smsView.findViewById<TextView>(R.id.smsMessage)

            addressTextView.text = sms.first
            bodyTextView.text = sms.second

            smsView.setOnClickListener {
                val action = HomeFragmentDirections.actionNavigationHomeToSmsDetailFragment(
                    timeSent = sms.third.toString(),
                    number = sms.first,
                    message = sms.second,
                    link = extractLink(sms.second)
                )
                findNavController().navigate(action)
            }

            smsContainer.addView(smsView)
        }
    }

    private fun extractLink(message: String): String {
        val regex = "(https?://|www\\.|\\S+\\.\\S{2,})([^\\s]*)".toRegex()
        val matchResult = regex.find(message)
        return matchResult?.value ?: "No link"
    }
}
