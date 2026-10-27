package eng.nookipet.lab7.ui.vet

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
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
import com.google.android.material.button.MaterialButton
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import eng.nookipet.lab7.ui.theme.applyPetPalette

/**
 * Vet booking screen.
 *   - pick reason from a list
 *   - pick date + time
 *   - pick clinic from a dropdown
 *   - Book → saves an Event of type VET
 */
class BookVetFragment : Fragment() {

    private val viewModel: VetViewModel by viewModels {
        viewModelFactory {
            initializer {
                val db = AppDatabase.getDatabase(requireContext())
                VetViewModel(
                    SelectedPetRepository(requireContext(), db.petDao()),
                    EventRepository(db.eventDao())
                )
            }
        }
    }

    private lateinit var reasonAdapter: VetReasonAdapter
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var dateLabel: String = ""
    private var timeLabel: String = ""
    private var selectedClinicName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_book_vet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyPetPalette(view)

        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        val tvPetAvatar: TextView = view.findViewById(R.id.tvBookPetAvatar)
        val tvPetName: TextView = view.findViewById(R.id.tvBookPetName)
        val tvPetSub: TextView = view.findViewById(R.id.tvBookPetSub)
        val rvReasons: RecyclerView = view.findViewById(R.id.rvReasons)
        val btnPickDate: MaterialButton = view.findViewById(R.id.btnPickDate)
        val btnPickTime: MaterialButton = view.findViewById(R.id.btnPickTime)
        val btnPickClinic: MaterialButton = view.findViewById(R.id.btnPickClinic)
        val btnNearby: MaterialButton = view.findViewById(R.id.btnNearby)
        val btnBook: MaterialButton = view.findViewById(R.id.btnBook)

        // Pre-fill date + time with current values
        val now = Calendar.getInstance()
        dateLabel = dateFormat.format(now.time)
        timeLabel = timeFormat.format(now.time)
        btnPickDate.text = dateLabel
        btnPickTime.text = timeLabel

        // Reasons
        reasonAdapter = VetReasonAdapter(viewModel.reasons) { /* selection handled internally */ }
        rvReasons.layoutManager = LinearLayoutManager(requireContext())
        rvReasons.adapter = reasonAdapter

        // Active pet info
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pet.collect { pet ->
                    if (pet == null) {
                        tvPetName.text = "No pet"
                        tvPetSub.text = "Add a pet first"
                        tvPetAvatar.text = "🐾"
                    } else {
                        tvPetName.text = pet.name
                        tvPetSub.text = "${pet.breed.ifBlank { pet.species }} · ${pet.weightG} g"
                        tvPetAvatar.text = pet.avatarEmoji.ifBlank { "🐾" }
                    }
                }
            }
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val picked = Calendar.getInstance().apply {
                        set(Calendar.YEAR, y)
                        set(Calendar.MONTH, m)
                        set(Calendar.DAY_OF_MONTH, d)
                    }
                    dateLabel = dateFormat.format(picked.time)
                    btnPickDate.text = dateLabel
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    val picked = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                    }
                    timeLabel = timeFormat.format(picked.time)
                    btnPickTime.text = timeLabel
                },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
            ).show()
        }

        btnPickClinic.setOnClickListener {
            val names = viewModel.clinics.map { it.name }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle("Choose clinic")
                .setItems(names) { _, which ->
                    selectedClinicName = viewModel.clinics[which].name
                    btnPickClinic.text = selectedClinicName
                }
                .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                .show()
        }

        btnNearby.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NearbyClinicsFragment())
                .addToBackStack(null)
                .commit()
        }

        btnBook.setOnClickListener {
            val reason = reasonAdapter.getSelectedReason().title
            val clinic = selectedClinicName.ifBlank { "Not selected" }

            viewModel.saveVisit(
                reason, dateLabel, timeLabel, clinic,
                onNoPet = {
                    Toast.makeText(requireContext(), "Add a pet first", Toast.LENGTH_SHORT).show()
                },
                onDone = {
                    Toast.makeText(
                        requireContext(),
                        "Booked: $reason on $dateLabel at $timeLabel",
                        Toast.LENGTH_LONG
                    ).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            )
        }
    }
}