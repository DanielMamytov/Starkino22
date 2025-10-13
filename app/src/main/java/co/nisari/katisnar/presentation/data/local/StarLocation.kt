package co.nisari.katisnar.presentation.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import co.nisari.katisnar.presentation.data.model.Weather
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "star_locations")
@TypeConverters(StarLocationConverters::class)
data class StarLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val location: String,
    val date: LocalDate,
    val time: LocalTime,
    val lat: Double,
    val lng: Double,
    val weather: Weather,
    val notes: String
)
