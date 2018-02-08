package com.djacoronel.sleeklauncher.di

import com.djacoronel.sleeklauncher.home.MainActivity
import com.djacoronel.sleeklauncher.iconutils.IconsActivity
import com.djacoronel.sleeklauncher.settings.BackgroundSettingsActivity
import com.djacoronel.sleeklauncher.settings.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
* Created by djacoronel on 2/7/18.
*/

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun bindSettingsActivity(): SettingsActivity

    @ContributesAndroidInjector
    abstract fun bindBackgroundSettingsActivity(): BackgroundSettingsActivity

    @ContributesAndroidInjector
    abstract fun bindIconsActivity(): IconsActivity

    @ContributesAndroidInjector
    abstract fun bindPrefsFragment(): SettingsActivity.PrefsFragment
}