package com.cesoft.cesrunner.ui.common

import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.cesoft.cesrunner.ui.theme.Green
import com.cesoft.cesrunner.ui.theme.SepMed
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SlideToUnlock(
    isUnlocked: Boolean,
    onUnlock: () -> Unit,
    hintText: String,
    modifier: Modifier = Modifier,
) {
    Track(
        enabled = !isUnlocked,
        onUnlock = onUnlock,
        modifier = modifier,
    ) { dragState, swipeFraction ->
        Hint(
            text = hintText,
            swipeFraction = swipeFraction,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(PaddingValues(horizontal = ThumbSize + 8.dp)),
        )
        Thumb(
            isLoading = isUnlocked,
            modifier = Modifier.offset {
                IntOffset(dragState.offset.roundToInt(), 0)
            },
        )
    }
}

enum class Anchors { Start, End }
private fun calculateSwipeFraction(progress: Float, endOfTrackPx: Float): Float {
    return progress/endOfTrackPx
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Track(
    enabled: Boolean,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.(AnchoredDraggableState<*>, Float) -> Unit),
) {

    val density = LocalDensity.current
    var fullWidth by remember { mutableIntStateOf(0) }

    val horizontalPadding = SepMed

    val endOfTrackPx = remember(fullWidth) {
        with(density) { fullWidth - (2 * horizontalPadding + ThumbSize).toPx() }
    }

    val hapticFeedback = LocalHapticFeedback.current
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val dragState = remember(endOfTrackPx) {
        AnchoredDraggableState(
            initialValue = Anchors.Start,
            anchors = DraggableAnchors {
                Anchors.Start at 0f
                Anchors.End at endOfTrackPx
            },
            positionalThreshold = { d -> d * 0.9f},
            velocityThreshold = { Float.POSITIVE_INFINITY },
            snapAnimationSpec = tween(),
            decayAnimationSpec = decayAnimationSpec,
            confirmValueChange = { anchor ->
                if (anchor == Anchors.End) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUnlock()
                }
                true
            }
        )
    }
    val swipeFraction by remember(dragState.offset) {
        android.util.Log.e("Slide", "swipeFraction----******------ ${dragState.offset} / $endOfTrackPx = "+calculateSwipeFraction(dragState.offset, endOfTrackPx) )
        derivedStateOf { calculateSwipeFraction(dragState.offset, endOfTrackPx) }
    }
    val backgroundColor by remember(swipeFraction) {
        derivedStateOf { calculateTrackColor(swipeFraction) }
    }
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .onSizeChanged { fullWidth = it.width }
            .height(56.dp)
            .fillMaxWidth()
            .anchoredDraggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                enabled = enabled,
                interactionSource = interactionSource,
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(percent = 50),
            )
            .padding(
                PaddingValues(
                    horizontal = horizontalPadding,
                    vertical = 8.dp,
                )
            ),
        content = {
            content(dragState, swipeFraction)
        }
    )
}

private fun calculateTrackColor(swipeFraction: Float): Color {
    val endOfColorChangeFraction = 0.4f
    val fraction = (swipeFraction / endOfColorChangeFraction).coerceIn(0f..1f)
    return lerp(Green, Color.Red, fraction)
}

private val ThumbSize = 40.dp
@Composable
private fun Thumb(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ThumbSize)
            .background(color = Color.White, shape = CircleShape)
            .padding(8.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(2.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
        } else {
            Image(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun Hint(
    text: String,
    swipeFraction: Float,
    modifier: Modifier = Modifier,
) {
    val hintTextColor by remember(swipeFraction) {
        derivedStateOf { calculateHintTextColor(swipeFraction) }
    }
    Text(
        text = text,
        color = hintTextColor,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
    )
}
private fun calculateHintTextColor(swipeFraction: Float): Color {
    val endOfFadeFraction = 0.5f
    val fraction = (swipeFraction / endOfFadeFraction).coerceIn(0f..1f)
    return lerp(Color.White, Color.White.copy(alpha = 0f), fraction)
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun SideToUnlock_Preview() {
    Surface {
        SlideToUnlock(
            isUnlocked = false,
            onUnlock = {  },
            hintText = "Swipe to unlock"
        )
    }
}