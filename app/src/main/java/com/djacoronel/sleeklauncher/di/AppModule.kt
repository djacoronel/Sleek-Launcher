package com.djacoronel.sleeklauncher.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.djacoronel.sleeklauncher.data.room.AppDatabase
import com.djacoronel.sleeklauncher.data.room.IconPrefsDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
* Created by djacoronel on 2/7/18.
*/

@Module
class AppModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideIconPrefsDao(database: AppDatabase): IconPrefsDao = database.iconPrefsDao()
}