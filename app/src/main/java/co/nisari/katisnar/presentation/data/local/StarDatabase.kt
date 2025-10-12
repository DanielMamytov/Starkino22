package co.nisari.katisnar.presentation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [StarLocation::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(StarLocationConverters::class)
abstract class StarDatabase : RoomDatabase() {
    abstract fun starLocationDao(): StarLocationDao
}
