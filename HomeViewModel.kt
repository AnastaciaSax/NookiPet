package eng.nookipet.lab7.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eng.nookipet.lab7.data.entity.Event
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val selectedPetRepo: SelectedPetRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    val pet: StateFlow<Pet?> = selectedPetRepo.selected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentEvents: StateFlow<List<Event>> = selectedPetRepo.selected
        .flatMapLatest { p ->
            flow {
                if (p == null) emit(emptyList())
                else eventRepo.getForPet(p.id).collect { emit(it) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Logs a quick action for the current pet.
     * Reads the selected pet inside a coroutine — no reliance on
     * StateFlow.value being populated yet.
     */
    fun logQuickAction(
        type: EventType,
        onSuccess: () -> Unit = {},
        onNoPet: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val current = selectedPetRepo.selected.first()
            if (current == null) { onNoPet(); return@launch }
            eventRepo.insert(
                Event(
                    petId = current.id,
                    type = type.name,
                    title = type.label,
                    amount = "",
                    repeatMode = "Quick action"
                )
            )
            onSuccess()
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch { eventRepo.delete(event) }
    }
}