package com.laioffer.spotify.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.datamodel.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer
    //listener就类似于Kotlin的callback function
    //我们的做法是让PlayerViewModel去implement Player.Listener这个interface
) : ViewModel(), Player.Listener  {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        //this就是PlayerViewModel object自己
        exoPlayer.addListener(this)

        //这里我们用flow来监测currentMs state
        //这是一个在UI thread做的一个suspend function, 这个function不会block ui, 它只会隔一秒emit一次
        viewModelScope.launch {
            flow {
                while (true) {
                    //iPlaying这个状态是Model层知道这个state现在的状态
                    if (exoPlayer.isPlaying) {
                        emit(exoPlayer.currentPosition to exoPlayer.duration)
                    }
                    delay(1000)
                }
            }.collect {
                _uiState.value = uiState.value.copy(currentMs = it.first, durationMs = it.second)
                Log.d("SpotifyPlayer", "CurrentMs: ${it.first}, DurationMs: ${it.second}")
            }
        }
    }

    //切歌function
    fun load(song: Song, album: Album) {
        //先更新ui state的状态
        _uiState.value = PlayerUiState(album = album, song = song, isPlaying = false)
        //拿到当前歌曲的url
        val mediaItem = MediaItem.Builder().setUri(song.src).build()
        exoPlayer.setMediaItem(mediaItem)
        //先load当前歌曲
        exoPlayer.prepare()
    }

    fun play() {
    //匿名类写法:// exoPlayer.addListener(object:: Player.Listener {()}, 也就是这个object是现在这个class的子类, 内部类
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    //这里removeListener是为了防止memory leak
    //为什么会产生memory leak? 因为playerViewModel dependent exo player,
    // 那么构建得时候要先new exoPlayer, 再new playerViewModel
    //销毁得时候要先销毁viewModel, 没有人用exoPlayer得时候它就会被回收销毁
    //但当 exoPlayer.addListener(this), this就是PlayerViewModel object自己
    //那么就代表exoPlayer也dependent viewModel了, 所以形成了一个circle
    //就必须先removeListener,才能完成正常的销毁
    override fun onCleared() {
        exoPlayer.removeListener(this)
        super.onCleared()
    }

    //这里的做法和favorite很像, favorite的source of choose是database, 这里的source of choose是exoplayer
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        Log.d("SpotifyPlayer", isPlaying.toString())
        _uiState.value = _uiState.value.copy(
            isPlaying = isPlaying
        )
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Log.d("spotify", error.toString())
    }

    fun seekTo(positionMs: Long) {
        //我们先更新UI的状态, 让用户先看到ui的位置改变, 然后再让exoPlayer去做seekTo
        //因为seekTo到现在得位置再播放需要prepare
        _uiState.value = uiState.value.copy(
            currentMs = positionMs
        )
        exoPlayer.seekTo(positionMs)
    }
}

//定义需要的state
data class PlayerUiState(
    //这里为什么是nullable的是因为不是打开这个app就会有一个歌曲在播放
    val album: Album? = null,
    val song: Song? = null,
    val isPlaying: Boolean = false,
    val currentMs: Long = 0,
    val durationMs: Long = 0,
)
