package com.laioffer.spotify

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
//加上这个annotation, Hilt就知道这个项目的dependency tree的入口在这里
class MainApplication: Application() { //这是Android这个项目的实际入口
    //为什么需要这个入口, 因为我们用Hilt来帮助我们做dependency injection, 它的作用域是整个dependency tree, 能够帮助我们new 和拿到tree上的每一个node
    //所以我们需要知道这个tree的 root节点,作为Hilt遍历整个tree的入口
}

