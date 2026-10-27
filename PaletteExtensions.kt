package eng.nookipet.lab7.ui.theme

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.launch

/**
 * Call once in Fragment.onViewCreated().
 * Subscribes to the active pet and repaints the whole view tree
 * with its palette whenever the pet changes.
 *
 * Thanks to the shared selectedPetIdFlow in AppPreferences, every
 * Fragment sees the change immediately.
 */
fun Fragment.applyPetPalette(view: View) {
    val db = AppDatabase.getDatabase(requireContext())
    val repo = SelectedPetRepository(requireContext(), db.petDao())

    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            repo.selected.collect { pet ->
                val palette = PetPaletteCatalog.byId(pet?.paletteId)
                PaletteApplier.applyToTree(view, palette)
            }
        }
    }
}