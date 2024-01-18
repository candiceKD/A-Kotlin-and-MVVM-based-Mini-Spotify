package com.laioffer.spotify.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.laioffer.spotify.datamodel.Album

//在这里定义了database所有的table, version代表了database的migration
@Database(entities = [Album::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    //给database使用DAO的接口
    abstract fun databaseDao(): DatabaseDao
}
//这个关系跟Retrofit的使用很像