package eng.nookipet.lab7.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.button.MaterialButtonToggleGroup
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import eng.nookipet.lab7.ui.theme.PetPaletteCatalog
import eng.nookipet.lab7.ui.widget.BarChartView
import eng.nookipet.lab7.ui.widget.OrbBackgroundView
import kotlinx.coroutines.launch
import eng.nookipet.lab7.ui.theme.applyPetPalette

/**
 * Statistics screen.
 *   - Week / Month / Year period toggle
 *   - Feedings per day chart — aggregated from real Event records
 *   - Weight chart — built from events with weightG > 0 (real data, no stub)
 *
 * Chart colors follow the active pet's palette, same as the rest of the app.
 */
class StatsFragment : Fragment() {

    private val viewModel: StatsViewModel by viewModels {
        viewModelFactory {
            initializer {
                val db = AppDatabase.getDatabase(requireContext())
                StatsViewModel(
                    SelectedPetRepository(requireContext(), db.petDao()),
                    EventRepository(db.eventDao())
                )
            }
        }
    }

    private lateinit var chartFeedings: BarChartView
    private lateinit var chartWeight: BarChartView
    private lateinit var tvStatsSub: TextView
    private lateinit var tvWeightTitle: TextView
    private lateinit var tvWeightHint: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_stats, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyPetPalette(view)

        val orbBg: OrbBackgroundView = view.findViewById(R.id.orbBackground)
        val toggle: MaterialButtonToggleGroup = view.findViewById(R.id.togglePeriod)
        chartFeedings = view.findViewById(R.id.chartFeedings)
        chartWeight = view.findViewById(R.id.chartWeight)
        tvStatsSub = view.findViewById(R.id.tvStatsSub)
        tvWeightTitle = view.findViewById(R.id.tvWeightTitle)
        tvWeightHint = view.findViewById(R.id.tvWeightHint)

        // Period toggle → ViewModel
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val period = when (checkedId) {
                R.id.btnMonth -> StatsPeriod.MONTH
                R.id.btnYear -> StatsPeriod.YEAR
                else -> StatsPeriod.WEEK
            }
            viewModel.setPeriod(period)
        }

        // Pet — subtitle + palette colors on both charts
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pet.collect { pet ->
                    if (pet == null) {
                        tvStatsSub.text = "No pet selected"
                        return@collect
                    }

                    tvStatsSub.text = "${pet.name} · activity overview"

                    val palette = PetPaletteCatalog.byId(pet.paletteId)
                    val labelColor = ContextCompat.getColor(requireContext(), R.color.text_hint)
                    val valueColor = ContextCompat.getColor(requireContext(), R.color.text_primary)

                    chartFeedings.setColors(palette.primary, palette.secondary, labelColor, valueColor)
                    chartWeight.setColors(palette.primary, palette.secondary, labelColor, valueColor)

                    // For pets without shell length (e.g. cats), show "Weight, g"
                    // — for snails it's already "Weight, g". If a future pet has
                    // a different unit, change this title dynamically here.
                    val unit = if (pet.breed.equals("Snail", ignoreCase = true) ||
                        pet.species.contains("Achatina", ignoreCase = true)) "g" else "g"
                    tvWeightTitle.text = "Weight, $unit"

                    orbBg.setPalette(palette)
                }
            }
        }

        // UI state — refresh both charts
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    chartFeedings.setData(
                        state.feedingsPerDay.map { BarChartView.BarData(it.label, it.value) }
                    )
                    chartWeight.setData(
                        state.weightSeries.map { BarChartView.BarData(it.label, it.value) }
                    )
                    // Show a small hint if there is no weight data for the period
                    tvWeightHint.isVisible = state.weightSeries.isEmpty()
                }
            }
        }
    }
}