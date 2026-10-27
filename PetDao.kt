package eng.nookipet.lab7.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eng.nookipet.lab7.data.entity.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    /** All pets — used on Home / Pets tab. */
    @Query("SELECT * FROM pets ORDER BY id ASC")
    fun getAll(): Flow<List<Pet>>

    /** First pet — the "current" one for Home screen. */
    @Query("SELECT * FROM pets ORDER BY id ASC LIMIT 1")
    fun getCurrent(): Flow<Pet?>

    /** Single pet — for Pet profile screen. */
    @Query("SELECT * FROM pets WHERE id = :petId")
    fun getByIdFlow(petId: Long): Flow<Pet?>

    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getById(petId: Long): Pet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: Pet): Long

    @Update
    suspend fun update(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)
}