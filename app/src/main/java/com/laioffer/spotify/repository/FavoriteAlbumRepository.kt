package com.laioffer.spotify.repository

import com.laioffer.spotify.database.DatabaseDao
import com.laioffer.spotify.datamodel.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FavoriteAlbumRepository @Inject constructor(private val databaseDao: DatabaseDao) {

    //这里没有加suspend, 这个流不是一个coroutine操作
    //这个就是一个监测这个id的Album的favorite的状态的改变, 一旦状态改变这个流的状态就会改变
    fun isFavoriteAlbum(id: Int): Flow<Boolean> =
        databaseDao.isFavoriteAlbum(id).flowOn(Dispatchers.IO)

    //suspend代表这个function只能在coroutine上执行
    suspend fun favoriteAlbum(album: Album) = withContext(Dispatchers.IO) {
        databaseDao.favoriteAlbum(album)
    }

    suspend fun unFavoriteAlbum(album: Album) = withContext(Dispatchers.IO) {
        databaseDao.unFavoriteAlbum(album)
    }

    fun fetchFavoriteAlbums(): Flow<List<Album>> =
        databaseDao.fetchFavoriteAlbums().flowOn(Dispatchers.IO)
}

