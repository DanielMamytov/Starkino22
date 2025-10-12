package co.nisari.katisnar.presentation.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import co.nisari.katisnar.presentation.data.model.Weather
import java.time.LocalDate
import java.time.LocalTime

class StarLocationConverters {

    @TypeConverter
    fun fromDate(date: LocalDate?): String? = date?.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toDate(dateString: String?): LocalDate? =
        dateString?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromTime(time: LocalTime?): String? = time?.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toTime(timeString: String?): LocalTime? =
        timeString?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromWeather(weather: Weather?): String? = weather?.name

    @TypeConverter
    fun toWeather(name: String?): Weather? =
        name?.let { Weather.valueOf(it) }
}
