package eng.nookipet.lab7.ui.pets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.repository.PetRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import eng.nookipet.lab7.data.repository.ReminderRepository
import eng.nookipet.lab7.data.repository.AttachmentRepository
import eng.nookipet.lab7.ui.theme.applyPetPalette
import eng.nookipet.lab7.data.repository.EventRepository

/**
 * Add or edit a pet. Mode is determined by petId argument:
 *   petId = 0 → create new
 *   petId > 0 → load existing, prefill fields, show Delete
 */
class AddPetFragment : Fragment() {

    private val viewModel: AddPetViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                val db = AppDatabase.getDatabase(app)
                AddPetViewModel(
                    app,
                    PetRepository(db.petDao()),
                    SelectedPetRepository(app, db.petDao()),
                    ReminderRepository(db.reminderDao()),
                    AttachmentRepository(db.attachmentDao()),
                    EventRepository(db.eventDao())
                )
            }
        }
    }
    // ── View refs (populated in onViewCreated) ─────────────────────
    private lateinit var etName: TextInputEditText
    private lateinit var etSpecies: TextInputEditText
    private lateinit var etBreed: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var etSize: TextInputEditText
    private lateinit var etHumidity: TextInputEditText
    private lateinit var etTemperature: TextInputEditText
    private lateinit var etDiet: TextInputEditText
    private lateinit var avatarGroup: ChipGroup
    private lateinit var genderGroup: ChipGroup

    private var petId: Long = 0L
    private var existingPet: Pet? = null

    companion object {
        private const val ARG_PET_ID = "pet_id"

        /** Creates fragment for create (petId = 0) or edit (petId > 0). */
        fun newInstance(petId: Long): AddPetFragment {
            val f = AddPetFragment()
            f.arguments = Bundle().apply { putLong(ARG_PET_ID, petId) }
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_pet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyPetPalette(view)

        petId = arguments?.getLong(ARG_PET_ID, 0L) ?: 0L

        val tvTitle: TextView = view.findViewById(R.id.tvFormTitle)
        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        val btnSave: MaterialButton = view.findViewById(R.id.btnSave)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)

        etName = view.findViewById(R.id.etName)
        etSpecies = view.findViewById(R.id.etSpecies)
        etBreed = view.findViewById(R.id.etBreed)
        etAge = view.findViewById(R.id.etAge)
        etWeight = view.findViewById(R.id.etWeight)
        etSize = view.findViewById(R.id.etSize)
        etHumidity = view.findViewById(R.id.etHumidity)
        etTemperature = view.findViewById(R.id.etTemperature)
        etDiet = view.findViewById(R.id.etDiet)
        avatarGroup = view.findViewById(R.id.avatarGroup)
        genderGroup = view.findViewById(R.id.genderGroup)

        val btnLogWeight: MaterialButton = view.findViewById(R.id.btnLogWeight)

        if (petId > 0L) {
            tvTitle.text = "Edit pet"
            btnDelete.visibility = View.VISIBLE
            btnLogWeight.visibility = View.VISIBLE
            loadPetForEdit()
        } else {
            tvTitle.text = "New pet"
            btnDelete.visibility = View.GONE
            btnLogWeight.visibility = View.GONE
        }

        btnLogWeight.setOnClickListener {
            existingPet?.let { showLogWeightDialog(it) }
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnSave.setOnClickListener { save() }

        btnDelete.setOnClickListener {
            existingPet?.let { confirmDelete(it) }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Load & save
    // ──────────────────────────────────────────────────────────────

    private fun loadPetForEdit() {
        viewLifecycleOwner.lifecycleScope.launch {
            val pet = viewModel.loadPet(petId)
            if (pet == null) {
                toast("Pet not found")
                requireActivity().onBackPressedDispatcher.onBackPressed()
                return@launch
            }
            existingPet = pet

            etName.setText(pet.name)
            etSpecies.setText(pet.species)
            etBreed.setText(pet.breed)
            etAge.setText(pet.ageLabel)
            etWeight.setText(pet.weightG.takeIf { it > 0 }?.toString() ?: "")
            etSize.setText(pet.shellLengthCm.takeIf { it > 0f }?.toString() ?: "")
            etHumidity.setText(pet.humidity.takeIf { it > 0 }?.toString() ?: "")
            etTemperature.setText(pet.temperature.takeIf { it > 0 }?.toString() ?: "")
            etDiet.setText(pet.diet)

            // Restore avatar chip
            val avatarChip = avatarGroup.children
                .filterIsInstance<Chip>()
                .firstOrNull { it.text.toString() == pet.avatarEmoji }
            (avatarChip ?: avatarGroup.getChildAt(0) as Chip).isChecked = true

            // Restore gender chip
            genderGroup.children.filterIsInstance<Chip>().forEach { chip ->
                chip.isChecked = chip.text.toString().equals(pet.gender, ignoreCase = true)
            }
        }
    }

    private fun save() {
        val name = etName.text?.toString()?.trim().orEmpty()
        if (name.isBlank()) {
            toast("Please enter a name")
            return
        }

        val species = etSpecies.text?.toString()?.trim().orEmpty()
        val breed = etBreed.text?.toString()?.trim().orEmpty()
        val age = etAge.text?.toString()?.trim().orEmpty()
        val weight = etWeight.text?.toString()?.toIntOrNull() ?: 0
        val size = etSize.text?.toString()?.toFloatOrNull() ?: 0f
        val humidity = etHumidity.text?.toString()?.toIntOrNull() ?: 0
        val temperature = etTemperature.text?.toString()?.toIntOrNull() ?: 0
        val diet = etDiet.text?.toString()?.trim().orEmpty()

        val avatarEmoji = avatarGroup.children
            .filterIsInstance<Chip>()
            .firstOrNull { it.isChecked }
            ?.text?.toString() ?: "🐾"

        val gender = genderGroup.children
            .filterIsInstance<Chip>()
            .firstOrNull { it.isChecked }
            ?.text?.toString() ?: "Unknown"

        val existing = existingPet
        if (existing == null) {
            viewModel.createPet(
                name, species, breed, gender, age,
                weight, size, humidity, temperature,
                diet, avatarEmoji
            ) {
                toast("$name added")
                close()
            }
        } else {
            viewModel.updatePet(
                existing, name, species, breed, gender, age,
                weight, size, humidity, temperature,
                diet, avatarEmoji
            ) {
                toast("$name updated")
                close()
            }
        }
    }

    private fun confirmDelete(pet: Pet) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete ${pet.name}?")
            .setMessage("All events and reminders for ${pet.name} will be removed. This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePet(pet) {
                    toast("${pet.name} deleted")
                    close()
                }
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    private fun close() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    /**
     * Small dialog with one numeric field — weight in grams.
     * On save → AddPetViewModel.logWeight creates an Event
     * AND updates Pet.weightG, so both the profile and the Stats
     * chart reflect the new measurement.
     */
    private fun showLogWeightDialog(pet: Pet) {
        val input = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Weight, g"
            setText(pet.weightG.toString())
            setPadding(48, 32, 48, 32)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Log weight for ${pet.name}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val weight = input.text?.toString()?.toIntOrNull()
                if (weight == null || weight <= 0) {
                    toast("Enter a valid weight")
                    return@setPositiveButton
                }
                viewModel.logWeight(pet, weight) {
                    toast("Weight logged: $weight g")
                    etWeight.setText(weight.toString())
                    existingPet = pet.copy(weightG = weight)
                }
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }
}