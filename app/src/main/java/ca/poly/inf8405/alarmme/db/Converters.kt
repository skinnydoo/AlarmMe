package ca.poly.inf8405.alarmme.db

import androidx.room.TypeConverter
import org.joda.time.DateTime

object Converters {
  @TypeConverter
  @JvmStatic
  fun toDateTime(value: Long?) : DateTime? = value?.let { DateTime(it) }
  
  @TypeConverter
  @JvmStatic
  fun fromDateTime(dateTime: DateTime?): Long? = dateTime?.millis
  
 /* @TypeConverter
  @JvmStatic
  fun toList(json: String? ): List<WeatherDescription>? = json?.let {
    val type = object : TypeToken<List<WeatherDescription>>() {}.type
    Gson().fromJson(json, type)
  }
  
  @TypeConverter
  @JvmStatic
  fun fromList(list: List<WeatherDescription>? ): String?  {
    if (list == null || list.isEmpty() ) return null
  
    val type = object : TypeToken<List<WeatherDescription>>() {}.type
    return Gson().toJson(list, type)
  }*/
  
  
  // add more converters here...
}