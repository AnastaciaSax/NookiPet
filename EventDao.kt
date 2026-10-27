package eng.nookipet.lab7.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eng.nookipet.lab7.data.entity.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    /** All events for a pet, newest first. */
    @Query("SELECT * FROM events WHERE petId = :petId ORDER BY timestamp DESC")
    fun getForPet(petId: Long): Flow<List<Event>>

    /** N most recent events across all pets — used on Home "Recent events". */
    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<Event>>

    /** Events count for a given type & time window — used on Stats. */
    @Query("""
        SELECT * FROM events
        WHERE petId = :petId AND type = :type AND timestamp >= :since
        ORDER BY timestamp DESC
    """)
    fun getForPetByTypeSince(petId: Long, type: String, since: Long): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event): Long

    @Delete
    suspend fun delete(event: Event)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteById(eventId: Long)
}