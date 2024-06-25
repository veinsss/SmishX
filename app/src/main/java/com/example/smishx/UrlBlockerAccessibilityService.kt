package com.example.smishx

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class UrlBlockerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "UrlBlockerService"
        private val phishingUrls = listOf("www.example.com", "example.com", "malicious.com", "www.phishing.com")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "Accessibility event triggered: ${event?.eventType}")
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED || it.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                val source = it.source
                Log.d(TAG, "Event source: $source")
                if (source != null) {
                    val text = getNodeText(source)
                    Log.d(TAG, "Node text: $text")
                    if (text != null && isPhishingUrl(text)) {
                        // Block the URL by showing a toast and preventing navigation
                        Toast.makeText(this, "Blocked Phishing URL: $text", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Blocked Phishing URL: $text")
                        preventNavigation(source)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    private fun getNodeText(node: AccessibilityNodeInfo?): String? {
        if (node == null) {
            return null
        }
        return when (node.className) {
            "android.widget.TextView", "android.widget.EditText", "android.widget.Button" -> node.text?.toString()
            else -> {
                for (i in 0 until node.childCount) {
                    val text = getNodeText(node.getChild(i))
                    if (!text.isNullOrEmpty()) {
                        return text
                    }
                }
                null
            }
        }
    }

    private fun isPhishingUrl(url: String): Boolean {
        return phishingUrls.any { url.contains(it, ignoreCase = true) }
    }

    private fun preventNavigation(source: AccessibilityNodeInfo) {
        // Perform actions to prevent navigation
        source.performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS)
        source.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        // Optionally, you can launch a safe intent or redirect the user to a safe page
        val safeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
        safeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(safeIntent)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        this.serviceInfo = info
        Log.d(TAG, "Accessibility service connected")
    }
}
