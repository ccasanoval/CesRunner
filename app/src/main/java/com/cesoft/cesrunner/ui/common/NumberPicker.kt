package com.cesoft.cesrunner.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


//@Composable
//fun NumberPicker(
//    ini: Int,
//    min: Int,
//    max: Int,
//    onChange: (Int) -> Unit = {},
//) {
//
//}


@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    min: Int,
    max: Int,
    value: Int,
    onSelect: (Int) -> Unit
) {
    val length = max - min + 3
    val listState = rememberLazyListState(value - min)
    val listStateFirst = remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val coroutineScope = rememberCoroutineScope()

    var oldValue by remember { mutableIntStateOf(value) }
    var currentValue by remember { mutableIntStateOf(value) }

    LaunchedEffect(!listState.isScrollInProgress && oldValue != currentValue) {
        coroutineScope.launch {
            onSelect(currentValue)
            oldValue = currentValue
            listState.animateScrollToItem(index = listState.firstVisibleItemIndex)
        }
    }

    Box(modifier = Modifier.height(106.dp)) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            items(count = length, itemContent = {
                val index = it + min - 1
                if (it == listStateFirst.value + 1) {
                    currentValue = index
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    modifier = modifier.alpha(if (it == listStateFirst.value + 1) 1f else 0.3f),
                    text = index.toString(),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.02.em,
                        fontSize = 22.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
            })
        }

        val spinnerGradient = Brush.linearGradient(
            0.0f to Color(0xFFFFFFFF),
            0.3f to Color(0x00000000),
            0.5f to Color(0x00000000),
            0.7f to Color(0x00000000),
            1.0f to Color(0xFFFFFFFF),
            start = Offset(0.0f, -13.0f),
            end = Offset(0.0f, 320.0f)
        )
        Spacer(
            modifier = modifier
                .background(brush = spinnerGradient)
                .height(106.dp)
                .width(120.dp)
        )
    }
}