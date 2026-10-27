package eng.nookipet.lab7.ui.vet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import eng.nookipet.lab7.ui.theme.applyPetPalette

/**
 * Nearby clinics screen.
 * Top — real OSMDroid map centered on Los Angeles with one marker per clinic.
 * Bottom — list of clinics; tapping a list item centers the map on its marker.
 *
 * Tile source: OpenStreetMap Mapnik (default), no API key required.
 */
class NearbyClinicsFragment : Fragment() {

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

    private lateinit var mapView: MapView
    private val markers = mutableMapOf<String, Marker>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nearby_clinics, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyPetPalette(view)

        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        val rvClinics: RecyclerView = view.findViewById(R.id.rvClinics)
        mapView = view.findViewById(R.id.mapView)

        // Basic map setup: multi-touch zoom, initial position over LA
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(10.5)
        mapView.controller.setCenter(GeoPoint(34.0522, -118.2437))

        // Add a marker per clinic
        val markerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_marker)
        viewModel.clinics.forEach { clinic ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(clinic.lat, clinic.lon)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = clinic.name
                snippet = "${clinic.address} · ${clinic.distanceKm} km"
                icon = markerIcon
            }
            mapView.overlays.add(marker)
            markers[clinic.name] = marker
        }
        mapView.invalidate()

        // Clinic list
        val adapter = ClinicAdapter(viewModel.clinics) { clinic ->
            // Tap on a clinic → center the map on its marker
            mapView.controller.animateTo(GeoPoint(clinic.lat, clinic.lon))
            mapView.controller.setZoom(14.0)
            markers[clinic.name]?.showInfoWindow()
        }
        rvClinics.layoutManager = LinearLayoutManager(requireContext())
        rvClinics.adapter = adapter

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    // OSMDroid lifecycle — pause/resume tile loading when fragment goes to background.
    // Prevents battery drain and useless network calls.
    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (::mapView.isInitialized) mapView.onPause()
    }
}