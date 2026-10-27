package eng.nookipet.lab7

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import eng.nookipet.lab7.data.dao.EventDao
import eng.nookipet.lab7.data.dao.PetDao
import eng.nookipet.lab7.data.dao.ReminderDao
import eng.nookipet.lab7.data.entity.Event
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.entity.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room database.
 * v6 — Attachment now stores type, timestamp, notes.
 */
@Database(
    entities = [
        Attachment::class,
        Pet::class,
        Event::class,
        Reminder::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attachmentDao(): AttachmentDao
    abstract fun petDao(): PetDao
    abstract fun eventDao(): EventDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nookipet_database"
                )
                    // TODO: при релизе — .addMigrations(...) вместо destructive
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(SeedCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private object SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch { seed(database) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Top-level suspend function — see earlier explanation for why
// it lives outside AppDatabase class.
// ─────────────────────────────────────────────────────────────
private suspend fun seed(db: AppDatabase) {

    // ── Pet 1: Finley the snail ────────────────────────────────
    val finleyId = db.petDao().insert(
        Pet(
            name = "Finley",
            species = "Achatina fulica",
            breed = "Snail",
            gender = "Male",
            ageLabel = "1y 2m",
            weightG = 42,
            shellLengthCm = 7.5f,
            humidity = 80,
            temperature = 24,
            lastParasiteTreatment = 0L,
            diet = "Cucumber, calcium, protein",
            paletteId = "mint",
            avatarEmoji = "🐌"
        )
    )

    // ── Pet 2: Luna the cat ────────────────────────────────────
    val lunaId = db.petDao().insert(
        Pet(
            name = "Luna",
            species = "Felis catus",
            breed = "Cat",
            gender = "Female",
            ageLabel = "3y",
            weightG = 4200,
            shellLengthCm = 0f,
            humidity = 0,
            temperature = 22,
            lastParasiteTreatment = System.currentTimeMillis() - 90L * 24 * 3600 * 1000,
            diet = "Wet food, dry kibble, water fountain",
            paletteId = "peach",
            avatarEmoji = "🐱"
        )
    )

    val now = System.currentTimeMillis()
    val day = 24L * 3600 * 1000

    // ── Finley: base events ────────────────────────────────────
    db.eventDao().insert(Event(petId = finleyId, type = EventType.FEEDING.name,
        title = "Feeding", amount = "Cucumber, 20 g", repeatMode = "Daily",
        timestamp = now - 2 * 3600 * 1000))
    db.eventDao().insert(Event(petId = finleyId, type = EventType.MISTING.name,
        title = "Misting", amount = "Humidity 80%", repeatMode = "Twice a day",
        timestamp = now - 8 * 3600 * 1000))
    db.eventDao().insert(Event(petId = finleyId, type = EventType.CLEANING.name,
        title = "Terrarium cleaning", amount = "", repeatMode = "Weekly",
        timestamp = now - day))

    // Daily weight measurements for the last 7 days — real trend on Week chart
    val finleyWeights = listOf(38, 39, 39, 40, 41, 41, 42)
    finleyWeights.forEachIndexed { i, w ->
        val daysAgo = 6 - i   // oldest first
        db.eventDao().insert(
            Event(
                petId = finleyId,
                type = EventType.OTHER.name,
                title = "Weight",
                amount = "$w g",
                timestamp = now - daysAgo * day,
                weightG = w
            )
        )
    }

    // Feeding events across the last 7 days — the feedings chart is populated too
    for (d in 0..6) {
        // Two feedings most days, one on some
        val count = if (d % 3 == 0) 1 else 2
        repeat(count) {
            db.eventDao().insert(
                Event(
                    petId = finleyId,
                    type = EventType.FEEDING.name,
                    title = "Feeding",
                    amount = "Cucumber, 20 g",
                    timestamp = now - d * day - it * 6 * 3600 * 1000
                )
            )
        }
    }

    // ── Luna: base events ─────────────────────────────────────
    db.eventDao().insert(Event(petId = lunaId, type = EventType.FEEDING.name,
        title = "Feeding", amount = "Wet food, 85 g", repeatMode = "Twice a day",
        timestamp = now - 3 * 3600 * 1000))
    db.eventDao().insert(Event(petId = lunaId, type = EventType.VET.name,
        title = "Vaccination", amount = "FVRCP booster", repeatMode = "Yearly",
        timestamp = now - 5 * day))

    // Daily weight for Luna — real trend
    val lunaWeights = listOf(4080, 4090, 4110, 4130, 4150, 4180, 4200)
    lunaWeights.forEachIndexed { i, w ->
        val daysAgo = 6 - i
        db.eventDao().insert(
            Event(
                petId = lunaId,
                type = EventType.OTHER.name,
                title = "Weight",
                amount = "$w g",
                timestamp = now - daysAgo * day,
                weightG = w
            )
        )
    }

    // ── Reminders ──────────────────────────────────────────────
    db.reminderDao().insert(Reminder(petId = finleyId,
        type = EventType.FEEDING.name, title = "Feeding",
        scheduleLabel = "Every day at 10:00", hourOfDay = 10))
    db.reminderDao().insert(Reminder(petId = finleyId,
        type = EventType.MISTING.name, title = "Misting",
        scheduleLabel = "Twice a day", hourOfDay = 8))
    db.reminderDao().insert(Reminder(petId = finleyId,
        type = EventType.MEDS.name, title = "Calcium",
        scheduleLabel = "Every other day, 12:00", hourOfDay = 12))

    db.reminderDao().insert(Reminder(petId = lunaId,
        type = EventType.FEEDING.name, title = "Feeding",
        scheduleLabel = "Twice a day · 8:00, 20:00", hourOfDay = 8))
    db.reminderDao().insert(Reminder(petId = lunaId,
        type = EventType.MEDS.name, title = "Flea prevention",
        scheduleLabel = "Every 30 days", hourOfDay = 9))
}