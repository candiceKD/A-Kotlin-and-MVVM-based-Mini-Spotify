package com.laioffer.spotify.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.repository.FavoriteAlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteAlbumRepository: FavoriteAlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState(emptyList()))
    val uiState: StateFlow<FavoriteUiState> = _uiState

    //之前的写法是写一个fetchFavoriteAlbums function, 这里是另外一种写法
    //这种写法不需要再onViewCreated阶段来执行这个function, init会在viewModel创建完之后执行
    //二者的执行时间几乎没有区别, 因为viewModel是lazyBuild, 会在Fragment第一次出现viewModel时候被创建
    //也就是onCreateView时候创建, 因为viewModel的生命周期是在Fragment的声明周期内部的
    //fetchHomeScreen也可以写成这样, 但是fetchPlaylistScreen不可以, 因为要依赖于传入的其他数据才能创建
    init {
        viewModelScope.launch {
            favoriteAlbumRepository.fetchFavoriteAlbums().collect {
                _uiState.value = FavoriteUiState(it)
            }
        }
    }

}

data class FavoriteUiState(
    val albums: List<Album>
)