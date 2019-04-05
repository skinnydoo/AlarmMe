package ca.poly.inf8405.alarmme.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ca.poly.inf8405.alarmme.vo.CheckPoint
import ca.poly.inf8405.alarmme.vo.Weather

/**
 * This Room represent a database layer on top of SQLite Database
 * It serves as an access point to the underlying SQLite database.
 * The Room database uses the DAO to issue queries to the SQLite database.
 */
@Database(
  entities = [
    CheckPoint::class,
    Weather::class
  ], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AlarmMeDb: RoomDatabase() {
  abstract fun checkPointDao(): CheckPointDao
  abstract fun weatherDao(): WeatherDao
}