package com.laioffer

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

//因为我们要把json file deserialize成kotlin list, 所以我们需要先new一些data class来进行映射和对应
//必须用这个annotation
@Serializable
data class Song(
    val name: String,
    val lyric: String,
    val src: String,
    val length: String
)

@Serializable
data class Playlist (
    val id: Long,
    val songs: List<Song>
)

//整个Playlists其实就是一个List<Playlist>


fun main() {
    //配置一个server, 然后把它start起来
    //Application::module中的::是一个function reference符号, 因为embeddedServer中的module需要的格式是module: Application.() -> Unit
    //代表的是在Application这个scope下的一个既没有输入也没有输出的lambda function, module是符合的,我们在下面用extension定义了这个function
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
    //为什么wait = true,因为我们需要卡住这个线程, 如果wait = false,那么当main 运行完就直接return了,就不能在listening你的request了,那么这个后端就不是active的

    //这就是handle nullable的方法, ?.let{}就相当于if (a?.length != null)
    var a: String? = "hello world"
    if (a?.length != null) {

    }
    a?.length?.let {
        print(it)
    } ?: run {
        // ..
    }

}

// extension
//Application class是Ktor这个库提供的, 代表的就是我们现在在运行的这个project
fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }
    // TODO: adding the routing configuration here
    //routing是一个function, 它里面传入的参数是一个lambda function, 当lambda是最后一个parameter的时候可以写在小括号外面,所以是下面这种结构
    routing {
        // endpoint, restful GET call
        //就是通过routing导航到不同的页面
        //"/"是根目录
        //就是把resource目录下的json文件和mp3文件return给前端
        //get因为是在一个lambda function的implementation里面, 所以它自带一个scope就是Routing
        //因为get里面能传两个parameter, 第一个是一个string, 第二个是一个interface, 这个interface通过查看进一步的详情,可以知道也是一个lambda function
        //所以最终将最后一个参数写在小括号外面
        get("/") {
            call.respondText("Hello World!")
        }

        get("/feed") {
            //!! 这个意思是一定不会为null, 但如果不幸为null,那么就会NPE
            //那么我们这里没有那么自信,所以我们要handle一下万一为null的这种情况所以用jsonString ?: "",就类似与if else
            //先把feed.json文件的jsonString读出来,然后塞到response里面
            //给一个返回内容的类型ContentType.Application.Json
            //为什么要用this::class.java.classLoader.getResource? 这是一个什么语法?????
/*
            this::class.java.classLoader：

            this 指当前类的实例。
            ::class 是 Kotlin 中获取类信息的语法。它类似于 Java 中的 .getClass()。
            java 是一个转换，将 Kotlin 的 KClass 转换为 Java 的 Class。
            classLoader 是 Java 类的一个属性，用于加载类和资源。
            getResource("feed.json")：

            这是 ClassLoader 类的一个方法，用于查找名为 "feed.json" 的资源。
            如果找到，它返回一个 URL 对象；如果没有找到，返回 null。
            ?.readText()：

            这里使用了 Kotlin 的安全调用操作符 ?.。
            这意味着如果 getResource 方法返回的不是 null，就调用 readText() 方法。
            readText() 是 Kotlin 的一个扩展方法，用于从 URL 读取所有文本内容，并将其作为字符串返回。
            如果 getResource 返回 null，那么整个表达式的结果也是 null
*/
            val jsonString = this::class.java.classLoader.getResource("feed.json")?.readText()
            call.respondText(jsonString ?: "", ContentType.Application.Json)
        }

        get("/playlists") {
            val jsonString = this::class.java.classLoader.getResource("playlists.json")?.readText()
            call.respondText(jsonString ?: "", ContentType.Application.Json)
        }

        //json文件是不能被解析的不能直接通过id拿到item,所以需要把json文件deserialized成一个kotlin list, 这样就能够通过index去拿到具体的一个item
        get("/playlist/{id}") {
            //如何handle nullable的情况? 用?.let{}
            //let里面的parameter是一个lambda function,所以写在小括号外面, 并且lambda function里面传入的是let拿到的不是null的结果
            this::class.java.classLoader.getResource("playlists.json")?.readText()?.let { jsonString ->
                //deserialization的方法,把json从string decode, 这个函数需要两个parameter, 一个是解码器,一个是string
                //因为playlists是一个list<playlist>所以需要用ListSerializer, 而Playlist又需要继续解码
                val playlists = Json.decodeFromString(ListSerializer(Playlist.serializer()), jsonString)
                //如何把id取出来的方式 {id}就是大括号传进来的就是parameter
                val id = call.parameters["id"]
                //如何filter? 找的满足条件的第一个item, 有可能给的这个id找不到, 所以需要用firstOrNull来handle一下
                //如果不想给lambda function起名字, 只需要一个传入的参数, 就可以直接用it
                //其实原本写作val item = playlists.firstOrNull{playlist -> playlist.id.toString() == id}
                val item = playlists.firstOrNull { it.id.toString() == id }
                //然后把找到的结果返回到response里面
                call.respondNullable(item)
            } ?: call.respond("null")
            //然后这里我们再用?:来handle 是null的情况
        }

        //当我们要听一首歌的时候, 如何把一首歌读出来, 就类似于下载
        //是一个静态方法, 读取的是根目录
        //static这个function有两个参数,第一个是一个string, 第二个是一个lambda,
        //相当于把static.songs这个文件夹暴露出来,变成public的,这样就能直接读每一首歌
        static("/") {
            staticBasePackage = "static"
            static("songs") {
                resources("songs")
            }
        }
    }
}