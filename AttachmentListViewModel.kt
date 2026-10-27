package eng.nookipet.lab7

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import eng.nookipet.lab7.data.repository.AttachmentRepository

class AttachmentListViewModel(
    private val repository: AttachmentRepository
) : ViewModel() {

    init {
        Log.d("MVVM", "AttachmentListViewModel created")
    }

    // stateIn converts a cold Flow from Room into a hot StateFlow.
    // WhileSubscribed(5000) keeps the flow alive for 5 seconds after the last subscriber,
    // so that a screen rotation doesn't trigger a full DB reload.
    val attachments: StateFlow<List<Attachment>> = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAttachment(attachment: Attachment) {
        Log.d("MVVM", "AttachmentListViewModel.addAttachment: ${attachment.name}")
        viewModelScope.launch {
            repository.insert(attachment)
        }
    }

    fun deleteAttachment(attachment: Attachment) {
        Log.d("MVVM", "AttachmentListViewModel.deleteAttachment: ${attachment.name}")
        viewModelScope.launch {
            repository.delete(attachment)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("MVVM", "AttachmentListViewModel.onCleared() — ViewModel destroyed")
    }
}