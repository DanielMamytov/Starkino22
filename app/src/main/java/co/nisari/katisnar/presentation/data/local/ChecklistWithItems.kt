package co.nisari.katisnar.presentation.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ChecklistWithItems(
    @Embedded val checklist: ChecklistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "checklistId",
        entity = ChecklistItemEntity::class
    )
    val items: List<ChecklistItemEntity>
)
