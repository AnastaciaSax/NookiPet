package eng.nookipet.lab7.data.repository

import android.content.Context
import eng.nookipet.lab7.AppPreferences
import eng.nookipet.lab7.data.dao.PetDao
import eng.nookipet.lab7.data.entity.Pet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Single source of truth for "which pet is currently active".
 *
 * Combines two sources:
 *   - Room: the full list of pets
 *   - AppPreferences.selectedPetIdFlow(): the id of the selected pet
 *
 * Since the id flow is a process-wide singleton, every instance of
 * SelectedPetRepository sees the same selection changes instantly —
 * no matter which Fragment or ViewModel created it.
 */
class SelectedPetRepository(
    private val context: Context,
    private val petDao: PetDao
) {

    /**
     * Current pet. Re-emits when:
     *   - the pet list changes (insert / delete)
     *   - the user selects another pet
     */
    val selected: Flow<Pet?> = combine(
        petDao.getAll(),
        AppPreferences.selectedPetIdFlow(context)
    ) { allPets, selectedId ->
        allPets.firstOrNull { it.id == selectedId } ?: allPets.firstOrNull()
    }

    /** Switches the active pet and persists the choice. */
    fun select(petId: Long) {
        AppPreferences.setSelectedPetId(context, petId)
    }

    /** Used when a new pet is created — immediately becomes selected. */
    fun selectNewlyCreated(petId: Long) = select(petId)
}