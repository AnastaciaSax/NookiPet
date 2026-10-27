package eng.nookipet.lab7.data.entity

/**
 * Types of events a pet owner can log.
 * Stored in DB as a string (see RoomTypeConverters or direct name usage).
 * Each type has an emoji + label for the UI.
 */
enum class EventType(val emoji: String, val label: String) {
    FEEDING("🍽", "Feeding"),
    MISTING("💧", "Misting"),
    CLEANING("🧹", "Cleaning"),
    MEDS("💊", "Meds"),
    VET("🩺", "Vet"),
    OTHER("🐾", "Other")
}