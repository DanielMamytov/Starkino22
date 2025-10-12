package co.nisari.katisnar.presentation.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StarLocationDao {

    @Query("SELECT * FROM star_locations ORDER BY date DESC, time DESC")
    fun getAll(): Flow<List<StarLocation>>

    @Query("SELECT * FROM star_locations WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<StarLocation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: StarLocation): Long

    @Update
    suspend fun update(location: StarLocation)

    @Query("DELETE FROM star_locations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM star_locations")
    suspend fun deleteAll()
}
