package eng.nookipet.lab7.ui.vet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.radiobutton.MaterialRadioButton
import eng.nookipet.lab7.R

/**
 * Simple reasons list — one selected at a time.
 * Uses a plain RecyclerView with manual radio state.
 */
class VetReasonAdapter(
    private val reasons: List<VetReason>,
    private val onSelect: (Int) -> Unit
) : RecyclerView.Adapter<VetReasonAdapter.ReasonViewHolder>() {

    private var selectedIndex: Int = 0

    fun getSelectedIndex(): Int = selectedIndex
    fun getSelectedReason(): VetReason = reasons[selectedIndex]

    inner class ReasonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.tvReasonEmoji)
        val title: TextView = view.findViewById(R.id.tvReasonTitle)
        val subtitle: TextView = view.findViewById(R.id.tvReasonSubtitle)
        val radio: MaterialRadioButton = view.findViewById(R.id.radioReason)

        fun bind(position: Int) {
            val reason = reasons[position]
            emoji.text = reason.emoji
            title.text = reason.title
            subtitle.text = reason.subtitle
            radio.isChecked = position == selectedIndex

            itemView.setOnClickListener {
                val prev = selectedIndex
                selectedIndex = position
                notifyItemChanged(prev)
                notifyItemChanged(selectedIndex)
                onSelect(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vet_reason, parent, false)
        return ReasonViewHolder(v)
    }

    override fun onBindViewHolder(holder: ReasonViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = reasons.size
}