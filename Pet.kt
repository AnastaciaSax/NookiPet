package eng.nookipet.lab7.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One pet in the owner's profile.
 * Storage unit: grams for weight, centimeters for shell length,
 * percentages for humidity, Celsius for temperature.
 * paletteId references PetPaletteCatalog — used to switch UI colors.
 */
@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val species: String,
    val breed: String = "",
    val gender: String = "Unknown",
    val ageLabel: String = "",

    // Key metrics
    val weightG: Int = 0,
    val shellLengthCm: Float = 0f,
    val humidity: Int = 0,
    val temperature: Int = 0,

    // Health
    val lastParasiteTreatment: Long = 0L,
    val diet: String = "",

    // Appearance
    val paletteId: String = "mint",       // → PetPaletteCatalog.byId()
    val avatarEmoji: String = "🐌",
    val notes: String = ""
)