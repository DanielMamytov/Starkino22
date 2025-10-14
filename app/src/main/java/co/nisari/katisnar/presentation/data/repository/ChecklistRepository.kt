package co.nisari.katisnar.presentation.data.repository

import co.nisari.katisnar.presentation.data.local.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChecklistRepository @Inject constructor(
    private val dao: ChecklistDao
) {
    fun getAll(): Flow<List<ChecklistWithItems>> = dao.getAllWithItems()
    fun getById(id: Long): Flow<ChecklistWithItems?> = dao.getWithItems(id)

    suspend fun insertChecklist(title: String, items: List<ChecklistItemEntity>, createdAt: Long = System.currentTimeMillis()) : Long {
        val id = dao.insertChecklist(ChecklistEntity(title = title, createdAt = createdAt))
        if (items.isNotEmpty()) {
            dao.insertItems(items.map { it.copy(id = 0, checklistId = id) })
        }
        return id
    }

    suspend fun updateChecklist(id: Long, title: String, createdAt: Long, items: List<ChecklistItemEntity>) {
        dao.updateChecklist(ChecklistEntity(id = id, title = title, createdAt = createdAt))
        dao.deleteItemsByChecklist(id)               // простой путь: подчистить
        if (items.isNotEmpty()) {
            dao.insertItems(items.map { it.copy(id = 0, checklistId = id) })
        }
    }

    suspend fun deleteChecklist(id: Long) = dao.deleteChecklist(id)

    suspend fun updateItem(item: ChecklistItemEntity) = dao.updateItem(item)
    suspend fun deleteItem(id: Long) = dao.deleteItem(id)
}
