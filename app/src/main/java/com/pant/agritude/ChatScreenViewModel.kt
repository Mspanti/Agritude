package com.pant.agritude

import android.app.Application
import android.content.Context
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pant.agritude.api.GeminiApiClient
import com.pant.agritude.chatscreen.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// சாட் திரையின் UI நிலை (UI state)
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLLMGenerating: Boolean = false,
    val chatHistory: List<List<Message>> = emptyList()
)

// சாட் திரையின் லாஜிக்கை நிர்வகிக்கிறது.
class ChatScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("chat_app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // InferenceModel மற்றும் TFLiteFinancialModel இன்ஸ்டன்ஸ்களை இங்கே பயன்படுத்துகிறோம்.
    private val inferenceModel = InferenceModel.getInstance(application)
    private val tfliteFinancialModel = TFLiteFinancialModel(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadChatHistory()
        // ViewModel தொடங்கியவுடன் இரண்டு மாடல்களையும் தொடங்குகிறது.
        viewModelScope.launch {
            inferenceModel.initialize()
            tfliteFinancialModel.initialize()
        }

        // InferenceModel-இன் பதில்களைக் கேட்கிறது.
        viewModelScope.launch {
            inferenceModel.partialResults.collect { (partialResult, done) ->
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.toMutableList()

                    val newResponse = if (updatedMessages.isNotEmpty() && updatedMessages.last().sender == "llm") {
                        updatedMessages.last().text + partialResult
                    } else {
                        partialResult
                    }

                    if (updatedMessages.isEmpty() || updatedMessages.last().sender != "llm") {
                        updatedMessages.add(Message(newResponse, "llm", System.currentTimeMillis()))
                    } else {
                        updatedMessages[updatedMessages.lastIndex] = updatedMessages.last().copy(text = newResponse)
                    }

                    currentState.copy(messages = updatedMessages, isLLMGenerating = !done)
                }
            }
        }
    }

    // பயனர் உள்ளீட்டைக் கையாள்கிறது.
    fun handleUserInput(text: String) {
        val userMessage = Message(text, "user", System.currentTimeMillis())
        _uiState.update { it.copy(messages = it.messages + userMessage) }
        _uiState.update { it.copy(isLLMGenerating = true) }

        val lowerCaseText = text.lowercase()

        // Agentic AI லாஜிக்: முதலில் கேள்வியைச் சரிபார்க்கிறது.
        if (lowerCaseText.contains("லாபம்" ) ||
            lowerCaseText.contains("வருவாய்") ||
            lowerCaseText.contains("சந்தை") ||
            lowerCaseText.contains("விலை") ||
            lowerCaseText.contains("வருமானம்")) {
            // நிதிக் கேள்வி எனில், முழு பகுப்பாய்வு pipeline-ஐ இயக்குகிறது.
            viewModelScope.launch {
                runFinancialAnalysis(text)
            }
        } else {
            // சாதாரண கேள்வி எனில், நேரடியாக Gemma-வை அழைக்கிறது.
            inferenceModel.generateResponseAsync(userMessage.text, ModelType.GEMMA)
        }
    }

    /**
     * நிதிக் கணிப்பு மற்றும் பதிலுக்கான முழுமையான pipeline.
     */
    private suspend fun runFinancialAnalysis(prompt: String) {
        // படி 1: Gemini API-ஐப் பயன்படுத்தி தகவல்களைப் பிரித்தெடுக்கிறது.
        val extractedDataJson = withContext(Dispatchers.IO) {
            GeminiApiClient.extractFinancialData(prompt)
        }

        if (extractedDataJson == null) {
            inferenceModel.generateResponseAsync("மன்னிக்கவும், உங்கள் கேள்வியில் உள்ள நிதித் தகவல்களைப் புரிந்துகொள்ள முடியவில்லை.", ModelType.GEMMA)
            _uiState.update { it.copy(isLLMGenerating = false) }
            return
        }

        val extractedData = try {
            val jsonObject = Gson().fromJson(extractedDataJson, com.google.gson.JsonObject::class.java)
            val area = jsonObject.get("area_acres")?.asFloat ?: 0f
            val fertilizer = jsonObject.get("fertilizer_kg")?.asFloat ?: 0f
            val otherCosts = jsonObject.get("other_costs_rs")?.asFloat ?: 0f

            // TFLiteFinancialModel-க்கு தேவையான சரியான உள்ளீட்டை இங்கே உருவாக்கவும்.
            // இந்த பட்டியல் TFLiteFinancialModel-இன் உள்ளீட்டு வடிவத்திற்கு பொருந்த வேண்டும்.
            // இது ஒரு உதாரணம் மட்டுமே, உன் மாடலுக்கு ஏற்ப மாற்ற வேண்டும்.
            val inputList = mutableListOf(area)
            inputList.addAll(List(21) { 0f }) // மற்ற 21 உள்ளீடுகளையும் 0 ஆக அமைக்கவும்.
            inputList.add(21, fertilizer)
            inputList.add(22, otherCosts)

            inputList
        } catch (e: Exception) {
            Log.e("ChatScreenViewModel", "Error parsing extracted data: ${e.message}")
            inferenceModel.generateResponseAsync("மன்னிக்கவும், பிரித்தெடுக்கப்பட்ட தரவுகளைப் பகுப்பாய்வு செய்ய முடியவில்லை.", ModelType.GEMMA)
            _uiState.update { it.copy(isLLMGenerating = false) }
            return
        }

        // படி 2: TFLiteFinancialModel-இன் கணிப்பைச் செய்கிறது.
        val prediction = tfliteFinancialModel.predict(extractedData)
        if (prediction == null) {
            inferenceModel.generateResponseAsync("கணிப்பு செய்யும்போது பிழை ஏற்பட்டது.", ModelType.GEMMA)
            _uiState.update { it.copy(isLLMGenerating = false) }
            return
        }

        // கதை சொல்லும் அணுகுமுறைக்கு கூடுதல் தகவல்களைச் சேர்க்கிறோம்.
        // இந்தத் தகவல்களை உண்மையான பயனரின் சுயவிவரத்தில் இருந்து எடுக்க வேண்டும்.
        // இப்போது, சோதனைக்காக ஒரு போலித் தரவைப் பயன்படுத்துகிறோம்.
        val farmerName = "சுரேஷ்"
        val cropType = "தக்காளி"
        val landType = "செம்மண்"

        // படி 3: கணிப்பு முடிவுகளை மீண்டும் Gemini API-ஐப் பயன்படுத்தி மனித மொழியில் மாற்றுகிறது.
        val formattedResponse = withContext(Dispatchers.IO) {
            GeminiApiClient.formatFinancialResponse(
                farmerName,
                cropType,
                landType,
                prediction
            )
        }

        if (formattedResponse != null) {
            inferenceModel.generateResponseAsync(formattedResponse, ModelType.GEMMA)
        } else {
            inferenceModel.generateResponseAsync("மன்னிக்கவும், பதிலைத் உருவாக்க முடியவில்லை.", ModelType.GEMMA)
        }
    }

    // இனிவரும் ஃபங்ஷன்கள் மாறாமல் அப்படியே இருக்கும்.
    fun addFile(uri: Uri) {
        val fileMessage = Message("", "user", System.currentTimeMillis(), uri)
        _uiState.update { it.copy(messages = it.messages + fileMessage) }
    }

    fun handleVoiceRecognitionResult(result: ActivityResult) {
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!matches.isNullOrEmpty()) {
            handleUserInput(matches[0])
        }
    }

    fun startNewChat() {
        if (uiState.value.messages.isNotEmpty()) {
            saveChat()
        }
        _uiState.update { it.copy(messages = emptyList()) }
    }

    fun saveChat() {
        if (uiState.value.messages.isEmpty()) return
        val json = sharedPreferences.getString("chat_history", null)
        val type = object : TypeToken<MutableList<List<Message>>>() {}.type
        val history: MutableList<List<Message>> = if (json != null) {
            gson.fromJson(json, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        history.add(uiState.value.messages)
        val updatedJson = gson.toJson(history)
        sharedPreferences.edit().putString("chat_history", updatedJson).apply()
        loadChatHistory()
    }

    private fun loadChatHistory() {
        val json = sharedPreferences.getString("chat_history", null)
        val history = if (json != null) {
            val type = object : TypeToken<List<List<Message>>>() {}.type
            gson.fromJson<List<List<Message>>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
        _uiState.update { it.copy(chatHistory = history) }
    }

    fun loadChat(chat: List<Message>) {
        _uiState.update { it.copy(messages = chat) }
    }

    fun deleteChatHistory() {
        sharedPreferences.edit().remove("chat_history").apply()
        _uiState.update { it.copy(messages = emptyList(), chatHistory = emptyList()) }
    }

    // ViewModel அழிக்கப்படும்போது மாடல்களை மூடுகிறது.
    override fun onCleared() {
        super.onCleared()
        inferenceModel.close()
        tfliteFinancialModel.close()
    }
}

class ChatViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatScreenViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
