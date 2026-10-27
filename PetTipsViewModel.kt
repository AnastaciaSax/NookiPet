package eng.nookipet.lab7.ui.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eng.nookipet.lab7.ApiService
import eng.nookipet.lab7.data.entity.PetTip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException

/**
 * UI state for the tips screen. One of three states at any time.
 */
sealed class TipsUiState {
    object Loading : TipsUiState()
    data class Success(val tips: List<PetTip>) : TipsUiState()
    data class Error(val message: String) : TipsUiState()
}

class PetTipsViewModel : ViewModel() {

    private val _state = MutableStateFlow<TipsUiState>(TipsUiState.Loading)
    val state: StateFlow<TipsUiState> = _state

    init { loadTips() }

    fun loadTips() {
        viewModelScope.launch {
            _state.value = TipsUiState.Loading
            try {
                val response = ApiService.petTipsApi.getTips()
                if (response.isSuccessful) {
                    _state.value = TipsUiState.Success(response.body() ?: emptyList())
                } else {
                    _state.value = TipsUiState.Error("Server error: ${response.code()}")
                }
            } catch (e: IOException) {
                _state.value = TipsUiState.Error("No internet connection")
            } catch (e: HttpException) {
                _state.value = TipsUiState.Error("HTTP error: ${e.code()}")
            } catch (e: Exception) {
                _state.value = TipsUiState.Error("Unknown error: ${e.message}")
            }
        }
    }
}