package co.nisari.katisnar.presentation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY id ASC")
    fun getAll(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun getById(id: Long): Flow<ArticleEntity?>

    @Insert
    suspend fun insert(article: ArticleEntity): Long

    @Query("UPDATE articles SET coverUri = :coverRes WHERE id = :id")
    suspend fun updateCover(id: Long, coverRes: Int)
}
