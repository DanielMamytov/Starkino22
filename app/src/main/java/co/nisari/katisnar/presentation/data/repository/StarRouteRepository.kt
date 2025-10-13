package co.nisari.katisnar.presentation.data.repository

import co.nisari.katisnar.presentation.data.local.StarRouteDao
import co.nisari.katisnar.presentation.ui.admiral.RoutePoint
import co.nisari.katisnar.presentation.ui.admiral.StarRoute
import co.nisari.katisnar.presentation.ui.admiral.StarRouteWithPoints
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarRouteRepository @Inject constructor(
    private val dao: StarRouteDao
) {
    fun getAll(): Flow<List<StarRoute>> = dao.getAll()

    fun getRouteWithPoints(id: Long): Flow<StarRouteWithPoints?> =
        dao.getRouteWithPoints(id)

    suspend fun insert(route: StarRoute, points: List<Pair<Double, Double>>): Long {
        val id = dao.insertRoute(route)
        if (points.isNotEmpty()) {
            val list = points.map { (lat, lng) ->
                RoutePoint(routeId = id, lat = lat, lng = lng)
            }
            dao.insertPoints(list)
        }
        return id
    }

    suspend fun update(route: StarRoute, points: List<Pair<Double, Double>>) {
        dao.updateRoute(route)
        dao.deletePointsByRoute(route.id)
        if (points.isNotEmpty()) {
            val list = points.map { (lat, lng) ->
                RoutePoint(routeId = route.id, lat = lat, lng = lng)
            }
            dao.insertPoints(list)
        }
    }

    suspend fun deleteById(id: Long) = dao.deleteRouteById(id)
}
