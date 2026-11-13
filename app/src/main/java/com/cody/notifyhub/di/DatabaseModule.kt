package com.cody.notifyhub.di

import android.content.Context
import androidx.room3.Room
import com.cody.notifyhub.data.local.AppDatabase
import com.cody.notifyhub.data.local.NotificationDao
import dagger.hilt.InstallIn
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)     // these objects live as long as the app
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "notifyhub.db").build()

    @Provides
    fun provideDao(db: AppDatabase): NotificationDao = db.notificationDao()
}