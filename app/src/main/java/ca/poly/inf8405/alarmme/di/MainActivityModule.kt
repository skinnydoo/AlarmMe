package ca.poly.inf8405.alarmme.di

import ca.poly.inf8405.alarmme.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityModule {
  @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
  abstract fun contributeMainActivity(): MainActivity
}