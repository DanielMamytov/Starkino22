package co.nisari.katisnar.presentation.data.repository

import co.nisari.katisnar.presentation.data.local.StarLocation
import co.nisari.katisnar   .presentation.data.local.StarLocationDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarLocationRepository @Inject constructor(
    private val dao: StarLocationDao
) {
    fun getAll(): Flow<List<StarLocation>> = dao.getAll()

    fun getById(id: Long): Flow<StarLocation?> = dao.getById(id)

    suspend fun insert(location: StarLocation): Long = dao.insert(location)

    suspend fun update(location: StarLocation) = dao.update(location)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}
