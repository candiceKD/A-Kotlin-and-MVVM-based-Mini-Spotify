package com.laioffer.spotify.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

//然后就需要new一个Retrofit的client出来帮我们来implement function call
@Module
//这个annotation的意思是Hilt就会把这个class当作一个factory Module, 一个dependency helper
@InstallIn(SingletonComponent::class)
//SingletonComponent指的是scope, SingletonComponent是最上面的那个scope, 所有的都可以用
//object = java中的static class, 就是我们不需要new, 可以直接点操作
object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    //因为我们使用emulator虚拟机去访问localhost, 所以虚拟机会所加一层映射, 我们的localhost url就会变成10.0.2.2
    //这个是在network_security_config里面配置的

    @Provides
    //加一个Provides, 这样Hilt就能把Retrofit这个不是我们自己创建的object从Module找出来提供出来了
    @Singleton
    //这个Singleton的意思是只创建一个
    //这是一个非常固定的标准化写法, 给NetworkApi提供Retrofit, 下面的写法就是如何build一个Retrofit
    //类似于创建一个Retrofit的实体
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient())//Http协议就是包裹在OkHttpClient内部的
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): NetworkApi {
        //然后这个实体去使用这些Api
        return retrofit.create(NetworkApi::class.java)
    }
}
//先有build出Retrofit然后才能由此provide NetworkApi