package eng.nookipet.lab7.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eng.nookipet.lab7.data.entity.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE petId = :petId ORDER BY id ASC")
    fun getForPet(petId: Long): Flow<List<Reminder>>

    /** One-shot version — used when we need the list right now (e.g. on pet delete). */
    @Query("SELECT * FROM reminders WHERE petId = :petId ORDER BY id ASC")
    suspend fun getForPetOnce(petId: Long): List<Reminder>

    @Query("SELECT * FROM reminders ORDER BY id ASC")
    fun getAll(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("UPDATE reminders SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}