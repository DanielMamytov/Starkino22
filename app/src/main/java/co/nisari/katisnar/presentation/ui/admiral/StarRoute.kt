package co.nisari.katisnar.presentation.ui.admiral

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "star_routes")
data class StarRoute(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: LocalDate,
    val time: LocalTime,
    val description: String
)
