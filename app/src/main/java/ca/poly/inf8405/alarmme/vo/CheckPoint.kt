package ca.poly.inf8405.alarmme.vo

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

/**
 * A basic class representing an entity that is a row in a column database table.
 *
 * @ Entity - You must annotate the class as an entity and supply a table name if not class name.
 * @ PrimaryKey - You must identify the primary key.
 * @ ColumnInfo - You must supply the column name if it is different from the variable name.
 *
 */
@Entity(
  tableName = "checkpoint_table",
  primaryKeys = ["name"]
)
data class CheckPoint(
  val name: String,
  val latitude: Double,
  val longitude: Double,
  val radius: Int,
  val active: Boolean = false,
  val favorited: Boolean = false,
  @Embedded(prefix = "weather_")
  var weather: Weather? = null
) {
  
  data class Weather (
    @SerializedName(value ="id")
    val id: Long,
    
    @SerializedName(value = "name")
    @ColumnInfo(name = "city_name")
    val name: String,
    
    @SerializedName(value = "coord")
    @Embedded(prefix = "coord_")
    val latlng: Coordinates,
    
    @SerializedName(value = "weather")
    @ColumnInfo( name = "weather_description")
    val description: List<WeatherDescription>,
    
    @SerializedName(value = "main")
    @Embedded(prefix = "weather_")
    val weatherInfo: WeatherInfo,
    
    @SerializedName(value = "sys")
    @Embedded(prefix = "city_")
    val city: City,
    
    @SerializedName(value = "dt")
    @ColumnInfo(name = "created_at")
    val dateTime: DateTime
  ) {
    
    data class Coordinates(
      @SerializedName(value = "lat")
      val latitude: Double,
      @SerializedName(value = "lon")
      val longitude: Double
    )
    
    data class WeatherDescription(
      @SerializedName(value = "description")
      val description: String,
      @SerializedName(value = "icon")
      val iconUrl: String
    )
    
    data class WeatherInfo(
      @SerializedName(value = "temp")
      val temperature: Double,
      @SerializedName(value = "pressure")
      val pressure: Double,
      @SerializedName(value = "humidity")
      val humidity: Double,
      @SerializedName(value = "temp_min")
      val minTemp: Double,
      @SerializedName(value = "temp_max")
      val maxTemp: Double
    )
    
    data class City(
      @SerializedName(value = "sunrise")
      val sunrise: DateTime,
      @SerializedName(value = "sunset")
      val sunset: DateTime
    )
  }
}