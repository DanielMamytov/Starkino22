package co.nisari.katisnar.presentation.data.local

import androidx.room.*
import co.nisari.katisnar.presentation.ui.admiral.RoutePoint
import co.nisari.katisnar.presentation.ui.admiral.StarRoute
import co.nisari.katisnar.presentation.ui.admiral.StarRouteWithPoints

import kotlinx.coroutines.flow.Flow

@Dao
interface StarRouteDao {

    // список (без точек)
    @Query("SELECT * FROM star_routes ORDER BY date DESC, time DESC")
    fun getAll(): Flow<List<StarRoute>>

    // детали/редактирование (с точками)
    @Transaction
    @Query("SELECT * FROM star_routes WHERE id = :id LIMIT 1")
    fun getRouteWithPoints(id: Long): Flow<StarRouteWithPoints?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: StarRoute): Long

    @Update
    suspend fun updateRoute(route: StarRoute)

    @Query("DELETE FROM star_routes WHERE id = :id")
    suspend fun deleteRouteById(id: Long)

    @Insert
    suspend fun insertPoints(points: List<RoutePoint>)

    @Query("DELETE FROM route_points WHERE routeId = :routeId")
    suspend fun deletePointsByRoute(routeId: Long)
}
