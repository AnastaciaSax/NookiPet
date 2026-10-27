package eng.nookipet.lab7.ui.pets

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.ui.theme.PetPaletteCatalog

/**
 * List of pets. Highlights the currently active one with a badge.
 * Uses ListAdapter + DiffUtil — no manual notify needed.
 */
class PetAdapter(
    private val selectedId: () -> Long,
    private val onPetClick: (Pet) -> Unit,
    private val onPetLongClick: (Pet) -> Unit
) : ListAdapter<Pet, PetAdapter.PetViewHolder>(PetDiffCallback) {

    inner class PetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: TextView = view.findViewById(R.id.tvAvatar)
        val name: TextView = view.findViewById(R.id.tvName)
        val subtitle: TextView = view.findViewById(R.id.tvSubtitle)
        val activeBadge: TextView = view.findViewById(R.id.tvActiveBadge)

        fun bind(pet: Pet) {
            name.text = pet.name

            // "Snail · 1y 2m" — fallbacks if breed/age are blank
            val breedLabel = pet.breed.ifBlank { pet.species }
            val ageLabel = pet.ageLabel.ifBlank { "unknown age" }
            subtitle.text = "$breedLabel · $ageLabel"

            avatar.text = pet.avatarEmoji.ifBlank { "🐾" }

            // Runtime gradient for the avatar, colored by the pet's palette
            val palette = PetPaletteCatalog.byId(pet.paletteId)
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(palette.primaryLight, palette.secondary)
            ).apply { shape = GradientDrawable.OVAL }
            avatar.background = gradient

            // Show "Active" pill only on the currently selected pet
            activeBadge.visibility =
                if (pet.id == selectedId()) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onPetClick(pet) }
            itemView.setOnLongClickListener {
                onPetLongClick(pet)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object PetDiffCallback : DiffUtil.ItemCallback<Pet>() {
    override fun areItemsTheSame(oldItem: Pet, newItem: Pet) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Pet, newItem: Pet) = oldItem == newItem
}