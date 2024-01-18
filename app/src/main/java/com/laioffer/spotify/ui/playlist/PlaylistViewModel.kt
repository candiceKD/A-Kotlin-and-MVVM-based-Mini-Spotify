package com.laioffer.spotify.ui.playlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.datamodel.Song
import com.laioffer.spotify.repository.FavoriteAlbumRepository
import com.laioffer.spotify.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    //需要的inject的dependency是playlistRepository
    private val playlistRepository: PlaylistRepository,
    private val favoriteAlbumRepository: FavoriteAlbumRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        //三个state中isFavorite和playlist都是有初始值的,所以我们这里只需要传入Album
        PlaylistUiState(
            //为什么需要一个empty的, 因为在创建一个object之初就需要传一个值进去, 要传值就只能在constructor里面传进去
            //但是如果在constructor传入Album就需要告诉Hilt dependency injection如何把Album传过来, 需要很麻烦的操作, 需要Associated
            //因为Album不是我们new出来的, 是后端server传过来的, 那我们就用Album.empty这个dummy node
            Album.empty()
        )
    )
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    //做了一个fetchPlaylist function
    fun fetchPlaylist(album: Album) {
        //在onViewCreated进行fetchPlaylist的时候把从后端server拿到的album这个时候换上, 对于用户来说是观感没有区别的
        _uiState.value = _uiState.value.copy(album = album)

        //下面这个network才是一个花时间的操作
        viewModelScope.launch {
            val playlist = playlistRepository.getPlaylist(album.id)
            _uiState.value = _uiState.value.copy(playlist = playlist.songs)
            Log.d("PlaylistViewModel", _uiState.value.toString())
        }
        //在这里launch第二个coroutine, 但是收集这个流的状态是一个coroutine操作
        viewModelScope.launch {
            favoriteAlbumRepository.isFavoriteAlbum(album.id).collect{
                _uiState.value = _uiState.value.copy(
                    isFavorite = it
                )
            }
        }
    }

    fun toggleFavorite(isFavorite: Boolean) {
        //_uiState.value = _uiState.value.copy(isFavorite = isFavorite)
        //只写上面这一行点击爱心状态会改变, 因为uiState变了, 但是再进入页面刷新的时候还是实心的,因为database并没有改变

        //这里也可以不用flow, 那么就需要每次去拉isFavorite的值, 然后来更新uiState, 但这样写不好的原因是,需要有两个source control
        //因为我们要确保database的操作是成功了,才能去更新uiState状态,所以我们需要去try catch database操作,
        // 而如果用Flow就只需要监测database状态
        val album = _uiState.value.album
        viewModelScope.launch {
            if (isFavorite) {
                favoriteAlbumRepository.favoriteAlbum(album)
            } else {
                favoriteAlbumRepository.unFavoriteAlbum(album)
            }
        }
    }

}

data class PlaylistUiState(
    val album: Album,
    val isFavorite: Boolean = false,
    val playlist: List<Song> = emptyList()
)