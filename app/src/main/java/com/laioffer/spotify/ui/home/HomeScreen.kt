package com.laioffer.spotify.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.laioffer.spotify.R
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.datamodel.Section

//UI view component都写在这里
//这里是compose, 要加composable 的annotation
//作为MVVM的view, 需要的viewModel的UI state
@Composable
fun HomeScreen(viewModel: HomeViewModel, onTap: (Album) -> Unit) {
    // collect values from this stateFLow and represents its latest value via state.
    //这里需要的是compose state, 而viewModel里面的是UI state
    //compose state 有一个关键词叫remember, 所以我们需要转换一下
    // val uiState by viewModel.uiState这里是拿到的stateFLow
    val uiState by viewModel.uiState.collectAsState()
//把HomeScreenContent写在HomeScreen里面, 把转化好的compose state传进来
    //为什么要再写一个HomeScreenContent, 因为我们要展现更多的stateless component
    HomeScreenContent(uiState = uiState, onTap = onTap)
}

//下面就是按照页面设计来进行逐步拆解, 用最基础的component来组成页面,
// component break 思想
//stateless 思想
@Composable
fun HomeScreenContent(uiState: HomeUiState, onTap: (Album) -> Unit) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        //把HomeScreenHeader放在LazyColumn的item里面
        item {
            HomeScreenHeader()
        }
//Kotlin版的if + switch
        when {
            uiState.isLoading -> {
                item {
                    //如果是loading的状态, 就调用LoadingSection
                    LoadingSection(stringResource(id = R.string.screen_loading))
                    //这里想要的就是一个text, 最好的是在strings.xml里面定义
                    //因为在strings.xml里面定义可以实现多语言
                }

            }
            else -> {
                //如果不loading的状态, 就显示一个list of item, feed里面take一个list
                // 有几个section, 就显示几个section
                items(uiState.feed) { item ->
                    AlbumSection(section = item, onTap = onTap)
                }
            }
        }


    }
}

@Composable
fun HomeScreenHeader() {
    Column {
        Text(
            stringResource(id = R.string.menu_home),
            style = MaterialTheme.typography.h4,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp)) //这个地方我们加了一个空格Spacer
        //当然也可以不用Column, 直接在Text上面加一个bottom padding
        //modifier= Modifier.padding(bottom = 16.dp),
    }
}

@Composable
private fun AlbumSection(section: Section, onTap: (Album) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            //按照后端feed里的sectionTitle
            text = section.sectionTitle,
            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)//每一个item都有一点的间隔
        ) {
            items(section.albums) { item ->
                AlbumCover(item, onTap)
            }
        }

    }
}

@Composable
private fun AlbumCover(album: Album, onTap: (Album) -> Unit) {//给一个onTap callback function
    //Kotlin是一个Functional programming, 所以就是我们传一个Album进来, 一旦trigger 这个onTap function, 就会发生你定义的功能
    Column(modifier = Modifier.clickable { onTap(album) }) {
        Box(modifier = Modifier.size(160.dp)) {
            AsyncImage(
                model = album.cover,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Text(
                text = album.name,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 4.dp, start = 2.dp)
                    .align(Alignment.BottomStart),
            )
        }

        Text(
            text = album.artists,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            color = Color.LightGray,
        )
    }
}


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

