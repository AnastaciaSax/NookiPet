package eng.nookipet.lab7

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    /** All attachments — legacy use. */
    @Query("SELECT * FROM attachments ORDER BY id DESC")
    fun getAll(): Flow<List<Attachment>>

    /** Documents of a specific pet. */
    @Query("SELECT * FROM attachments WHERE petId = :petId ORDER BY id DESC")
    fun getForPet(petId: Long): Flow<List<Attachment>>

    /** One-shot — used to clean up before pet deletion. */
    @Query("SELECT * FROM attachments WHERE petId = :petId")
    suspend fun getForPetOnce(petId: Long): List<Attachment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: Attachment): Long

    @Delete
    suspend fun delete(attachment: Attachment)

    @Query("DELETE FROM attachments WHERE petId = :petId")
    suspend fun deleteAllForPet(petId: Long)
}