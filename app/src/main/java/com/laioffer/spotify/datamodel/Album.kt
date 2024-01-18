package com.laioffer.spotify.datamodel

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
//我们这里要把Album作为一个Entity, 作为database中的一个table
@Entity
data class Album(
    @PrimaryKey
    val id: Int,
    //因为在server端的json文件中, 没有使用name这个key,而是叫album 为了在serialized的时候不会让converter confused, 所以要加一个annotation
    @SerializedName("album")
    val name: String,
    val year: String,
    val cover: String,
    val artists: String,
    val description: String
): Serializable {
    //关键词告诉系统这个data model可以serializable
    //一个class能够被序列化,代表它所有的field都能被序列化
    //但是function没有办法序列化, 如果class带有function是没办法序列化的

    //目的是: viewModel的创立之初需要给Album一个初始状态
    //这是一个dummy的Album
        companion object {
        fun empty(): Album {
            return Album(
                id = -1,
                "",
                "",
                "",
                "",
                "",
            )
        }
    }
}
