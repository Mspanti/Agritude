package com.pant.agritude

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pant.agritude.data.MarketDataResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


sealed class MarketDataState {
    object Loading : MarketDataState()
    data class Success(val data: MarketDataResponse) : MarketDataState()
    data class Error(val message: String) : MarketDataState()
}

class DashboardViewModel(private val repository: AgriTudeRepository) : ViewModel() {

    private val _marketData = MutableStateFlow<MarketDataState>(MarketDataState.Loading)
    val marketData: StateFlow<MarketDataState> = _marketData.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                try {
                    repository.getMarketPrices().collect { response ->
                        // API-இலிருந்து வெற்றிகரமான பதில் வந்தால் மட்டுமே தரவைக் காட்டுகிறோம்
                        _marketData.value = MarketDataState.Success(response)
                    }
                } catch (e: Exception) {
                    _marketData.value = MarketDataState.Error(e.localizedMessage ?: "ஒரு தெரியாத பிழை ஏற்பட்டது.")
                }
                delay(15000) // 15 வினாடிகளுக்கு ஒருமுறை புதுப்பிக்கவும்
            }
        }
    }
}

// DashboardViewModel-ஐ சரியாக உருவாக்க இந்த Factory தேவை.
class DashboardViewModelFactory(private val repository: AgriTudeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
