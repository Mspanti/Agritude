package com.pant.agritude.api

import android.util.Log
import com.google.gson.Gson
import com.pant.agritude.InferenceModel
import com.pant.agritude.ModelType
import com.pant.agritude.FinancialPrediction
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

// ஜெமினி ஏபிஐக்கான கிளையண்ட்
object GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val API_KEY = "AIzaSyAE2cm-ist4r4f24BGXi6_Px4ODWNlcI7Q" // இங்கே உங்கள் API கீயை வைக்கவும்.
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent?key=$API_KEY"

    /**
     * ஒரு உரையிலிருந்து நிதித் தரவுகளைப் பிரித்தெடுக்க Gemini API-ஐப் பயன்படுத்தவும்.
     * @param prompt: பிரித்தெடுக்க வேண்டிய உரை
     * @return: ஒரு JSON ஸ்ட்ரிங் அல்லது null
     */
    suspend fun extractFinancialData(prompt: String): String? {
        val payload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", "Extract the following financial data from the text in a JSON format with keys: 'area_acres', 'fertilizer_kg', 'other_costs_rs'. Text: '$prompt'") })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("responseSchema", JSONObject().apply {
                    put("type", "OBJECT")
                    put("properties", JSONObject().apply {
                        put("area_acres", JSONObject().apply { put("type", "number") })
                        put("fertilizer_kg", JSONObject().apply { put("type", "number") })
                        put("other_costs_rs", JSONObject().apply { put("type", "number") })
                    })
                    put("propertyOrdering", JSONArray().apply {
                        put("area_acres")
                        put("fertilizer_kg")
                        put("other_costs_rs")
                    })
                })
            })
        }.toString()

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = payload.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(API_URL)
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                Log.d("GeminiApiClient", "Extraction Response: $jsonResponse")
                val contentPart = JSONObject(jsonResponse).optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                if (contentPart != null) {
                    contentPart.toString()
                } else {
                    null
                }
            } else {
                Log.e("GeminiApiClient", "API call failed with code: ${response.code}")
                null
            }
        } catch (e: IOException) {
            Log.e("GeminiApiClient", "Error during API call: ${e.message}")
            null
        }
    }

    /**
     * Updates the prompt to generate a narrative-driven response.
     * @param farmerName The name of the farmer.
     * @param cropType The type of crop.
     * @param landType The type of land.
     * @param prediction The financial prediction data.
     * @return A formatted string or null.
     */
    suspend fun formatFinancialResponse(
        farmerName: String,
        cropType: String,
        landType: String,
        prediction: FinancialPrediction
    ): String? {
        val prompt = "கணக்கீடுகள் மற்றும் தகவல்களைப் பயன்படுத்தி, இந்த விவசாயிக்கு ஒரு ஊக்கமளிக்கும் கதையைத் தமிழில் எழுது. " +
                "கதையில் லாபம் மற்றும் பிற நிதி விவரங்களைச் சேர்ப்பதன் மூலம் அது மிகவும் ஆக்கப்பூர்வமாக இருக்க வேண்டும்.\n\n" +
                "விவசாயியின் பெயர்: $farmerName\n" +
                "பயிர்: $cropType\n" +
                "நில வகை: $landType\n" +
                "மதிப்பிடப்பட்ட லாபம்: ${String.format("%.2f", prediction.profit)} (ரூபாய்)\n" +
                "மொத்த உற்பத்தி: ${String.format("%.2f", prediction.production)} (கிலோ)\n" +
                "விளைச்சல்: ${String.format("%.2f", prediction.yieldValue)} (சராசரியாக)\n\n" +
                "இந்தத் தகவல்களைப் பயன்படுத்தி, இந்த விவசாயி எவ்வாறு பெரும் லாபம் ஈட்டி, தன் கிராமத்திற்கும் முன்மாதிரியாக விளங்கினார் என்பதைப் பற்றிய ஒரு சிறு, சுவாரஸ்யமான கதையை உருவாக்கு."

        val payload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }.toString()

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = payload.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(API_URL)
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                Log.d("GeminiApiClient", "Formatting Response: $jsonResponse")
                val text = JSONObject(jsonResponse)
                    .optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                text
            } else {
                Log.e("GeminiApiClient", "API call failed with code: ${response.code}")
                null
            }
        } catch (e: IOException) {
            Log.e("GeminiApiClient", "Error during API call: ${e.message}")
            null
        }
    }
}
