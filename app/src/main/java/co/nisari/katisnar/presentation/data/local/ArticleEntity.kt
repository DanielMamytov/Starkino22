package co.nisari.katisnar.presentation.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val coverUri: Int?,   // путь к локальному ресурсу/файлу, можно null
    val content: String
)
