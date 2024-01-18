package com.laioffer.spotify.repository

import com.laioffer.spotify.datamodel.Playlist
import com.laioffer.spotify.network.NetworkApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
//因为我们要去后端拿回数据, 所以HomeRepository需要depends on NetworkApi
class PlaylistRepository @Inject constructor(
    private val networkApi: NetworkApi
) {
    suspend fun getPlaylist(id: Int): Playlist = withContext(Dispatchers.IO) {
        networkApi.getPlaylist(id).execute().body()!!
    }
}
//用等号接住,就不需要return