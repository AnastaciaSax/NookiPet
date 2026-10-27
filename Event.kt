package eng.nookipet.lab7.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single logged event for a pet: feeding, misting, cleaning, meds, etc.
 * Foreign key to Pet ensures no orphan events exist.
 * Indices on petId speed up "get all events for pet X" query.
 */
@Entity(
    tableName = "events",
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
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val petId: Long,                   // owner pet id
    val type: String,                  // EventType.name — stored as string
    val title: String,                 // "Feeding" / "Misting" etc. (localized label)
    val amount: String = "",           // "20 g" / "Humidity 80%"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val repeatMode: String = "None",    // "None" / "Daily" / "Twice a day" / "Every other day"

    /**
     * Optional weight measurement in grams at the moment of this event.
     * 0 = not a weight entry, just a regular event.
     * When > 0, Stats uses this to build the real weight trend chart.
     */
    val weightG: Int = 0
)