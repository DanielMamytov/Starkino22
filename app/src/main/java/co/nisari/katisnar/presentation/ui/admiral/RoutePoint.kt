package co.nisari.katisnar.presentation.ui.admiral

import androidx.room.*

@Entity(
    tableName = "route_points",
    foreignKeys = [ForeignKey(
        entity = StarRoute::class,
        parentColumns = ["id"],
        childColumns = ["routeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("routeId")]
)
data class RoutePoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: Long,
    val lat: Double,
    val lng: Double
)
