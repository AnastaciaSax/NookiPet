package eng.nookipet.lab7.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.entity.Event
import eng.nookipet.lab7.data.entity.EventType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Simple event list for Home's "Recent events" section.
 * Emoji, title, details, friendly timestamp, long-press to delete.
 */
class EventAdapter(
    private val onEventLongClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiff) {

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.tvEventEmoji)
        val title: TextView = view.findViewById(R.id.tvEventTitle)
        val details: TextView = view.findViewById(R.id.tvEventDetails)
        val time: TextView = view.findViewById(R.id.tvEventTime)

        fun bind(event: Event) {
            val type = runCatching { EventType.valueOf(event.type) }
                .getOrDefault(EventType.OTHER)
            emoji.text = type.emoji
            title.text = event.title
            details.text = event.amount.ifBlank { type.label }
            time.text = event.timestamp.asFriendlyTime()

            itemView.setOnLongClickListener {
                onEventLongClick(event)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(v)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object EventDiff : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Event, newItem: Event) = oldItem == newItem
}

/** "Today, 10:15" / "Yesterday, 19:00" / "05.10, 14:30" */
private fun Long.asFriendlyTime(): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = this@asFriendlyTime }

    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(then.time)

    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

    return when {
        sameDay -> "Today, $timeStr"
        isYesterday -> "Yesterday, $timeStr"
        else -> {
            val date = SimpleDateFormat("dd.MM", Locale.getDefault()).format(then.time)
            "$date, $timeStr"
        }
    }
}