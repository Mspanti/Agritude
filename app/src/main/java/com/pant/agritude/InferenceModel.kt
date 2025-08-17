package com.pant.agritude

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// எந்த மாடலைப் பயன்படுத்த வேண்டும் என்பதைக் குறிக்கும் enum
enum class ModelType {
    GEMMA,
    FINANCIAL
}

/**
 * LlmInference மாடல்களை நிர்வகிக்கிறது.
 * இப்போது Gemma மற்றும் TFinancial ஆகிய இரண்டு மாடல்களையும் கையாள முடியும்.
 */
class InferenceModel private constructor(private val context: Context) {
    private var gemmaInference: LlmInference? = null
    private var financialInference: LlmInference? = null

    // மாடல் ஃபைல்களின் பெயர்கள்
    private val GEMMA_MODEL_FILE_NAME = "gemma-2b-it-cpu-int4.tflite"
    private val FINANCIAL_MODEL_FILE_NAME = "agritude_financial_model.tflite" // உங்கள் financial model-இன் ஃபைல் பெயர்

    // மாடல் ஃபைல்களுக்கான Path-கள்
    private val GEMMA_MODEL_PATH: String
        get() = File(context.cacheDir, GEMMA_MODEL_FILE_NAME).absolutePath

    private val FINANCIAL_MODEL_PATH: String
        get() = File(context.cacheDir, FINANCIAL_MODEL_FILE_NAME).absolutePath

    private val _partialResults = MutableSharedFlow<Pair<String, Boolean>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val partialResults: SharedFlow<Pair<String, Boolean>> = _partialResults.asSharedFlow()

    private fun modelExists(fileName: String): Boolean = File(context.cacheDir, fileName).exists()

    /**
     * மாடல் ஃபைலை assets-லிருந்து cache-க்கு நகலெடுக்கிறது.
     */
    @Throws(IOException::class)
    private fun copyModelFromAssets(fileName: String, destinationPath: String) {
        val assetManager = context.assets
        val inputStream = assetManager.open(fileName)
        val outFile = File(destinationPath)
        val outputStream = FileOutputStream(outFile)

        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }

        inputStream.close()
        outputStream.flush()
        outputStream.close()
        Log.d("InferenceModel", "Model file copied to: ${outFile.absolutePath}")
    }

    /**
     * LLM மாடல்களைத் தொடங்குகிறது.
     * இப்போது Gemma மற்றும் Financial Model ஆகிய இரண்டையும் லோட் செய்கிறது.
     */
    suspend fun initialize() {
        try {
            // Gemma மாடலை லோட் செய்கிறது
            if (!modelExists(GEMMA_MODEL_FILE_NAME)) {
                copyModelFromAssets(GEMMA_MODEL_FILE_NAME, GEMMA_MODEL_PATH)
            }
            gemmaInference = LlmInference.createFromOptions(context, LlmInferenceOptions.builder()
                .setModelPath(GEMMA_MODEL_PATH)
                .setMaxTokens(1024)
                .build())
            Log.d("InferenceModel", "Gemma LlmInference instance created successfully.")

            // Financial மாடலை லோட் செய்கிறது
            if (!modelExists(FINANCIAL_MODEL_FILE_NAME)) {
                copyModelFromAssets(FINANCIAL_MODEL_FILE_NAME, FINANCIAL_MODEL_PATH)
            }
            financialInference = LlmInference.createFromOptions(context, LlmInferenceOptions.builder()
                .setModelPath(FINANCIAL_MODEL_PATH)
                .setMaxTokens(1024)
                .build())
            Log.d("InferenceModel", "Financial LlmInference instance created successfully.")

        } catch (e: Exception) {
            Log.e("InferenceModel", "Error initializing LLM models: ${e.message}", e)
            _partialResults.tryEmit("Error: LLM Model initialization failed." to true)
        }
    }

    /**
     * LLM மாடலில் இருந்து அசிங்கரமான பதிலைத் உருவாக்குகிறது.
     * எந்த மாடலை பயன்படுத்த வேண்டும் என்பதை ModelType மூலம் முடிவு செய்கிறது.
     */
    fun generateResponseAsync(prompt: String, modelType: ModelType) {
        val llmToUse = when (modelType) {
            ModelType.GEMMA -> gemmaInference
            ModelType.FINANCIAL -> financialInference
        }

        if (llmToUse != null) {
            llmToUse.generateResponseAsync(prompt) { partialResult, done ->
                _partialResults.tryEmit(partialResult to done)
            }
        } else {
            _partialResults.tryEmit("LLM Model ($modelType) is not initialized." to true)
        }
    }

    /**
     * LlmInference இன்ஸ்டன்ஸ்களை மூடுகிறது.
     */
    fun close() {
        gemmaInference?.close()
        gemmaInference = null
        financialInference?.close()
        financialInference = null
        Log.d("InferenceModel", "LlmInference instances closed.")
    }

    companion object {
        @Volatile
        private var instance: InferenceModel? = null

        fun getInstance(context: Context): InferenceModel {
            return instance ?: synchronized(this) {
                instance ?: InferenceModel(context.applicationContext).also { instance = it }
            }
        }
    }
}
