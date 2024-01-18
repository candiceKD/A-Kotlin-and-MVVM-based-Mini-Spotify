package com.laioffer.spotify.network

import com.laioffer.spotify.datamodel.Playlist
import com.laioffer.spotify.datamodel.Section
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

//因为Get feed和 Get playlist有很多相同的code, 这里使用interface是因为我们不需要去实现它,interface的作用只是用来描述我的request长什么样子
//而Retrofit可以根据我们的描述帮我们create一个object出来实现我们的function call
//类似于我们之前学的spring
interface NetworkApi {
    @GET("feed") //request的样子
    fun getHomeFeed(): Call<List<Section>> //response的样子
    //在做playlistFragment的时候再定义一个新的api
    @GET("playlist/{id}")
    //用到@Path的一个属性, 把id作为一个parameter传进去
    fun getPlaylist(@Path("id") id: Int): Call<Playlist>
}