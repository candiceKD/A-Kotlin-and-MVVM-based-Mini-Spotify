package com.laioffer.spotify.datamodel

import com.google.gson.annotations.SerializedName

data class Section(
    @SerializedName("section_title")
    //这里使用annotation, 是因为json文件里面section_title是一个snake case, 而java需要的是camelCase, 所以要加一个annotation给converter
    val sectionTitle: String,
    val albums: List<Album>
)
