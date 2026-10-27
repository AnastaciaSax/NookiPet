package eng.nookipet.lab7.ui.pets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.repository.PetRepository
import eng.nookipet.lab7.data.repository.ReminderRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import eng.nookipet.lab7.ui.theme.PetPaletteCatalog
import kotlinx.coroutines.launch
import eng.nookipet.lab7.data.repository.AttachmentRepository
import eng.nookipet.lab7.data.entity.Event
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.repository.EventRepository

/**
 * Backs the Add/Edit pet screen.
 *
 * On delete — cancels WorkManager notifications for all reminders
 * of the pet before removing it. Without this, notification tasks
 * would still fire after the pet is gone.
 */

class AddPetViewModel(
    app: Application,
    private val petRepo: PetRepository,
    private val selectedPetRepo: SelectedPetRepository,
    private val reminderRepo: ReminderRepository,
    private val attachmentRepo: AttachmentRepository,
    private val eventRepo: EventRepository
) : AndroidViewModel(app) {

    suspend fun loadPet(petId: Long): Pet? = petRepo.getByIdOnce(petId)

    fun createPet(
        name: String,
        species: String,
        breed: String,
        gender: String,
        ageLabel: String,
        weightG: Int,
        shellLengthCm: Float,
        humidity: Int,
        temperature: Int,
        diet: String,
        avatarEmoji: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val petId = petRepo.insert(
                Pet(
                    name = name,
                    species = species,
                    breed = breed,
                    gender = gender,
                    ageLabel = ageLabel,
                    weightG = weightG,
                    shellLengthCm = shellLengthCm,
                    humidity = humidity,
                    temperature = temperature,
                    diet = diet,
                    paletteId = guessPalette(species, breed),
                    avatarEmoji = avatarEmoji
                )
            )
            selectedPetRepo.selectNewlyCreated(petId)
            onDone()
        }
    }

    fun updatePet(
        existing: Pet,
        name: String,
        species: String,
        breed: String,
        gender: String,
        ageLabel: String,
        weightG: Int,
        shellLengthCm: Float,
        humidity: Int,
        temperature: Int,
        diet: String,
        avatarEmoji: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val updated = existing.copy(
                name = name,
                species = species,
                breed = breed,
                gender = gender,
                ageLabel = ageLabel,
                weightG = weightG,
                shellLengthCm = shellLengthCm,
                humidity = humidity,
                temperature = temperature,
                diet = diet,
                paletteId = guessPalette(species, breed),
                avatarEmoji = avatarEmoji
            )
            petRepo.update(updated)
            onDone()
        }
    }

    /**
     * Deletes the pet.
     * Before the Room CASCADE removes events and reminders, cancels
     * all WorkManager notification tasks for the pet's reminders.
     */
    fun deletePet(pet: Pet, onDone: () -> Unit) {
        viewModelScope.launch {
            val reminders = reminderRepo.getForPetOnce(pet.id)
            val wm = WorkManager.getInstance(getApplication())
            reminders.forEach { r ->
                wm.cancelUniqueWork("reminder_${r.id}")
            }
            attachmentRepo.deleteAllForPet(pet.id)
            petRepo.delete(pet)
            onDone()
        }
    }

    private fun guessPalette(species: String, breed: String): String {
        val haystack = "$species $breed".lowercase()
        return when {
            "cat" in haystack || "felis" in haystack -> PetPaletteCatalog.PEACH.id
            "dog" in haystack || "canine" in haystack -> PetPaletteCatalog.PEACH.id
            "reptile" in haystack || "gecko" in haystack ||
                    "lizard" in haystack || "snake" in haystack -> PetPaletteCatalog.FOREST.id
            "fish" in haystack || "aquarium" in haystack -> PetPaletteCatalog.OCEAN.id
            "bird" in haystack || "parrot" in haystack -> PetPaletteCatalog.SUNSET.id
            else -> PetPaletteCatalog.MINT.id
        }
    }
    /**
     * Logs a new weight measurement:
     *   1. Creates an Event of type OTHER with weightG > 0
     *   2. Updates Pet.weightG to the same value
     *
     * Event has a real timestamp = now, so the Stats weight chart
     * immediately shows this point on the right side of the graph.
     */
    fun logWeight(pet: Pet, weightG: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            eventRepo.insert(
                Event(
                    petId = pet.id,
                    type = EventType.OTHER.name,
                    title = "Weight",
                    amount = "$weightG g",
                    timestamp = System.currentTimeMillis(),
                    weightG = weightG
                )
            )
            petRepo.update(pet.copy(weightG = weightG))
            onDone()
        }
    }
}