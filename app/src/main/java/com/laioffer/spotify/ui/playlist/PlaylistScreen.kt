package com.laioffer.spotify.ui.playlist

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.laioffer.spotify.R
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.datamodel.Song
import com.laioffer.spotify.player.PlayerUiState
import com.laioffer.spotify.player.PlayerViewModel

//实际的页面的内容需要compose这个library来帮助实现
@Composable
fun PlaylistScreen(
    playlistViewModel: PlaylistViewModel,
    //这个Screen的状态来自于两个viewModel
    playerViewModel: PlayerViewModel
) {
    //把我们viewModel里面的stateFlow转化为compose能够用的State
    //当viewModel发生变化, 就会使得compose的状态发生变化
    val playlistUiState by playlistViewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()

    PlaylistScreenContent(
        playlistUiState = playlistUiState,
        playerUiState = playerUiState,
        onTapFavorite = {
            Log.d("PlaylistScreen", "Tap favorite $it")
            //因为控制爱心变化是state的变化, state是在viewModel里面handle, 而不是像跳转一样需要在Fragment里面处理
            //所以我们不需要把这个callback function传给Fragment
            playlistViewModel.toggleFavorite(it)
        },
        onTapSong = {
            playerViewModel.load(it, playlistUiState.album)
            playerViewModel.play()
        } 
    )
}

//为什么要再写一个playlistScreenContent, 为了stateless
@Composable
private fun PlaylistScreenContent(
    playlistUiState: PlaylistUiState,
    playerUiState: PlayerUiState,
    onTapFavorite: (Boolean) -> Unit,
    onTapSong: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp),
    ) {
        Cover(
            album = playlistUiState.album,
            isFavorite = playlistUiState.isFavorite,
            onTapFavorite = onTapFavorite
        )
        PlaylistHeader(album = playlistUiState.album)

        PlaylistContent(
            playlist = playlistUiState.playlist,
            currentSong = playerUiState.song,
            onTapSong = onTapSong
        )
    }
}


@Composable
private fun Cover(
    album: Album,
    isFavorite: Boolean,
    onTapFavorite: (Boolean) -> Unit,
    //callback function就是点了之后它知道要去回call哪个function
) {
    Column(
        modifier = Modifier.fillMaxWidth()
        //需要整个和屏幕一样宽
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                modifier = Modifier
                    //固定大小的爱心
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    //trigger一个onTap callback function
                    .clickable { onTapFavorite(!isFavorite) },
                painter = painterResource(
                    id = if (isFavorite) {
                        R.drawable.ic_favorite_24
                    } else {
                        R.drawable.ic_unfavorite_24
                    }
                ),
                //染色
                tint = if (isFavorite) {
                    Color.Green
                } else {
                    Color.Gray
                },
                //是给盲人用的
                contentDescription = ""
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1.0f)
                    .align(Alignment.Center)
            ) {
                // Vinyl background
                Image(
                    modifier = Modifier
                        .fillMaxSize(),
                    painter = painterResource(id = R.drawable.vinyl_background),
                    contentDescription = null
                )

                //AsyncImage 网络download的image
                AsyncImage(
                    model = album.cover,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(1.0f)
                        .align(Alignment.Center)
                        .clip(CircleShape),
                    contentScale = ContentScale.FillBounds
                )
            }

        }
        Text(
            text = album.description,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.caption,
            color = Color.Gray,
        )
    }
}


@Composable
private fun PlaylistHeader(album: Album) {
    Column {
        Text(
            text = album.name,
            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            //stringPlaceHolder有更通用的做法, 为了更好的翻译为别的语言, 应该在string.xml里面来做
            text = stringResource(id = R.string.album_info, album.artists, album.year),
            style = MaterialTheme.typography.body2,
            color = Color.LightGray,
        )
    }
}

@Composable
private fun PlaylistContent(
    playlist: List<Song>,
    currentSong: Song?,
    onTapSong: (Song) -> Unit
) {
    //这个state用来记住一下之前的ListState
    val state = rememberLazyListState()
    LazyColumn(state = state) {
        items(playlist) { song ->
            Song(
                song,
                //这个地方当被选中一首歌,这首歌将会显示被select的状态
                currentSong == song,
                onTapSong
            )

        }

        item {
            //最底下给一个40dp的空白是为了防止用户点不到, 因为我们后面要做一个floating playing bar
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


@Composable
private fun Song(
    song: Song,
    isPlaying: Boolean,
    onTapSong: (Song) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable { onTapSong(song) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = song.name,
                style = MaterialTheme.typography.body2,
                color = if (isPlaying) {
                    Color.Green
                } else {
                    Color.White
                }
            )
            Text(
                text = song.lyric,
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
        }

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = song.length,
            style = MaterialTheme.typography.caption,
            color = Color.LightGray
        )
    }
}
