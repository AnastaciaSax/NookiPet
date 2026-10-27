package eng.nookipet.lab7.ui.reminders

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.repository.ReminderRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import eng.nookipet.lab7.ui.theme.PetPaletteCatalog
import eng.nookipet.lab7.ui.widget.OrbBackgroundView
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import eng.nookipet.lab7.ui.theme.applyPetPalette

/**
 * Reminders screen for the active pet.
 *   - list with toggle switches
 *   - FAB → dialog with title, type chips, TimePicker
 *   - long-press → delete confirmation
 *
 * Toggling a switch writes to Room immediately, so state survives restart.
 */
class RemindersFragment : Fragment() {

    private val viewModel: RemindersViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                val db = AppDatabase.getDatabase(app)
                RemindersViewModel(
                    app,
                    SelectedPetRepository(app, db.petDao()),
                    ReminderRepository(db.reminderDao())
                )
            }
        }
    }

    private lateinit var adapter: ReminderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_reminders, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyPetPalette(view)

        val orbBg: OrbBackgroundView = view.findViewById(R.id.orbBackground)
        val tvSub: TextView = view.findViewById(R.id.tvRemindersSub)
        val rv: RecyclerView = view.findViewById(R.id.rvReminders)
        val emptyState: View = view.findViewById(R.id.emptyState)
        val fab: FloatingActionButton = view.findViewById(R.id.fabAddReminder)

        adapter = ReminderAdapter(
            onToggle = { reminder, enabled -> viewModel.toggle(reminder.id, enabled) },
            onLongClick = { reminder -> confirmDelete(reminder) }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Active pet — subtitle + orb palette
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pet.collect { pet ->
                    if (pet == null) {
                        tvSub.text = "Add a pet first"
                        orbBg.setPalette(PetPaletteCatalog.MINT)
                    } else {
                        tvSub.text = "For ${pet.name}"
                        orbBg.setPalette(PetPaletteCatalog.byId(pet.paletteId))
                    }
                }
            }
        }

        // Reminders list
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reminders.collect { list ->
                    adapter.submitList(list)
                    emptyState.isVisible = list.isEmpty()
                }
            }
        }

        fab.setOnClickListener { showAddDialog() }
    }

    // ──────────────────────────────────────────────────────────────
    // Add reminder dialog
    // ──────────────────────────────────────────────────────────────

    private fun showAddDialog() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        // Apply active pet's palette to the dialog's view tree
        val db = AppDatabase.getDatabase(requireContext())
        val repo = eng.nookipet.lab7.data.repository.SelectedPetRepository(requireContext(), db.petDao())
        viewLifecycleOwner.lifecycleScope.launch {
            repo.selected.collect { pet ->
                val palette = eng.nookipet.lab7.ui.theme.PetPaletteCatalog.byId(pet?.paletteId)
                eng.nookipet.lab7.ui.theme.PaletteApplier.applyToTree(dialogView, palette)
            }
        }

        val etTitle: TextInputEditText = dialogView.findViewById(R.id.etReminderTitle)
        val chipGroup: ChipGroup = dialogView.findViewById(R.id.chipGroupReminderType)
        val btnPickTime: MaterialButton = dialogView.findViewById(R.id.btnPickTime)

        // Default time — 10:00. Updated by the TimePicker.
        var hour = 10
        var minute = 0
        btnPickTime.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

        btnPickTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    hour = h
                    minute = m
                    btnPickTime.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                },
                hour, minute, true
            ).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("New reminder")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text?.toString()?.trim().orEmpty()
                if (title.isBlank()) {
                    Toast.makeText(requireContext(), "Enter a title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val chipText = chipGroup.children
                    .filterIsInstance<Chip>()
                    .firstOrNull { it.isChecked }
                    ?.text?.toString() ?: "🐾 Other"

                val type = when {
                    chipText.contains("Feeding") -> EventType.FEEDING
                    chipText.contains("Misting") -> EventType.MISTING
                    chipText.contains("Cleaning") -> EventType.CLEANING
                    chipText.contains("Meds") -> EventType.MEDS
                    chipText.contains("Vet") -> EventType.VET
                    else -> EventType.OTHER
                }

                val schedule = String.format(
                    Locale.getDefault(),
                    "Every day at %02d:%02d",
                    hour, minute
                )

                viewModel.addReminder(
                    title, schedule, type.name, hour, minute,
                    onNoPet = {
                        Toast.makeText(requireContext(), "Add a pet first", Toast.LENGTH_SHORT).show()
                    }
                )
                Toast.makeText(requireContext(), "Reminder added", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    // ──────────────────────────────────────────────────────────────
    // Delete confirmation
    // ──────────────────────────────────────────────────────────────

    private fun confirmDelete(reminder: eng.nookipet.lab7.data.entity.Reminder) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete reminder?")
            .setMessage("\"${reminder.title}\" will be removed.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(reminder)
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }
}