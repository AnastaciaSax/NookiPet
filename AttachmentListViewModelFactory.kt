package eng.nookipet.lab7

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import eng.nookipet.lab7.data.repository.AttachmentRepository

class AttachmentListViewModelFactory(
    private val repository: AttachmentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttachmentListViewModel::class.java)) {
            return AttachmentListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}