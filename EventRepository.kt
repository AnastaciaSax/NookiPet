package eng.nookipet.lab7.data.repository

import eng.nookipet.lab7.data.dao.EventDao
import eng.nookipet.lab7.data.entity.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(private val dao: EventDao) {

    fun getForPet(petId: Long): Flow<List<Event>> = dao.getForPet(petId)

    fun getRecent(limit: Int = 5): Flow<List<Event>> = dao.getRecent(limit)

    fun getForPetByTypeSince(petId: Long, type: String, since: Long): Flow<List<Event>> =
        dao.getForPetByTypeSince(petId, type, since)

    suspend fun insert(event: Event): Long = dao.insert(event)

    suspend fun delete(event: Event) = dao.delete(event)

    suspend fun deleteById(eventId: Long) = dao.deleteById(eventId)
}