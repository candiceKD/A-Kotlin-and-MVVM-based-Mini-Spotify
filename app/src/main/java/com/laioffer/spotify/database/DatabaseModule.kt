package com.laioffer.spotify.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    //这是一个builder pattern, Favorite这个功能是由model这一层完成的, 那么要实现这一功能要把Dao inject进去
    //而Dao是第三方 room database library实现的, 不是我们new出来的,所以我们需要provide给Hilt
    @Provides
    @Singleton
    //ApplicationContext是系统本身自带的
    //先build AppDatabase
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "spotify_db"
        ).build()
    }

    @Provides
    @Singleton
    //然后由AppDatabase去provide Dao
    fun provideDatabaseDao(database: AppDatabase): DatabaseDao {
        return database.databaseDao()
    }
}