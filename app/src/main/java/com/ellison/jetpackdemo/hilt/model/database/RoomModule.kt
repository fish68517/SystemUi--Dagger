package com.ellison.jetpackdemo.hilt.model.database

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {
    @Singleton
    @Provides
    fun provideNewMovieDataBase(application: Application): NewMovieDataBase {
        return Room.databaseBuilder(application,
                NewMovieDataBase::class.java,
                "hilt-movie.db")
                .fallbackToDestructiveMigrationFrom(1)
                .allowMainThreadQueries()
                .build()
    }

    @Singleton
    @Provides
    fun provideMovieDao(newMovieDataBase: NewMovieDataBase): MovieDao {
        return newMovieDataBase.movieDao()
    }
}
