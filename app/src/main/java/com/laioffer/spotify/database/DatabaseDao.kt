package com.laioffer.spotify.database

import androidx.room.*
import com.laioffer.spotify.datamodel.Album
import kotlinx.coroutines.flow.Flow

//我们的app可以有很多个Dao, 我们需要什么功能就inject什么Dao
@Dao
interface DatabaseDao {

    //已经favorite过了就会触发onConflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun favoriteAlbum(album: Album)

    @Query("SELECT EXISTS(SELECT * FROM Album WHERE id = :id)")
    fun isFavoriteAlbum(id : Int) : Flow<Boolean>

    @Delete
    suspend fun unFavoriteAlbum(album: Album)

    //给Favorite page服务
    @Query("select * from Album")
    fun fetchFavoriteAlbums(): Flow<List<Album>>

}