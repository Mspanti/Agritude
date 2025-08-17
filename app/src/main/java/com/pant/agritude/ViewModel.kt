package com.pant.agritude

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import com.pant.agritude.ModelType

data class UiState(
    val llmResponse: String = "",
    val isLLMGenerating: Boolean = false,
    val financialPrediction: FinancialPrediction? = null,
    val isFinancialModelRunning: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val inferenceModel = InferenceModel.getInstance(application.applicationContext)
    private val tfliteModel = TFLiteFinancialModel(application.applicationContext)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        tfliteModel.initialize()

        // LLM இலிருந்து பகுதியளவு முடிவுகளைச் சேகரித்து UI நிலையைப் புதுப்பிக்கிறது
        viewModelScope.launch {
            inferenceModel.partialResults.collect { (partialResult, done) ->
                _uiState.update { currentState ->
                    val newResponse = currentState.llmResponse + partialResult
                    currentState.copy(llmResponse = newResponse, isLLMGenerating = !done)
                }
            }
        }
    }

    fun generateResponse(prompt: String) {
        // முந்தைய பதிலை நீக்கிவிட்டு புதிய உருவாக்கத்தைத் தொடங்குகிறது
        _uiState.update { it.copy(llmResponse = "", isLLMGenerating = true) }
        inferenceModel.generateResponseAsync(prompt , ModelType.GEMMA)
    }

    fun runFinancialModel(inputData: List<Float>) {
        _uiState.update { it.copy(isFinancialModelRunning = true) }
        viewModelScope.launch {
            try {
                val prediction = tfliteModel.predict(inputData)
                _uiState.update { it.copy(financialPrediction = prediction, isFinancialModelRunning = false) }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error running financial model", e)
                _uiState.update { it.copy(isFinancialModelRunning = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        inferenceModel.close()
        tfliteModel.close()
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}