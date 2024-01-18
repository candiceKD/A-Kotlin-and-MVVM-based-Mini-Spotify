package com.laioffer.spotify

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.room.Room
import coil.compose.AsyncImage
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.laioffer.spotify.database.AppDatabase
import com.laioffer.spotify.database.DatabaseDao
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.network.NetworkApi
import com.laioffer.spotify.player.PlayerBar
import com.laioffer.spotify.player.PlayerViewModel
import com.laioffer.spotify.repository.HomeRepository
import com.laioffer.spotify.ui.theme.SpotifyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
//这个EntryPoint是每一个scope的入口, Hilt通过这个入口,能够把这个scope的所有的dependency都提供了
class MainActivity : AppCompatActivity() {
    //Kotlin里面的static value和function 是用companion object来替换static关键词的
    /*   companion object {
            val a = "hello world"
            val b = "laioffer"

            fun helloWorld(){
            }
          }
        这些value 和function都可以通过点操作来拿到
    */

    @Inject
    lateinit var api: NetworkApi
    //因为MainActivity这个class是Android framework替我创建的, 所以我们无法用constructor injection, 因为我们不能construct它
    //这个时候我们只能用field injection
    @Inject
    //这里主要是为了测试使用
    lateinit var databaseDao: DatabaseDao
    //因为我们的playerBar要放在mainActivity里面, 所以我们要先创建playerViewModel
    private val playerViewModel: PlayerViewModel by viewModels()

    private val TAG = "Network"

//onCreate这个方法是从父类继承来的, 因为前面有override, 父类AppCompatActivity是Android framework提供的
    //这个function只做两件事一个是pull data,一个是set UI长什么样子
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    //R代表Resource folder的缩写, java或者kotlin要从res文件下读文件都是这种写法, 代表resource文件夹下的layout文件夹里的activity_main文件
        Log.d(TAG, "We are at onCreate()");


    //因为我们要实现, click navigation tab就能找到对应的页面, 所以需要先把这个view找到
    //这个就类似于web开发里面的findElementById, findElementsByClassName
    // 它能够自动帮我们handle click这个操作, 所以我们不需要写, 但是我们需要自己写click之后链接到哪个页面这个逻辑
    //findViewById需要告诉它view的类型, 所以尖括号里加的就是view的类型, Home和Favorite这两个Fragment都属于这个view
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

   //这里去找FragmentContainer
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

    //UI里面只要想要实现跳转这个逻辑, 就需要一个class叫navController, 是用来跳转的工具
    //先从Fragment Container里面拿到navController, 拿的方法就是先找到这个FragmentContainer
        val navController = navHostFragment.navController

    //找到controller之后把graph set给controller, controller就知道以什么逻辑去跳转了
        navController.setGraph(R.navigation.nav_graph)
    //然后把controller setup给我们之前找到的navView, 那么view拥有了controller这个tool又有了graph这个knowledge,就能实现点击哪个tab跳转显示哪个view
        NavigationUI.setupWithNavController(navView, navController)

    //下面这部分可以没有, 之前是有个bug必须有, 最近修复了
        navView.setOnItemSelectedListener {
            NavigationUI.onNavDestinationSelected(it, navController)
            navController.popBackStack(it.itemId, inclusive = false)
            true
        }

    //先去找到activity_main里的ComposeView通过id找到playerBar
        val playerBar = findViewById<ComposeView>(R.id.player_bar)
        playerBar.apply {
            //setContent就定义了UI长什么样子
            setContent {
                MaterialTheme(colors = darkColors()) {
                    PlayerBar(
                        playerViewModel
                    )
                }
            }
        }

        // Test retrofit
        GlobalScope.launch(Dispatchers.IO) {
            //要实现NetworkApi这个interface就要先有一个Retrofit的client出来帮我们来implement function call
            //val retrofit = NetworkModule.provideRetrofit       去call这个object里面的一个function来build 一个retrofit
            //val api = retrofit.create(NetworkApi::class.java)   用这个retrofit去create一个NetworkApi类型的interface
            //val call = api.getHomeFeed()  调用api里的function call去拿到一个call类型的task, List<Section> 是这个call task完成的结果的样子
            //val response = call. execute()   去执行这个task 拿到后端call 返回的response
            //val sections = response.body()    这个response里面还有其他的一些组成部分, 我们需要的是list of sections, 就是response的body部分

            //一般不会像上面一样分步来写, 只不过上面是为了好理解
            //val api = NetworkModule.provideRetrofit().create(NetworkApi::class.java)
            val response = api.getHomeFeed().execute().body()
            Log.d("Network", response.toString())
        }

        // remember it runs everytime you start the app
    //这种写法和上面写法都可以,这是两种写法
//        GlobalScope.launch {
//            withContext(Dispatchers.IO) {
//                val album = Album(
//                    id = 1,
//                    name =  "Hexagonal",
//                    year = "2008",
//                    cover = "https://upload.wikimedia.org/wikipedia/en/6/6d/Leessang-Hexagonal_%28cover%29.jpg",
//                    artists = "Lesssang",
//                    description = "Leessang (Korean: 리쌍) was a South Korean hip hop duo, composed of Kang Hee-gun (Gary or Garie) and Gil Seong-joon (Gil)"
//                )
//                databaseDao.favoriteAlbum(album)
//            }
//        }

    }
    //学习如何打log和打断点来帮助我们debug
    //log是Android framework提供的, d是debug, TAG类似于书签
//    override fun onStart() {
//        super.onStart()
//        Log.d(TAG, "We are at onStart()")
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Log.d(TAG, "We are at onResume()")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Log.d(TAG, "We are at onPause()")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.d(TAG, "We are at onStop()")
//    }
//
//    override fun onDestroy() {
//        Log.d(TAG, "We are at onDestroy()")
//        super.onDestroy()
//    }


}

//@Composable
//fun HelloContent() {
//
//    // state
//    var name by remember { mutableStateOf("") }
//
//    // (state, setState) = ""
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text(
//            text = "Hello!",
//            modifier = Modifier.padding(bottom = 8.dp),
//            style = MaterialTheme.typography.body2
//        )
//        OutlinedTextField(
//            value = name,
//            onValueChange = {
//                name = it
//            },
//            label = { Text("Name") }
//        )
//    }
//}

@Composable
private fun LoadingSection(text: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            color = Color.White
        )
    }
}

@Composable
fun AlbumCover() {
    Column() {
        Box(modifier = Modifier.size(160.dp)) {
            AsyncImage(
                model = "https://upload.wikimedia.org/wikipedia/en/d/d1/Stillfantasy.jpg",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Text( // ctrl + J
                text = "Still Fantasy",
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp, start = 2.dp).align(Alignment.BottomStart)
            )
        }
        Text(
            text = "jay Chu",
            modifier = Modifier.padding(top = 4.dp),
            style=MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            color = Color.White)
    }
}


@Preview(showBackground = true, name = "Preview LoadingSection")
@Composable
fun DefaultPreview() {
    SpotifyTheme {
        Surface {
            AlbumCover()
        }
    }
}
