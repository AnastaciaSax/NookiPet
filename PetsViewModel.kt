package eng.nookipet.lab7.ui.pets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.repository.AttachmentRepository
import eng.nookipet.lab7.data.repository.PetRepository
import eng.nookipet.lab7.data.repository.ReminderRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetsViewModel(
    app: Application,
    private val petRepo: PetRepository,
    private val selectedPetRepo: SelectedPetRepository,
    private val reminderRepo: ReminderRepository,
    private val attachmentRepo: AttachmentRepository
) : AndroidViewModel(app) {

    val pets: StateFlow<List<Pet>> = petRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedId: StateFlow<Long> = selectedPetRepo.selected
        .map { it?.id ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun select(pet: Pet) {
        selectedPetRepo.select(pet.id)
    }

    fun delete(pet: Pet) {
        viewModelScope.launch {
            val wm = WorkManager.getInstance(getApplication())
            reminderRepo.getForPetOnce(pet.id).forEach { r ->
                wm.cancelUniqueWork("reminder_${r.id}")
            }
            attachmentRepo.deleteAllForPet(pet.id)
            petRepo.delete(pet)
        }
    }
}