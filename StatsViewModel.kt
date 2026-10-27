package eng.nookipet.lab7.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eng.nookipet.lab7.data.entity.Event
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class StatsPeriod { WEEK, MONTH, YEAR }

data class BarPoint(val label: String, val value: Float)

data class StatsUiState(
    val feedingsPerDay: List<BarPoint> = emptyList(),
    val weightSeries: List<BarPoint> = emptyList()
)

class StatsViewModel(
    private val selectedPetRepo: SelectedPetRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.WEEK)
    val period: StateFlow<StatsPeriod> = _period

    val pet: StateFlow<Pet?> = selectedPetRepo.selected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<StatsUiState> = combine(
        selectedPetRepo.selected,
        _period
    ) { pet, period -> Pair(pet, period) }
        .flatMapLatest { (pet, period) ->
            flow {
                if (pet == null) {
                    emit(StatsUiState())
                    return@flow
                }

                val since = period.windowStartMillis()
                eventRepo.getForPet(pet.id).collect { allEvents ->
                    val recent = allEvents.filter { it.timestamp >= since }
                    val feedings = recent.filter { it.type == EventType.FEEDING.name }
                    // Sorted ascending — later entries override earlier ones
                    // inside the same calendar bucket.
                    val weights = recent.filter { it.weightG > 0 }
                        .sortedBy { it.timestamp }

                    emit(
                        StatsUiState(
                            feedingsPerDay = aggregateFeedings(feedings, period),
                            weightSeries = aggregateWeight(weights, period)
                        )
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    fun setPeriod(p: StatsPeriod) { _period.value = p }

    // ─────────────────────────────────────────────────────────────
    // Feedings — count per calendar day / week / month
    // ─────────────────────────────────────────────────────────────

    private fun aggregateFeedings(events: List<Event>, period: StatsPeriod): List<BarPoint> {
        val todayMidnight = todayMidnightMillis()
        val dayMs = TimeUnit.DAYS.toMillis(1)

        return when (period) {
            StatsPeriod.WEEK -> {
                // 7 buckets: index 0 = 6 days ago, index 6 = today
                val buckets = IntArray(7)
                events.forEach { e ->
                    val daysAgo = ((todayMidnight - midnightOf(e.timestamp)) / dayMs).toInt()
                    if (daysAgo in 0..6) buckets[6 - daysAgo]++
                }
                val labels = (0..6).map { i -> weekdayShortName(6 - i) }
                buckets.mapIndexed { i, c -> BarPoint(labels[i], c.toFloat()) }
            }

            StatsPeriod.MONTH -> {
                // 4 buckets: index 0 = 3 weeks ago, index 3 = current week
                val buckets = IntArray(4)
                val weekMs = TimeUnit.DAYS.toMillis(7)
                events.forEach { e ->
                    val weeksAgo = ((todayMidnight - midnightOf(e.timestamp)) / weekMs).toInt()
                    if (weeksAgo in 0..3) buckets[3 - weeksAgo]++
                }
                buckets.mapIndexed { i, c -> BarPoint("W${i + 1}", c.toFloat()) }
            }

            StatsPeriod.YEAR -> {
                // 12 buckets: calendar months of the current year
                val buckets = IntArray(12)
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val cal = Calendar.getInstance()
                events.forEach { e ->
                    cal.timeInMillis = e.timestamp
                    if (cal.get(Calendar.YEAR) == currentYear) {
                        buckets[cal.get(Calendar.MONTH)]++
                    }
                }
                val months = monthLabels()
                buckets.mapIndexed { i, c -> BarPoint(months[i], c.toFloat()) }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Weight — one bucket per calendar day / week / month.
    //   If multiple measurements fall in the same bucket, the latest
    //   one wins (buckets array is overwritten in ascending order).
    //   Empty buckets stay 0 → no bar drawn, but label is preserved.
    // ─────────────────────────────────────────────────────────────

    private fun aggregateWeight(events: List<Event>, period: StatsPeriod): List<BarPoint> {
        if (events.isEmpty()) return emptyList()

        val todayMidnight = todayMidnightMillis()
        val dayMs = TimeUnit.DAYS.toMillis(1)

        return when (period) {
            StatsPeriod.WEEK -> {
                val buckets = arrayOfNulls<Int>(7)
                events.forEach { e ->
                    val daysAgo = ((todayMidnight - midnightOf(e.timestamp)) / dayMs).toInt()
                    if (daysAgo in 0..6) buckets[6 - daysAgo] = e.weightG
                }
                val labels = (0..6).map { i -> weekdayShortName(6 - i) }
                buckets.mapIndexed { i, v -> BarPoint(labels[i], (v ?: 0).toFloat()) }
            }

            StatsPeriod.MONTH -> {
                val buckets = arrayOfNulls<Int>(4)
                val weekMs = TimeUnit.DAYS.toMillis(7)
                events.forEach { e ->
                    val weeksAgo = ((todayMidnight - midnightOf(e.timestamp)) / weekMs).toInt()
                    if (weeksAgo in 0..3) buckets[3 - weeksAgo] = e.weightG
                }
                buckets.mapIndexed { i, v -> BarPoint("W${i + 1}", (v ?: 0).toFloat()) }
            }

            StatsPeriod.YEAR -> {
                val buckets = arrayOfNulls<Int>(12)
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val cal = Calendar.getInstance()
                events.forEach { e ->
                    cal.timeInMillis = e.timestamp
                    if (cal.get(Calendar.YEAR) == currentYear) {
                        buckets[cal.get(Calendar.MONTH)] = e.weightG
                    }
                }
                val months = monthLabels()
                buckets.mapIndexed { i, v -> BarPoint(months[i], (v ?: 0).toFloat()) }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    /**
     * Start of the current period window, anchored to midnight.
     *
     * WEEK  → 6 days before today (7 full calendar days including today)
     * MONTH → 27 days before today (4 full weeks)
     * YEAR  → Jan 1 of the current year
     */
    private fun StatsPeriod.windowStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (this) {
            StatsPeriod.WEEK -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.timeInMillis
            }
            StatsPeriod.MONTH -> {
                cal.add(Calendar.DAY_OF_YEAR, -27)
                cal.timeInMillis
            }
            StatsPeriod.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }
        }
    }

    /** Midnight of the day the given timestamp belongs to. */
    private fun midnightOf(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun todayMidnightMillis(): Long = midnightOf(System.currentTimeMillis())

    /** "Mon" / "Tue" / ... for N days ago. */
    private fun weekdayShortName(daysAgo: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            Calendar.SUNDAY -> "Sun"
            else -> ""
        }
    }

    private fun monthLabels(): List<String> = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
}