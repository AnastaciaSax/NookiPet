package eng.nookipet.lab7.ui.home

import android.graphics.drawable.GradientDrawable
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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import eng.nookipet.lab7.ui.theme.PetPaletteCatalog
import eng.nookipet.lab7.ui.widget.OrbBackgroundView
import kotlinx.coroutines.launch
import eng.nookipet.lab7.AppPreferences
import eng.nookipet.lab7.ui.theme.applyPetPalette

/**
 * Home screen.
 *   - greeting + avatar
 *   - gradient pet card (colors from selected pet's palette)
 *   - 4 quick actions — one tap logs an event
 *   - recent events feed
 *   - FAB — pick event type from dialog, logs immediately
 *
 * All data comes from HomeViewModel. No direct DB / network access here.
 */
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        viewModelFactory {
            initializer {
                val db = AppDatabase.getDatabase(requireContext())
                HomeViewModel(
                    SelectedPetRepository(requireContext(), db.petDao()),
                    EventRepository(db.eventDao())
                )
            }
        }
    }

    private lateinit var adapter: EventAdapter
    private lateinit var orbBg: OrbBackgroundView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyPetPalette(view)

        orbBg = view.findViewById(R.id.orbBackground)
        val tvGreetingSub: TextView = view.findViewById(R.id.tvGreetingSub)
        val tvGreeting: TextView = view.findViewById(R.id.tvGreeting)
        val tvPetName: TextView = view.findViewById(R.id.tvPetName)
        val tvPetMeta: TextView = view.findViewById(R.id.tvPetMeta)
        val tvPetAvatar: TextView = view.findViewById(R.id.tvPetAvatar)
        val cardPetInner: View = view.findViewById(R.id.cardPetInner)
        val tvEmptyEvents: TextView = view.findViewById(R.id.tvEmptyEvents)
        val rvEvents: RecyclerView = view.findViewById(R.id.rvEvents)
        val fab: FloatingActionButton = view.findViewById(R.id.fabAddEvent)

        val actionFeeding: MaterialCardView = view.findViewById(R.id.actionFeeding)
        val actionCleaning: MaterialCardView = view.findViewById(R.id.actionCleaning)
        val actionVitamins: MaterialCardView = view.findViewById(R.id.actionVitamins)
        val actionVet: MaterialCardView = view.findViewById(R.id.actionVet)

        // Events list
        adapter = EventAdapter(
            onEventLongClick = { event ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete event?")
                    .setMessage("\"${event.title}\" will be removed.")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteEvent(event)
                        Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                    .show()
            }
        )
        rvEvents.layoutManager = LinearLayoutManager(requireContext())
        rvEvents.adapter = adapter

        // Greeting reacts to user name changes from Settings.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppPreferences.userNameFlow(requireContext()).collect { name ->
                    tvGreeting.text = if (name.isBlank()) "Hello 👋" else "Hi, $name 👋"
                }
            }
        }

        // Observe the active pet → update header, card, orbs
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pet.collect { pet ->
                    if (pet == null) {
                        tvGreetingSub.text = "Add your first pet to start"
                        tvPetName.text = "No pet yet"
                        tvPetMeta.text = "Open the Pets tab"
                        tvPetAvatar.text = "🐾"
                    } else {
                        tvGreetingSub.text = "How is ${pet.name} today?"
                        tvPetName.text = pet.name
                        val breed = pet.breed.ifBlank { pet.species }
                        val age = pet.ageLabel.ifBlank { "age unknown" }
                        tvPetMeta.text = "$breed · $age"
                        tvPetAvatar.text = pet.avatarEmoji.ifBlank { "🐾" }

                        // Apply palette: orbs + gradient of the pet card
                        val palette = PetPaletteCatalog.byId(pet.paletteId)
                        // Explicitly recolor the FAB — PaletteApplier already does this,
// but this makes the color update instant, without waiting for
// the tree traversal in a separate coroutine.
                        view.findViewById<FloatingActionButton>(R.id.fabAddEvent)
                            .backgroundTintList = android.content.res.ColorStateList.valueOf(palette.primary)
                        orbBg.setPalette(palette)

                        val gradient = GradientDrawable(
                            GradientDrawable.Orientation.TL_BR,
                            intArrayOf(
                                palette.primaryLight,
                                palette.primary,
                                palette.secondary
                            )
                        ).apply {
                            cornerRadius = 22f * resources.displayMetrics.density
                        }
                        cardPetInner.background = gradient
                    }
                }
            }
        }

        // Observe recent events
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentEvents.collect { list ->
                    adapter.submitList(list)
                    tvEmptyEvents.isVisible = list.isEmpty()
                }
            }
        }

        // Quick actions — log an event in one tap
        actionFeeding.setOnClickListener { quick(EventType.FEEDING) }
        actionCleaning.setOnClickListener { quick(EventType.CLEANING) }
        actionVitamins.setOnClickListener { quick(EventType.MEDS) }
        actionVet.setOnClickListener { quick(EventType.VET) }

        // FAB — dialog to pick event type
        fab.setOnClickListener { openAddEvent() }
    }

    private fun quick(type: EventType) {
        viewModel.logQuickAction(
            type,
            onSuccess = { Toast.makeText(requireContext(), "${type.label} logged", Toast.LENGTH_SHORT).show() },
            onNoPet = { Toast.makeText(requireContext(), "Add a pet first", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun openAddEvent() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, eng.nookipet.lab7.ui.event.AddEventFragment())
            .addToBackStack(null)
            .commit()
    }
}