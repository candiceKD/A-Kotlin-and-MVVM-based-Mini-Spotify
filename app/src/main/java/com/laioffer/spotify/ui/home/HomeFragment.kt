package com.laioffer.spotify.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.laioffer.spotify.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    //为什么不写成@inject? 因为系统已经帮我们把额外的操作做完了
    private val viewModel: HomeViewModel by viewModels()


    override fun onCreateView( // onCreateView就是马上要创建UI view
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //所以feed在这句代码的状态为空, isLoading状态为true
        //return inflater.inflate(R.layout.fragment_home, container, false) 这个是老的XML的写法, 我们已经不用了, 用下面的ComposeView来替换
        //这个是最原始的代码, 是帮我们创建一个layout文件, 然后把这个layout填充满整个container
        // Inflate the layout for this fragment
        return ComposeView(requireContext()).apply {
            //用一个ComposeView, 这样viewModel和compose就联系起来了
            setContent {
                MaterialTheme(colors = darkColors()){
                    HomeScreen(viewModel, onTap = {
                        Log.d("HomeFragment", "We tapped ${it.name}")
                        val direction = HomeFragmentDirections.actionHomeFragmentToPlaylistFragment(it)
                        //就是用Fragment name加Direction, 然后点action操作
                        //这是一个code generate 出来的class, 其实navigation controller就是通过这个class来做nav direction,
                        // 这class里面的field hashmap可以放album
                        findNavController().navigate(directions = direction)
                        //direction也是bottom bar在跳转时候使用的确切的class, 不管是页面上的跳转还是bottom bar的跳转都是最终在direction中完成的

                        //onTap最终要让HomeFragment来处理,navController就是把拿到的graph set进Fragment里面, 所以离navController最近的是Fragment
                        //所以Fragment也可以拿到controller, 那我们就要把onTap这个具体的function一层层向上放在HomeFragment里面来处理
                        //之所以不把navController传下去到在AlbumCover里面直接处理,是因为还是isolation的原则, 当其他人想要在album上做点击之后发生其他逻辑的时候无法复用, 只能发生跳转这一件事
                    })
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //如果isLoading这个state是true, 那我们就去做fetch数据
        if (viewModel.uiState.value.isLoading) {
            viewModel.fetchHomeScreen()
        }
    }
}
