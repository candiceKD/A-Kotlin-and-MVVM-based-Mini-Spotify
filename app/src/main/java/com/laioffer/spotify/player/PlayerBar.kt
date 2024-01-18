package com.laioffer.spotify.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.laioffer.spotify.R
import com.laioffer.spotify.ui.theme.TransparentBlack

@Composable
fun PlayerBar(viewModel: PlayerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    //当刚进入的时候state是null的时候, 不希望显示出来
    val isVisible = uiState.album != null && uiState.song != null

    //这个内部定义了一个remember, 当visible这个state发生变化, 会有一个动画fit in效果
    //这里只是一个引导我们发散的引子
    AnimatedVisibility(isVisible) {

        PlayerBarContent(uiState = uiState,
            togglePlay = {
                if (uiState.isPlaying) {
                    viewModel.pause()
                } else {
                    viewModel.play()
                }
            },
            seekTo = {
                viewModel.seekTo(it)
            }
        )
    }
}

@Composable
private fun PlayerBarContent(
    uiState: PlayerUiState,
    //给icon使用的
    togglePlay: () -> Unit,
    seekTo: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TransparentBlack)
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp, top = 8.dp, bottom = 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = uiState.album?.cover,
                contentDescription = null,
                modifier = Modifier
                    .width(60.dp)
                    .aspectRatio(1.0f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.FillBounds
            )
//有权重的会在其他没有权重的component占据完自己该占据的空间之后, fill remaining space
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = uiState.song?.name ?: "",
                    style = MaterialTheme.typography.body2,
                    color = Color.White,
                )
                Text(
                    text = uiState.song?.lyric ?: "",
                    style = MaterialTheme.typography.caption,
                    color = Color.White,
                )
            }

            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        togglePlay()
                    },
                painter = painterResource(
                    id = if (uiState.isPlaying) {
                        R.drawable.ic_pause_24
                    } else {
                        R.drawable.ic_play_arrow_24
                    }
                ),
                tint = Color.White,
                contentDescription = ""
            )
        }

        SeekBar(
            uiState.currentMs.toFloat(),
            uiState.durationMs.toFloat()
        ) {
            seekTo(it)
        }
    }
}

//这是一个stateful component
@Composable
//currentMs: Float, durationValue: Float,这两个state是viewModel传过来的state
private fun SeekBar(currentMs: Float, durationValue: Float, seekTo: (Long) -> Unit) {
    var seekBarPosition by remember { mutableStateOf(0f) }
    var seeking by remember { mutableStateOf(false) }
    if (!seeking) {
        seekBarPosition = currentMs
    }
    //Slider是系统自带的一个compose 组件
    Slider(
        modifier = Modifier.height(24.dp),
        value = seekBarPosition,
        valueRange = 0f..durationValue,
        //拖得时候会trigger 这个function
        onValueChange = {
            //seeking改为true, ignore viewModel的state
            seeking = true
            seekBarPosition = it

        },
        onValueChangeFinished = {
            //拖完松手之后seeking改为false, 然后去call seekTo的callback function
            seekTo(seekBarPosition.toLong())
            seeking = false
        },
        colors = SliderDefaults.colors(
            thumbColor = Color.Transparent,
            inactiveTrackColor = Color.LightGray,
            activeTrackColor = Color.Green
        )
    )
}