package com.laioffer.spotify.datamodel

import com.google.gson.annotations.SerializedName

data class Playlist(
    @SerializedName("id")
    //加这个annotation是为了序列化的时候, 把json里面的id译制成albumId
    val albumId: String,
    val songs: List<Song>
)
