package eng.nookipet.lab7.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Reminder item for a pet (feeding at 10:00, misting twice a day, etc.).
 * enabled toggles the switch in the Reminders screen.
 * The Worker reads these to schedule notifications.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("petId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val petId: Long,
    val type: String,             // EventType.name
    val title: String,            // "Feeding"
    val scheduleLabel: String,    // "Every day at 10:00" / "Twice a day" / "Every other day, 12:00"
    val enabled: Boolean = true,
    val hourOfDay: Int = 10,      // 0..23 — used to schedule WorkManager notification
    val minute: Int = 0
)