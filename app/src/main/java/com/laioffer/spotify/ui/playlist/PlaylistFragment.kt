package com.laioffer.spotify.ui.playlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.laioffer.spotify.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment : Fragment() {
    //把Album传进来拿到, album这些data都叫做Arguments, 这都是固定写法
    private val navArgs by navArgs<PlaylistFragmentArgs>()
    //拿到数据之后,跳转操作都完成了, 然后我们要按照MVVM的框架来构造页面, 把viewModel传进来
    //这个viewModel是一个Fragment scope viewModel, 是Fragment的extension function, 它的lifeCycle与Fragment有关
    private val viewModel: PlaylistViewModel by viewModels()
    //这个viewModel是一个MainActivity viewModel, 它的lifecycle与MainActivity有关
    //当屏幕rotate, Activity会被重新创建, 但是这个viewModel不会消失, 因为viewModel本来就是handle activity状态的\
    //只有这个app消失了, 这个viewModel才会消失
    private val playerViewModel: PlayerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(colors = darkColors()) {
                    //PlaylistScreen要用在Fragment里面
                    PlaylistScreen(
                        playlistViewModel = viewModel,
                        playerViewModel = playerViewModel
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("PlaylistFragment", navArgs.album.toString())
        //当view已经创建了之后, fetch这个Playlist
        viewModel.fetchPlaylist(navArgs.album)
    }
}