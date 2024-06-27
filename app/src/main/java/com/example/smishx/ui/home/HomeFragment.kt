package com.example.smishx.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smishx.R
import com.example.smishx.databinding.FragmentHomeBinding
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val SMS_PERMISSION_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        checkAndRequestPermissions()
        loadLinks()

        return root
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_SMS), SMS_PERMISSION_CODE)
        } else {
            fetchSmsMessages()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchSmsMessages()
            } else {
                Toast.makeText(requireContext(), "SMS Read Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchSmsMessages() {
        val smsList = mutableListOf<Triple<String, String, Long>>()
        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE)
        val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -8)
        val startOfPeriod = calendar.timeInMillis

        cursor?.use {
            val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS)
            val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY)
            val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.Inbox.DATE)
            while (it.moveToNext()) {
                val date = it.getLong(dateIndex)
                if (date >= startOfPeriod) {
                    val address = it.getString(addressIndex)
                    val body = it.getString(bodyIndex)
                    smsList.add(Triple(address, body, date))
                }
            }
        }

        displaySmsMessages(smsList)
    }

    private fun displaySmsMessages(smsList: List<Triple<String, String, Long>>) {
        binding.smsContainer.removeAllViews()
        for (sms in smsList) {
            val smsView = LayoutInflater.from(context).inflate(R.layout.sms_item, binding.smsContainer, false)
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
            binding.smsContainer.addView(smsView)
        }
    }

    private fun extractLink(message: String): String {
        val regex = "(https?://|www\\.|\\S+\\.\\S{2,})([^\\s]*)".toRegex()
        val matchResult = regex.find(message)
        return matchResult?.value ?: "No link"
    }

    private fun loadLinks() {
        val sharedPref = requireActivity().getSharedPreferences("links", Context.MODE_PRIVATE) ?: return

        val legitimateLinks = sharedPref.getStringSet("legitimate_links", setOf()) ?: setOf()
        displayLinks(legitimateLinks, binding.legitimateLinksList)
    }

    private fun displayLinks(links: Set<String>, container: LinearLayout) {
        container.removeAllViews()
        for (link in links) {
            val linkView = LayoutInflater.from(context).inflate(R.layout.link_item, container, false)
            val linkTextView = linkView.findViewById<TextView>(R.id.linkTextView)
            linkTextView.text = link
            linkTextView.setOnClickListener {
                openLink(link)
            }
            container.addView(linkView)
        }
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
