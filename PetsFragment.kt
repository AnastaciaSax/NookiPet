package eng.nookipet.lab7.ui.pets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.repository.PetRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import eng.nookipet.lab7.ui.widget.OrbBackgroundView
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import eng.nookipet.lab7.data.repository.ReminderRepository
import eng.nookipet.lab7.data.repository.AttachmentRepository
import eng.nookipet.lab7.ui.theme.applyPetPalette

/**
 * Pets tab: list of all pets, active pet highlighted, options via long press.
 * Tapping a pet → becomes active across the whole app.
 * Long press → menu with Set active / Edit / Delete.
 */
class PetsFragment : Fragment() {

    private val viewModel: PetsViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                val db = AppDatabase.getDatabase(app)
                PetsViewModel(
                    app,
                    PetRepository(db.petDao()),
                    SelectedPetRepository(app, db.petDao()),
                    ReminderRepository(db.reminderDao()),
                    AttachmentRepository(db.attachmentDao())
                )
            }
        }
    }

    private lateinit var adapter: PetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_pets, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyPetPalette(view)

        val rv: RecyclerView = view.findViewById(R.id.rvPets)
        val fab: FloatingActionButton = view.findViewById(R.id.fabAddPet)
        val emptyState: View = view.findViewById(R.id.emptyState)

        adapter = PetAdapter(
            selectedId = { viewModel.selectedId.value },
            onPetClick = { pet -> viewModel.select(pet); toast("${pet.name} is now active") },
            onPetLongClick = { pet -> showOptionsDialog(pet) }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Re-bind items when selection changes — to move the "Active" badge
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedId.collect { adapter.notifyDataSetChanged() }
            }
        }

        // Subscribe to the pets list
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pets.collect { list ->
                    adapter.submitList(list)
                    emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        fab.setOnClickListener { openAddPet() }
    }

    /** Long-press menu: Set active / Edit / Delete. */
    private fun showOptionsDialog(pet: Pet) {
        val isActive = pet.id == viewModel.selectedId.value
        val isOnlyOne = viewModel.pets.value.size <= 1

        // Build the label list depending on state
        val labels = mutableListOf<String>()
        if (!isActive) labels.add("Set as active")
        labels.add("Edit")
        labels.add("Documents")

        val deleteLabel = if (isOnlyOne)
            "Delete (cannot — this is the only pet)"
        else
            "Delete"
        labels.add(deleteLabel)

        AlertDialog.Builder(requireContext())
            .setTitle(pet.name)
            .setItems(labels.toTypedArray()) { _, which ->
                val chosen = labels[which]
                when {
                    chosen == "Set as active" -> {
                        viewModel.select(pet)
                        toast("${pet.name} is now active")
                    }
                    chosen == "Edit" -> openEditPet(pet.id)
                    chosen == "Documents" -> openDocuments(pet)
                    chosen.startsWith("Delete") && !isOnlyOne -> confirmDelete(pet)
                    chosen.startsWith("Delete") -> toast("Cannot delete the only pet")
                }
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    /** Second confirmation before cascade-deleting events and reminders. */
    private fun confirmDelete(pet: Pet) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete ${pet.name}?")
            .setMessage("All events and reminders for ${pet.name} will also be removed. This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(pet)
                toast("${pet.name} deleted")
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    private fun openAddPet() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, AddPetFragment.newInstance(petId = 0L))
            .addToBackStack(null)
            .commit()
    }

    private fun openEditPet(petId: Long) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, AddPetFragment.newInstance(petId))
            .addToBackStack(null)
            .commit()
    }
    private fun openDocuments(pet: Pet) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, eng.nookipet.lab7.ui.documents.DocumentsFragment.newInstance(pet.id, pet.name))
            .addToBackStack(null)
            .commit()
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}