package eng.nookipet.lab7.ui.vet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eng.nookipet.lab7.data.entity.Event
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/** A reason for visiting the vet — matches the "Book a vet" mockup. */
data class VetReason(val emoji: String, val title: String, val subtitle: String)

/**
 * A nearby clinic.
 * lat / lon — real coordinates, used to place the marker on the map.
 */
data class Clinic(
    val name: String,
    val address: String,
    val distanceKm: Float,
    val hours: String,
    val rating: Double,
    val reviews: Int,
    val lat: Double,
    val lon: Double,
    val acceptsExotic: Boolean = false
)

/**
 * Backs BookVetFragment and NearbyClinicsFragment.
 *   - reasons list
 *   - clinics list (mock, but with real LA coordinates)
 *   - saveVisit() writes an Event of type VET to Room
 */
class VetViewModel(
    private val selectedPetRepo: SelectedPetRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    val pet: StateFlow<Pet?> = selectedPetRepo.selected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val reasons: List<VetReason> = listOf(
        VetReason("🩺", "Routine check-up", "Preventive exam"),
        VetReason("💉", "Vaccination", "Scheduled vaccine"),
        VetReason("🐛", "Parasites", "Treatment / exam"),
        VetReason("🩹", "Injury", "Shell, body, limbs"),
        VetReason("🍽", "Feeding issues", "Refusal to eat, lethargy"),
        VetReason("❓", "Other", "Add a note")
    )

    // ─────────────────────────────────────────────────────────────
    // Mock clinics in Los Angeles area with real coordinates.
    // Real addresses, real hours, real lat/lon — so map markers
    // land on the right places.
    // TODO: replace with Google Places / Overpass API call in production.
    // ─────────────────────────────────────────────────────────────
    val clinics: List<Clinic> = listOf(
        Clinic(
            name = "VCA Animal Specialty & Emergency Center",
            address = "1535 S Sepulveda Blvd, Los Angeles, CA 90025",
            distanceKm = 1.2f,
            hours = "24/7 emergency",
            rating = 4.7, reviews = 1284,
            lat = 34.0417, lon = -118.4443
        ),
        Clinic(
            name = "ACCESS Specialty Animal Hospital",
            address = "9599 Jefferson Blvd, Culver City, CA 90232",
            distanceKm = 3.6f,
            hours = "24/7",
            rating = 4.6, reviews = 942,
            lat = 34.0248, lon = -118.3911
        ),
        Clinic(
            name = "Exotic Animal Veterinary Center",
            address = "950 S Arroyo Pkwy, Pasadena, CA 91105",
            distanceKm = 12.4f,
            hours = "Mon–Sat, 8:00–18:00",
            rating = 4.9, reviews = 218,
            lat = 34.1325, lon = -118.1496,
            acceptsExotic = true
        ),
        Clinic(
            name = "Beverly Hills Small Animal Hospital",
            address = "9001 W Olympic Blvd, Beverly Hills, CA 90211",
            distanceKm = 6.1f,
            hours = "Mon–Fri, 7:00–19:00",
            rating = 4.8, reviews = 476,
            lat = 34.0635, lon = -118.3868
        ),
        Clinic(
            name = "Santa Monica Pet Medical Center",
            address = "1518 Wilshire Blvd, Santa Monica, CA 90403",
            distanceKm = 9.8f,
            hours = "Mon–Sun, 8:00–20:00",
            rating = 4.7, reviews = 613,
            lat = 34.0354, lon = -118.4855
        ),
        Clinic(
            name = "Long Beach Animal Hospital",
            address = "3816 E Anaheim St, Long Beach, CA 90804",
            distanceKm = 27.3f,
            hours = "24/7",
            rating = 4.5, reviews = 1057,
            lat = 33.7835, lon = -118.1253
        )
    )

    /**
     * Saves the vet visit as an Event of type VET.
     * The event will appear in Home's "Recent events" and Stats screen.
     */
    fun saveVisit(
        reason: String,
        dateLabel: String,
        timeLabel: String,
        clinicName: String,
        onNoPet: () -> Unit = {},
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val current = selectedPetRepo.selected.first()
            if (current == null) {
                onNoPet()
                return@launch
            }
            eventRepo.insert(
                Event(
                    petId = current.id,
                    type = EventType.VET.name,
                    title = reason,
                    amount = "$dateLabel · $timeLabel · $clinicName",
                    repeatMode = "One-time visit"
                )
            )
            onDone()
        }
    }
}