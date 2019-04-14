package ca.poly.inf8405.alarmme.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import ca.poly.inf8405.alarmme.db.AlarmMeDb
import ca.poly.inf8405.alarmme.db.CheckPointDao
import ca.poly.inf8405.alarmme.di.qualifier.ApplicationContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
  
  @Singleton
  @Provides
  @ApplicationContext
  fun provideApplicationContext(app: Application): Context = app
  
  @Singleton
  @Provides
  fun provideDb(app: Application): AlarmMeDb = with(app) {
    Room
      .databaseBuilder(this, AlarmMeDb::class.java, DATABASE_NAME)
      // tell Room to destroy and recreate the app's database tables when we upgrade versions
      // since we haven't provided migration paths between schema versions
      .fallbackToDestructiveMigration()
      // Build the database
      .build()
  }
  
  @Singleton
  @Provides
  fun provideCheckPointDao(db: AlarmMeDb): CheckPointDao = db.checkPointDao()
  
  companion object {
    private const val DATABASE_NAME = "alarm-me.db"
  }
}