package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

@Serializable
data class SelectionRectQuad(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float
)

@Serializable
data class SelectionAnchor(
    val clientX: Float = 0f,
    val clientY: Float = 0f,
    val clientBottom: Float = 0f,
    val clientWidth: Float = 0f,
    val clientHeight: Float = 0f
)

@Serializable
data class TextSelectionPayload(
    val text: String = "",
    val pageNumber: Int = 1,
    val quads: List<SelectionRectQuad> = emptyList(),
    val anchor: SelectionAnchor = SelectionAnchor()
)

val HIGHLIGHT_COLOR_PRESETS = listOf(
    0xFFFFFF98.toInt(), // Yellow
    0xFF53FFBC.toInt(), // Green
    0xFF80EBFF.toInt(), // Blue
    0xFFFFCBE6.toInt(), // Pink
    0xFFFF4F5F.toInt()  // Red
)

@Composable
fun SelectionActionBar(
    isVisible: Boolean,
    anchor: SelectionAnchor,
    selectedColor: Int? = null,
    isEditExistingHighlight: Boolean = false,
    onColorSelected: (Int) -> Unit,
    onDeleteHighlight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Convert pixel coordinates (scaled by devicePixelRatio) to Dp
    val leftDp: Dp
    val topDp: Dp
    val bottomDp: Dp
    val widthDp: Dp
    with(density) {
        leftDp = anchor.clientX.toDp()
        topDp = anchor.clientY.toDp()
        bottomDp = anchor.clientBottom.toDp()
        widthDp = anchor.clientWidth.toDp()
    }

    // Estimated toolbar dimensions:
    // 5 colors + icon + divider + paddings ≈ 180dp (or ~220dp with delete button)
    val estimatedToolbarWidth = if (isEditExistingHighlight) 226.dp else 190.dp
    val estimatedToolbarHeight = 44.dp

    // Center horizontally on selected text, clamped with 8dp screen edge padding
    val selectionCenterX = leftDp + (widthDp / 2)
    val offsetX = (selectionCenterX - (estimatedToolbarWidth / 2)).coerceIn(
        8.dp,
        (screenWidth - estimatedToolbarWidth - 8.dp).coerceAtLeast(8.dp)
    )

    // Position vertically: 30dp below selection by default (to avoid native selection handles).
    // If not enough space below, flip to above the selection.
    val spaceBelow = screenHeight - (bottomDp + 30.dp + estimatedToolbarHeight)
    val offsetY = if (spaceBelow < 40.dp && topDp > (estimatedToolbarHeight + 16.dp)) {
        (topDp - estimatedToolbarHeight - 12.dp).coerceAtLeast(8.dp)
    } else {
        (bottomDp + 30.dp).coerceAtMost(screenHeight - estimatedToolbarHeight - 8.dp)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.9f, animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(120)) + scaleOut(targetScale = 0.9f, animationSpec = tween(120)),
            modifier = Modifier
                .offset(x = offsetX, y = offsetY)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small FormatColorFill Icon
                    Icon(
                        imageVector = Icons.Rounded.FormatColorFill,
                        contentDescription = "Highlight",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(22.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // 5 Color Circles
                    HIGHLIGHT_COLOR_PRESETS.forEach { colorInt ->
                        val color = Color(colorInt)
                        val isSelected = selectedColor == colorInt
                        val borderWidth = if (isSelected) 2.dp else 1.dp
                        val borderColor = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    border = BorderStroke(borderWidth, borderColor),
                                    shape = CircleShape
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onColorSelected(colorInt) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(13.dp),
                                    tint = Color.Black.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Optional delete button if in edit mode
                    if (isEditExistingHighlight) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        IconButton(
                            onClick = onDeleteHighlight,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete Highlight",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
