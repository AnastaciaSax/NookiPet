package eng.nookipet.lab7.data.repository

import eng.nookipet.lab7.data.dao.ReminderDao
import eng.nookipet.lab7.data.entity.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {

    fun getForPet(petId: Long): Flow<List<Reminder>> = dao.getForPet(petId)

    suspend fun getForPetOnce(petId: Long): List<Reminder> = dao.getForPetOnce(petId)

    fun getAll(): Flow<List<Reminder>> = dao.getAll()

    suspend fun insert(reminder: Reminder): Long = dao.insert(reminder)

    suspend fun update(reminder: Reminder) = dao.update(reminder)

    suspend fun delete(reminder: Reminder) = dao.delete(reminder)

    suspend fun setEnabled(id: Long, enabled: Boolean) = dao.setEnabled(id, enabled)
}