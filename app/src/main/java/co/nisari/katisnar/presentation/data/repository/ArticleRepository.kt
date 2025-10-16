package co.nisari.katisnar.presentation.data.repository

import co.nisari.katisnar.presentation.data.local.ArticleDao
import co.nisari.katisnar.presentation.data.local.ArticleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ArticleRepository @Inject constructor(
    private val dao: ArticleDao
) {
    fun getAll(): Flow<List<ArticleEntity>> = dao.getAll()
    fun getById(id: Long): Flow<ArticleEntity?> = dao.getById(id)
    suspend fun insert(article: ArticleEntity): Long = dao.insert(article)
    suspend fun updateCover(id: Long, coverRes: Int) = dao.updateCover(id, coverRes)
}
