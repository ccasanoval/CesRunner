package com.cesoft.cesrunner.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fontBig
import kotlinx.coroutines.launch

@Composable
fun NumberPicker(
    title: String = "",
    subtitle: String = "",
    modifier: Modifier = Modifier,
    min: Int,
    max: Int,
    value: Int,
    onSelect: (Int) -> Unit
) {
    Row(modifier = modifier) {
        val mod2 = Modifier.padding(horizontal = SepMin)
        if(title.isNotBlank())
            Text(text = title, fontSize = fontBig, modifier = mod2.weight(.5f))
        HorizontalNumberPicker(mod2, min, max, value, onSelect)
        if(subtitle.isNotBlank())
            Text(subtitle, fontSize = fontBig, textAlign = TextAlign.Right, modifier = mod2)
    }
}

@Composable
fun HorizontalNumberPicker(
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
            if(currentValue > max) currentValue = max
            if(oldValue != currentValue) {
                onSelect(currentValue)
                oldValue = currentValue
            }
            //listState.animateScrollToItem(index = listState.firstVisibleItemIndex)
        }
    }

    //TODO: Calc width needed to show 3 items...
    val w = if(max >= 999) 150.dp
            else if(max >= 99) 110.dp
            else 75.dp
    Box(modifier = modifier.width(w)) {
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            state = listState
        ) {
            items(count = length, itemContent = {
                val index = it + min - 1
                if (it == listStateFirst.value + 1) {
                    currentValue = index
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if(index < min || index > max) " " else index.toString(),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.02.em,
                        fontSize = fontBig
                    ),
                    modifier = Modifier
                        .padding(horizontal = SepMin)
                        .alpha(if (it == listStateFirst.value + 1) 1f else 0.3f),
                )
                Spacer(modifier = Modifier.height(6.dp))
            })
        }
    }
}
//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun NumberPicker_Preview() {
    val modifier = Modifier.border(1.dp, Color.Red)
    Surface {
        Column {
            NumberPicker("AAA", "bb", modifier, 0, 9, 5) { }
            Text("+++++AAAA++++", modifier = Modifier.size(SepMed))
            NumberPicker("CCCC", "", modifier, 0, 99, 80) { }
            Text("+++++AAAA++++", modifier = Modifier.size(SepMed))
            NumberPicker("ZZZ", "VVV", modifier, 0, 999, 950) { }
        }
    }
}

@Preview
@Composable
private fun HorizontalNumberPicker_Preview() {
    val modifier = Modifier.border(1.dp, Color.Red)
    Surface {
        Column {
            HorizontalNumberPicker(modifier, 0, 10, 5) { }
            Text("AAAA", modifier = Modifier.size(SepMed))
            HorizontalNumberPicker(modifier, 30, 300, 150) { }
        }
    }
}
