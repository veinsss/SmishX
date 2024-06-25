package com.example.smishx.ui.smsdetail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smishx.databinding.FragmentSmsDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SmsDetailFragment : Fragment() {

    private var _binding: FragmentSmsDetailBinding? = null
    private val binding get() = _binding!!
    private var detectedLink: String? = null
    private val client = OkHttpClient()
    private var features: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSmsDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val args = SmsDetailFragmentArgs.fromBundle(requireArguments())

        val date = Date(args.timeSent.toLong())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

        binding.textTimeSent.text = formattedDate
        binding.textNumber.text = args.number
        binding.textMessage.text = args.message
        detectedLink = extractLink(args.message)
        binding.textLink.text = detectedLink ?: "Link not found"

        if (detectedLink != null) {
            binding.buttonBlockLink.visibility = View.VISIBLE
            binding.buttonScanLink.visibility = View.VISIBLE
        }

        binding.buttonBlockLink.setOnClickListener {
            blockLink(detectedLink!!)
        }

        binding.buttonScanLink.setOnClickListener {
            fetchPrediction(detectedLink!!)
        }

        binding.buttonViewMore.setOnClickListener {
            binding.textFeatures.text = features ?: "No features found"
            binding.textFeatures.visibility = View.VISIBLE
            binding.scrollViewFeatures.visibility = View.VISIBLE
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun extractLink(message: String): String? {
        val regex = "(https?://|www\\.|\\S+\\.\\S{2,})([^\\s]*)".toRegex()
        val matchResult = regex.find(message)
        return matchResult?.value
    }

    private fun blockLink(link: String) {
        val sharedPref = requireActivity().getSharedPreferences("blocked_links", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(link, link)
            apply()
        }
        Toast.makeText(requireContext(), "Link blocked: $link", Toast.LENGTH_SHORT).show()
    }

    private fun fetchPrediction(url: String) {
        val json = JSONObject()
        json.put("url", url)

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://192.168.254.104:8000/predict")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseData = response.body?.string()
                if (responseData != null) {
                    val jsonResponse = JSONObject(responseData)
                    val prediction = jsonResponse.getString("prediction")
                    features = jsonResponse.getJSONObject("features").toString(2)

                    withContext(Dispatchers.Main) {
                        binding.textScanResult.text = "Prediction: $prediction"
                        binding.textScanResult.setTextColor(
                            if (prediction.equals("Legitimate", ignoreCase = true)) Color.GREEN else Color.RED
                        )
                        binding.textScanResult.visibility = View.VISIBLE
                        binding.buttonViewMore.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.textScanResult.text = "Error: ${e.message}"
                    binding.textScanResult.setTextColor(Color.RED)
                    binding.textScanResult.visibility = View.VISIBLE
                }
            }
        }
    }
}
