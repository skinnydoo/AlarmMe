package ca.poly.inf8405.alarmme.di

import ca.poly.inf8405.alarmme.ui.MapFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {
 @ContributesAndroidInjector
 abstract fun contributeMapFragment(): MapFragment
}