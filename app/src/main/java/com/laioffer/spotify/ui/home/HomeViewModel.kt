package com.laioffer.spotify.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laioffer.spotify.datamodel.Section
import com.laioffer.spotify.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//然后构建MVVM框架里面的viewModel层
@HiltViewModel
//这个annotation不光是帮我们把class和injection都创建出来,还会把stateflow额外的操作都完成
//必须要extend Google写好的viewModel class
class HomeViewModel @Inject constructor(private val repository: HomeRepository): ViewModel() {
    //MutableStateFlow是一个数据流, 当uiState发生改变, 数据流会发生update, 所有使用这个数据流的会接受到这个re-render
    //这个_uiState是可以改变的, 所以这个是viewModel层来使用的
    private val _uiState = MutableStateFlow(HomeUiState(feed = emptyList(), isLoading = true))
    //uiState这是不能改变的, immutable是只读的, 所以这个是给view层的Ui使用的
    //也就是说user不能自己改变uiState, 当user触发uiState改变的时候只能通过viewModel层来进行update
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    //viewModel里面的state都是UI state, 而view里面需要的是compose state

    //view来call这个function, 这个view也就是HomeFragment
    fun fetchHomeScreen() {
        //我让repository 去fetch data
        // 创建一个IO线程, 把Network request放在IO线程上来做
        //Scope的作用就是用来更好的管理thread, 我们可以在scope里面launch很多thread, 但只要scope被destroy了,thread全都没有了
        viewModelScope.launch {
            //Scope定义的是一个范围, 当viewModel被cancel掉了, 那么其中的Coroutine也就不复存在了, 这对不会产生多余的memory leak还是很重要的
            //然后拿到的数据去更新这两个state
            //这个代码还是在main thread上, 但是会被suspend, network request会在IO thread上执行,
            // 所以main thread会先去执行UI state update. //coroutine会被suspend,但是thread不会
            val sections = repository.getHomeSections()
            _uiState.value = HomeUiState(feed = sections, isLoading = false)
            Log.d("HomeViewModel", _uiState.value.toString())
        }
    }
}

//所以我们需要一个data class来定义UI state集中管理, 类似于React里面需要定义哪些state
data class HomeUiState(
    val feed: List<Section>,
    val isLoading: Boolean
)
