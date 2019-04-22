package ca.poly.inf8405.alarmme.di

import android.app.Application
import ca.poly.inf8405.alarmme.AlarmMeApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

/**
 * This class represent the root of our dagger graph.
 * It is providing 3 modules to our app
 *
 * Android apps have one application class.
 * That is why we have one application component.
 * It is responsible for providing application
 * scope instances (eg. OkHttp, Database, SharedPrefs.).
 */
@Singleton
@Component(modules = [
  AndroidInjectionModule::class, // Provides our activities and fragments with given module
  AppModule::class, // Provide app context, persistence db, shared pref etc.
  ApiModule::class, // Provides Api key, retrofit, okhttp etc...
  LocationModule::class, // Provides FusedLocation Api, Location Settings API, etc
  MainActivityModule::class, // Provides our MainActivity
  ServiceBuilderModule::class // Provides our Services
])
interface AppComponent {
  fun inject(alarmMeApp: AlarmMeApp)
  fun inject(alarmMeGlideModule: AlarmMeGlideModule)
  
  @Component.Builder
  interface Builder {
    @BindsInstance
    fun application(application: Application): Builder
    
    fun build(): AppComponent
  }
}