package eng.nookipet.lab7.data.entity

/**
 * A single care tip loaded via Retrofit from the backend.
 * Same shape as the JSON in assets/pet_tips.json.
 */
data class PetTip(
    val id: Int,
    val species: String,
    val category: String,
    val title: String,
    val body: String
)