package co.nisari.katisnar.presentation.ui.admiral

import androidx.room.Embedded
import androidx.room.Relation

data class StarRouteWithPoints(
    @Embedded val route: StarRoute,
    @Relation(
        parentColumn = "id",
        entityColumn = "routeId",
        entity = RoutePoint::class
    )
    val points: List<RoutePoint>
)
