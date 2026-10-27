package eng.nookipet.lab7.ui.reminders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.entity.Reminder

/**
 * List of reminders for the active pet.
 *   - switch toggles enabled state (writes to Room)
 *   - long-press deletes with confirmation
 */
class ReminderAdapter(
    private val onToggle: (Reminder, Boolean) -> Unit,
    private val onLongClick: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiff) {

    inner class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.tvReminderEmoji)
        val title: TextView = view.findViewById(R.id.tvReminderTitle)
        val schedule: TextView = view.findViewById(R.id.tvReminderSchedule)
        val switch: MaterialSwitch = view.findViewById(R.id.switchEnabled)

        fun bind(reminder: Reminder) {
            val type = runCatching { EventType.valueOf(reminder.type) }
                .getOrDefault(EventType.OTHER)
            emoji.text = type.emoji
            title.text = reminder.title
            schedule.text = reminder.scheduleLabel

            // Detach listener before setting checked state — otherwise
            // recycled items would fire spurious callbacks during bind.
            switch.setOnCheckedChangeListener(null)
            switch.isChecked = reminder.enabled
            switch.setOnCheckedChangeListener { _, checked ->
                onToggle(reminder, checked)
            }

            itemView.setOnLongClickListener {
                onLongClick(reminder)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(v)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object ReminderDiff : DiffUtil.ItemCallback<Reminder>() {
    override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder) =
        oldItem == newItem
}