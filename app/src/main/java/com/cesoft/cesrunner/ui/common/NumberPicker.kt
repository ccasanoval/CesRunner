package com.cesoft.cesrunner.ui.common

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.findFirstRoot
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.cesoft.cesrunner.ui.settings.SettingsPage
import com.cesoft.cesrunner.ui.settings.SettingsViewModel
import com.cesoft.cesrunner.ui.theme.SepMax
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.max

private fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

@Composable
fun BorderedText(state: LazyListState, index: Int, value: Int) {
    val borderColor by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            val itemInfo = visibleItemsInfo.firstOrNull { it.index == index}
            itemInfo?.let {
                val delta = it.size/2
                val center = state.layoutInfo.viewportEndOffset / 2
                val childCenter = it.offset + it.size / 2
                val target = childCenter - center
                if (target in -delta..delta) return@derivedStateOf Red
            }
            Transparent
        }
    }
    //val borderColor = if(index == value) MaterialTheme.colorScheme.secondary else Transparent
    val textColor = MaterialTheme.colorScheme.primary
    Text(
        text = index.toString(),
        style = TextStyle(fontSize = 20.sp),
        color = textColor,
        modifier = Modifier
            .size(50.dp)
            .border(2.dp, borderColor, CircleShape)
            .wrapContentHeight(),
        textAlign = TextAlign.Center
    )
}

@Composable
fun NumberPicker(
    ini: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit = {},
) {
    var inc = 0
    var value by rememberSaveable { mutableIntStateOf(ini) }
    var off = max(max - value, value - min)
    var offMin = value - off - 3
    var offMax = value + off + 3

    val state = rememberLazyListState()
    //val snapBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
    LaunchedEffect(ini, min, max) {
        inc = 0
        value = ini
        off = max(max - value, value - min)
        offMin = value - off - 3
        offMax = value + off + 3
        state.scrollToItem(off)
        val z = state.firstVisibleItemIndex
        android.util.Log.e("AAA", "LAUNCHEd $value $min $max *********  $z ******** ${off}")
    }

//    val nestedScrollConnection = remember {
//        object : NestedScrollConnection {
//            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
//
//                inc += available.x.toInt() / 50
//                //value += inc / 50
//                android.util.Log.e("AAAA", "---------AAA------ $value ${inc}  ------")
//                return Offset.Zero
//            }
//        }
//    }

    val leftRightFade = Brush.horizontalGradient(
        0f to Transparent,
        0.5f to MaterialTheme.colorScheme.primary,
        1f to Transparent
    )

    val list = mutableListOf<@Composable () -> Unit>()
    for(i in offMin..offMax) {
        if(i < min) list.add {Spacer(Modifier.size(50.dp).border(1.dp, Blue))}
        else if(i > max) list.add {Spacer(Modifier.size(50.dp).border(1.dp, Blue))}
        else list.add { BorderedText(state, i, value) }
    }

    LazyRow(
        state = state,
        //flingBehavior = snapBehavior,
        modifier = Modifier
            //.nestedScroll(nestedScrollConnection)
            .fadingEdge(brush = leftRightFade)
            .fillMaxWidth(),
        //horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        itemsIndexed(list) { index, item ->
            item()
        }
//        for(i in offMin..offMax) {
//            if(i < min) item { Spacer(Modifier.size(50.dp).border(1.dp, Blue)) }
//            else if(i > max) item { Spacer(Modifier.size(50.dp).border(1.dp, Blue)) }
//            else item { BorderedText(state, i, value) }
//        }
    }
}


//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun SettingsPage_Preview() {
    Surface {
        Column {
            NumberPicker(5, 0, 10)
            NumberPicker(2, 1, 47)
            NumberPicker(8, 5, 9)
            //NumberPicker(1, 1, 9)
            //NumberPicker(0, 0, 5)
            //NumberPicker(5, 5, 9)
        }
    }
}

/*
//==================================================================================================
@Composable
fun NumberPicker2(
    value: Int,
    min: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    assert(value < max)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val contentPadding = (maxWidth - 50.dp) / 2
        val offSet = maxWidth / 5
        val itemSpacing = offSet - 50.dp
        val pagerState = rememberPagerState(pageCount = { max })
        LaunchedEffect(value, max) {
            pagerState.scrollToPage(value)
        }
        val context = LocalContext.current
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                Toast.makeText(context,"$page",Toast.LENGTH_SHORT).show()
            }
        }

        val scope = rememberCoroutineScope()

        val mutableInteractionSource = remember {
            MutableInteractionSource()
        }

        CenterCircle(
            modifier = modifier.size(50.dp).align(Alignment.Center),
            fillColor = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp
        )

        HorizontalPager(
            modifier = modifier,
            state = pagerState,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(0)
            ),
            contentPadding = PaddingValues(horizontal = contentPadding),
            pageSpacing = itemSpacing,
        ) { page ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .graphicsLayer {
                        val pageOffset = ((pagerState.currentPage - page) + pagerState
                            .currentPageOffsetFraction).absoluteValue
                        // Set the item alpha based on the distance from the center
                        val percentFromCenter = 1.0f - (pageOffset / (5f / 2f))
                        val opacity = 0.25f + (percentFromCenter * 0.75f).coerceIn(0f, 1f)

                        alpha = opacity
                        clip = true
                    }
                    .clickable(
                        interactionSource = mutableInteractionSource,
                        indication = null,
                        enabled = true,
                    ) {
                        scope.launch { pagerState.animateScrollToPage(page) }
                    }) {
                Text(
                    text = "$page",
                    color = if(page >= min) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(50.dp)
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CenterCircle(
    modifier: Modifier = Modifier,
    fillColor: Color,
    strokeWidth: Dp
) {
    Canvas(
        modifier = modifier.size(75.dp)
    ) {
        drawArc(
            color = fillColor,
            0f,
            360f,
            true,
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
    }
}*/