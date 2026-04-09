package com.wiswm.drawing_pad

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import com.wiswm.drawing_pad.DrawingPadViewModel.Stroke
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.wiswm.drawing_pad.resources.Colors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

class DrawingPadScreen(
    val taskTag: String,
    val onClose: (taskTag: String, photoPath: String?) -> Unit
) {
    companion object {
        const val TAG = "DrawingPadScreen"
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun Content(
        iconBack: DrawableResource,
        iconConfirm: DrawableResource,
        iconErase: DrawableResource
    ) {
        val drawingPadViewModel = remember { DrawingPadViewModel(taskTag, onClose) }

        Box(modifier = Modifier.background(Color.Black)) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                containerColor = Colors.PacificBlue,
                content = { padding ->
                    ContentView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        drawingPadViewModel = drawingPadViewModel
                    )
                },
                bottomBar = {
                    BottomView(
                        drawingPadViewModel = drawingPadViewModel,
                        iconBack = iconBack,
                        iconConfirm = iconConfirm,
                        iconErase = iconErase
                    )
                }
            )
        }
    }

    @Composable
    private fun ContentView(
        modifier: Modifier,
        drawingPadViewModel: DrawingPadViewModel
    ) {
        Box(modifier = modifier.background(Color.White)) {
            DrawingPad(drawingPadViewModel)
        }
    }

    @Composable
    private fun BottomView(
        drawingPadViewModel: DrawingPadViewModel,
        iconBack: DrawableResource,
        iconConfirm: DrawableResource,
        iconErase: DrawableResource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Black.copy(alpha = 0.8f)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    drawingPadViewModel.closeScreen()
                },
            ) {
                Icon(
                    painter = painterResource(iconBack),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
            IconButton(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    drawingPadViewModel.clearDrawing()
                },
            ) {
                Icon(
                    painter = painterResource(iconErase),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
            IconButton(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    drawingPadViewModel.saveDrawing()
                    drawingPadViewModel.clearDrawing()
                    drawingPadViewModel.closeScreen()
                },
            ) {
                Icon(
                    painter = painterResource(iconConfirm),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun DrawingPad(drawingPadViewModel: DrawingPadViewModel) {
        var currentStroke by remember { mutableStateOf<Stroke?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke = Stroke(mutableListOf(offset))
                        },
                        onDrag = { change, _ ->
                            currentStroke = currentStroke?.copy(
                                points = currentStroke!!.points.toMutableList().apply {
                                    add(change.position)
                                }
                            )
                        },
                        onDragEnd = {
                            currentStroke?.let {
                                drawingPadViewModel.updateDrawing(it)
                            }
                            currentStroke = null
                        },
                        onDragCancel = { currentStroke = null }
                    )
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        drawingPadViewModel.updateDrawing(
                            heightPx = size.height,
                            widthPx = size.width
                        )
                    }
            ) {
                drawingPadViewModel.state.drawing?.let { nnDrawing ->
                    for (s in nnDrawing.strokeList) drawStroke(s)
                }
                currentStroke?.let { drawStroke(it) }
            }
        }
    }

    private fun DrawScope.drawStroke(s: Stroke) {
        if (s.points.size > 1) {
            val path = Path().apply {
                moveTo(s.points.first().x, s.points.first().y)
                s.points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path = path,
                color = Color.Black,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = s.strokeWidth)
            )
        }
    }
}