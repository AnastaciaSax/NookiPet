package eng.nookipet.lab7.data.repository

import eng.nookipet.lab7.Attachment
import eng.nookipet.lab7.AttachmentDao
import kotlinx.coroutines.flow.Flow

class AttachmentRepository(private val dao: AttachmentDao) {

    fun getAll(): Flow<List<Attachment>> = dao.getAll()

    fun getForPet(petId: Long): Flow<List<Attachment>> = dao.getForPet(petId)

    suspend fun getForPetOnce(petId: Long): List<Attachment> = dao.getForPetOnce(petId)

    suspend fun insert(attachment: Attachment): Long = dao.insert(attachment)

    suspend fun delete(attachment: Attachment) = dao.delete(attachment)

    suspend fun deleteAllForPet(petId: Long) = dao.deleteAllForPet(petId)
}