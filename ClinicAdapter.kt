package eng.nookipet.lab7.ui.vet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eng.nookipet.lab7.R
import java.util.Locale

/**
 * Clinic list. Tap → notifies the fragment so the map can center on it.
 */
class ClinicAdapter(
    private val clinics: List<Clinic>,
    private val onClinicClick: (Clinic) -> Unit
) : RecyclerView.Adapter<ClinicAdapter.ClinicViewHolder>() {

    inner class ClinicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.tvClinicEmoji)
        val name: TextView = view.findViewById(R.id.tvClinicName)
        val address: TextView = view.findViewById(R.id.tvClinicAddress)
        val rating: TextView = view.findViewById(R.id.tvClinicRating)

        fun bind(clinic: Clinic) {
            emoji.text = if (clinic.acceptsExotic) "🦎" else "🏥"
            name.text = clinic.name
            address.text = String.format(
                Locale.getDefault(),
                "%s · %.1f km · %s",
                clinic.address, clinic.distanceKm, clinic.hours
            )
            rating.text = String.format(
                Locale.getDefault(),
                "★ %.1f · %d reviews",
                clinic.rating, clinic.reviews
            )
            itemView.setOnClickListener { onClinicClick(clinic) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClinicViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clinic, parent, false)
        return ClinicViewHolder(v)
    }

    override fun onBindViewHolder(holder: ClinicViewHolder, position: Int) {
        holder.bind(clinics[position])
    }

    override fun getItemCount(): Int = clinics.size
}