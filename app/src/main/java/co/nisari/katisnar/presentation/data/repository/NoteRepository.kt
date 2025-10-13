package co.nisari.katisnar.presentation.data.repository

import co.nisari.katisnar.presentation.data.local.NoteDao
import co.nisari.katisnar.presentation.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val dao: NoteDao
) {
    fun getAll(): Flow<List<NoteEntity>> = dao.getAll()
    fun getById(id: Long): Flow<NoteEntity?> = dao.getById(id)
    suspend fun insert(note: NoteEntity): Long = dao.insert(note)
    suspend fun update(note: NoteEntity) = dao.update(note)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
