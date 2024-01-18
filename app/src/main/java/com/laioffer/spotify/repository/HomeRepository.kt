package com.laioffer.spotify.repository

import com.laioffer.spotify.datamodel.Section
import com.laioffer.spotify.network.NetworkApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

//从后端拉数据是在Model层完成的, 然后所有的data都是用Repository来命名
class HomeRepository @Inject constructor(private val networkApi: NetworkApi) {
    //因为我们要去后端拿回数据, 所以HomeRepository需要depends on NetworkApi

    //suspend和withContext关键词调用这个function必须在coroutine上执行, 使得变成了main -safe function
    suspend fun getHomeSections(): List<Section> {
        //告诉coroutine把这个function放在IO线程上
        return withContext(Dispatchers.IO) {
            //delay(3000)
            networkApi.getHomeFeed().execute().body()!!
        }
    }
}