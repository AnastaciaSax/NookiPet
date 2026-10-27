package eng.nookipet.lab7.ui.tips

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.entity.PetTip

class PetTipAdapter : ListAdapter<PetTip, PetTipAdapter.TipViewHolder>(TipDiff) {

    inner class TipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val species: TextView = view.findViewById(R.id.tvTipSpecies)
        val category: TextView = view.findViewById(R.id.tvTipCategory)
        val title: TextView = view.findViewById(R.id.tvTipTitle)
        val body: TextView = view.findViewById(R.id.tvTipBody)

        fun bind(tip: PetTip) {
            species.text = tip.species.uppercase()
            category.text = tip.category.uppercase()
            title.text = tip.title
            body.text = tip.body
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tip, parent, false)
        return TipViewHolder(v)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object TipDiff : DiffUtil.ItemCallback<PetTip>() {
    override fun areItemsTheSame(oldItem: PetTip, newItem: PetTip) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: PetTip, newItem: PetTip) = oldItem == newItem
}