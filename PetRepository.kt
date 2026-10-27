package eng.nookipet.lab7.data.repository

import eng.nookipet.lab7.data.dao.PetDao
import eng.nookipet.lab7.data.entity.Pet
import kotlinx.coroutines.flow.Flow

/**
 * Hides Room details from ViewModel.
 * If we later add caching / network sync, only this class changes.
 */
class PetRepository(private val dao: PetDao) {

    fun getAll(): Flow<List<Pet>> = dao.getAll()

    fun getCurrent(): Flow<Pet?> = dao.getCurrent()

    fun getById(petId: Long): Flow<Pet?> = dao.getByIdFlow(petId)

    suspend fun getByIdOnce(petId: Long): Pet? = dao.getById(petId)

    suspend fun insert(pet: Pet): Long = dao.insert(pet)

    suspend fun update(pet: Pet) = dao.update(pet)

    suspend fun delete(pet: Pet) = dao.delete(pet)
}