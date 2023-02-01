package com.example.callblocker.di

import android.content.Context
import androidx.room.Room
import com.example.callblocker.data.ContactDatabase
import com.example.callblocker.util.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideContactDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            ContactDatabase::class.java,
            DATABASE_NAME
        ).build()

    @Singleton
    @Provides
    fun provideContactDao(db: ContactDatabase) = db.getContactDao()
}