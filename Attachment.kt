package eng.nookipet.lab7

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A file attached to a specific pet.
 * fileName = name of the copy inside app's private filesDir/documents/
 * (so it survives moves/deletes of the original).
 */
@Entity(
    tableName = "attachments",
    indices = [Index("petId")]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sizeKb: Int,
    val petId: Long = 0L,
    val type: String = "Other",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val fileName: String = ""
)