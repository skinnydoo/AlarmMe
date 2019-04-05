package ca.poly.inf8405.alarmme.vo

import androidx.room.*

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
  indices = [
    Index("weather_id")
  ],
  foreignKeys = [ForeignKey(
    entity = Weather::class,
    parentColumns = ["id"],
    childColumns = ["weather_id"],
    onUpdate = ForeignKey.CASCADE,
    deferred = true
  )]
)
data class CheckPoint(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "cp_id")
  val Id: Long = 0,
  @ColumnInfo(name = "checkpoint_name")
  val name: String,
  val latitude: Double,
  val longitude: Double,
  val reached: Boolean = false,
  val favorited: Boolean = false,
  @ColumnInfo(name = "weather_id")
  val weatherId: String
)