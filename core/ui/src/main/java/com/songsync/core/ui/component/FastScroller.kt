package com.songsync.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun FastScroller(
    items: List<String>, // List of display headers or titles
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var currentLetter by remember { mutableStateOf("") }
    var columnHeight by remember { mutableFloatStateOf(1f) }

    val alphabet = remember(items) {
        items.mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .distinct()
            .sorted()
    }

    if (alphabet.size < 3) return

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(40.dp)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Floating Letter Bubble
        AnimatedVisibility(
            visible = isDragging && currentLetter.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(x = -130, y = dragY.toInt() - 60) }
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentLetter,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Alphabet Track
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(28.dp)
                .onGloballyPositioned { coordinates ->
                    columnHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(alphabet, items) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragY = offset.y
                            updateLetterAndScroll(offset.y, columnHeight, alphabet, items, listState) { letter ->
                                currentLetter = letter
                            }
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onVerticalDrag = { change, _ ->
                            dragY = change.position.y.coerceIn(0f, columnHeight)
                            updateLetterAndScroll(dragY, columnHeight, alphabet, items, listState) { letter ->
                                currentLetter = letter
                            }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            alphabet.forEach { char ->
                Text(
                    text = char.toString(),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun updateLetterAndScroll(
    y: Float,
    totalHeight: Float,
    alphabet: List<Char>,
    items: List<String>,
    listState: LazyListState,
    onLetterSelected: (String) -> Unit
) {
    if (totalHeight <= 0f || alphabet.isEmpty()) return
    val fraction = (y / totalHeight).coerceIn(0f, 0.999f)
    val index = (fraction * alphabet.size).toInt()
    val char = alphabet[index]
    onLetterSelected(char.toString())

    val targetItemIndex = items.indexOfFirst {
        it.firstOrNull()?.uppercaseChar() == char
    }
    if (targetItemIndex >= 0) {
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            listState.scrollToItem(targetItemIndex)
        }
    }
}
