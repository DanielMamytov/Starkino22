package co.nisari.katisnar.presentation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.nisari.katisnar.presentation.ui.admiral.RoutePoint
import co.nisari.katisnar.presentation.ui.admiral.StarRoute

@Database(
    entities = [StarLocation::class,StarRoute::class,
        RoutePoint::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(StarLocationConverters::class)
abstract class StarDatabase : RoomDatabase() {
    abstract fun starLocationDao(): StarLocationDao
    abstract fun starRouteDao(): StarRouteDao
}
