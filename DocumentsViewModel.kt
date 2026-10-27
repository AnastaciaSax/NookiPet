package eng.nookipet.lab7.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eng.nookipet.lab7.Attachment
import eng.nookipet.lab7.data.repository.AttachmentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the Documents screen for one specific pet.
 * The petId is passed at construction time — no global selection here,
 * because documents are always viewed in the context of one pet.
 */
class DocumentsViewModel(
    private val petId: Long,
    private val repo: AttachmentRepository
) : ViewModel() {

    val documents: StateFlow<List<Attachment>> = repo.getForPet(petId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDocument(name: String, sizeKb: Int, type: String, notes: String, fileName: String) {
        viewModelScope.launch {
            repo.insert(
                Attachment(
                    name = name,
                    sizeKb = sizeKb,
                    petId = petId,
                    type = type,
                    notes = notes,
                    fileName = fileName
                )
            )
        }
    }

    fun deleteDocument(attachment: Attachment) {
        viewModelScope.launch { repo.delete(attachment) }
    }
}