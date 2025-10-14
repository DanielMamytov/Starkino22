package co.nisari.katisnar.presentation.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    // Списки с пунктами
    @Transaction
    @Query("SELECT * FROM checklists ORDER BY createdAt DESC")
    fun getAllWithItems(): Flow<List<ChecklistWithItems>>

    @Transaction
    @Query("SELECT * FROM checklists WHERE id = :id")
    fun getWithItems(id: Long): Flow<ChecklistWithItems?>

    // Чеклист
    @Insert
    suspend fun insertChecklist(list: ChecklistEntity): Long

    @Update
    suspend fun updateChecklist(list: ChecklistEntity)

    @Query("DELETE FROM checklists WHERE id = :id")
    suspend fun deleteChecklist(id: Long)

    // Пункты
    @Insert
    suspend fun insertItems(items: List<ChecklistItemEntity>)

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM checklist_items WHERE checklistId = :checklistId")
    suspend fun deleteItemsByChecklist(checklistId: Long)
}
