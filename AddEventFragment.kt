package eng.nookipet.lab7.ui.event

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import eng.nookipet.lab7.ui.theme.applyPetPalette

/**
 * Full form for logging a new event.
 * Saves into Room, optionally schedules a WorkManager notification.
 * Returns to the previous screen after save.
 */
class AddEventFragment : Fragment() {

    private val viewModel: AddEventViewModel by viewModels {
        viewModelFactory {
            initializer {
                val db = AppDatabase.getDatabase(requireContext())
                AddEventViewModel(
                    SelectedPetRepository(requireContext(), db.petDao()),
                    EventRepository(db.eventDao())
                )
            }
        }
    }

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var chipType: ChipGroup
    private lateinit var chipRepeat: ChipGroup
    private lateinit var switchNotify: MaterialSwitch

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_event, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyPetPalette(view)

        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        val btnSave: MaterialButton = view.findViewById(R.id.btnSave)
        etTitle = view.findViewById(R.id.etTitle)
        etAmount = view.findViewById(R.id.etAmount)
        etDate = view.findViewById(R.id.etDate)
        chipType = view.findViewById(R.id.chipGroupType)
        chipRepeat = view.findViewById(R.id.chipGroupRepeat)
        switchNotify = view.findViewById(R.id.switchNotify)

        // Pre-fill today's date
        etDate.setText(dateFormat.format(Calendar.getInstance().time))

        // Tap on the date field → open DatePickerDialog
        etDate.setOnClickListener { showDatePicker() }

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnSave.setOnClickListener { save() }
    }

    // ──────────────────────────────────────────────────────────────

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, y)
                    set(Calendar.MONTH, m)
                    set(Calendar.DAY_OF_MONTH, d)
                }
                etDate.setText(dateFormat.format(picked.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /** Which chip is selected in a ChipGroup → its text */
    private fun selectedChipText(group: ChipGroup): String =
        group.children
            .filterIsInstance<Chip>()
            .firstOrNull { it.isChecked }
            ?.text?.toString()
            ?: ""

    private fun save() {
        // Event type: map chip → EventType
        val typeChipText = selectedChipText(chipType)
        val type = when {
            typeChipText.contains("Feeding") -> EventType.FEEDING
            typeChipText.contains("Misting") -> EventType.MISTING
            typeChipText.contains("Cleaning") -> EventType.CLEANING
            typeChipText.contains("Meds") -> EventType.MEDS
            typeChipText.contains("Vet") -> EventType.VET
            else -> EventType.OTHER
        }

        val title = etTitle.text?.toString()?.trim().orEmpty()
        val amount = etAmount.text?.toString()?.trim().orEmpty()
        val dateLabel = etDate.text?.toString().orEmpty()
        val repeatMode = selectedChipText(chipRepeat)

        val notifyDelay = if (switchNotify.isChecked) 10L else 0L

        viewModel.saveEvent(
            type = type,
            title = title,
            amount = amount,
            dateLabel = dateLabel,
            repeatMode = repeatMode,
            notifyInSeconds = notifyDelay,
            appContext = requireContext().applicationContext,
            onNoPet = {
                Toast.makeText(requireContext(), "Add a pet first", Toast.LENGTH_SHORT).show()
            },
            onDone = {
                Toast.makeText(
                    requireContext(),
                    if (notifyDelay > 0) "Saved. Notification in 10s" else "Saved",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        )
    }
}