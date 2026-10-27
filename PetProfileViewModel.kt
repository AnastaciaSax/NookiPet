package eng.nookipet.lab7.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eng.nookipet.lab7.data.content.CareGuide
import eng.nookipet.lab7.data.content.PetCareGuide
import eng.nookipet.lab7.data.entity.Event
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.PetRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetProfileViewModel(
    private val selectedPetRepo: SelectedPetRepository,
    private val petRepo: PetRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    val pet: StateFlow<Pet?> = selectedPetRepo.selected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val events: StateFlow<List<Event>> = selectedPetRepo.selected
        .flatMapLatest { p ->
            flow {
                if (p == null) emit(emptyList())
                else eventRepo.getForPet(p.id).collect { emit(it) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val careGuide: StateFlow<CareGuide?> = pet
        .map { p -> p?.let { PetCareGuide.forPet(it.species, it.breed) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updatePet(pet: Pet) {
        viewModelScope.launch { petRepo.update(pet) }
    }

    /**
     * Logs a weight measurement: saves an Event with weightG > 0
     * AND updates Pet.weightG to the latest value.
     * That way the profile shows the current weight and Stats builds a real trend.
     */
    fun logWeight(weightG: Int) {
        val current = pet.value ?: return
        viewModelScope.launch {
            eventRepo.insert(
                Event(
                    petId = current.id,
                    type = EventType.OTHER.name,
                    title = "Weight",
                    amount = "$weightG g",
                    weightG = weightG
                )
            )
            petRepo.update(current.copy(weightG = weightG))
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch { eventRepo.delete(event) }
    }
}