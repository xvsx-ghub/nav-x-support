package com.wiswm.drawing_pad

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel

class DrawingPadViewModel(
    taskTag: String,
    val onClose: (taskTag: String, photoPath: String?) -> Unit
) : ViewModel() {
    companion object {
        const val TAG = "DrawingPadViewModel"
    }

    enum class Event {
        UNKNOWN,
        COSE_SCREEN
    }

    data class State(
        val taskTag: String,
        val drawingFilePath: String?,
        val drawing: Drawing?
    )

    var state by mutableStateOf(State(taskTag, null, Drawing(strokeList = mutableListOf(), heightPx = 0, widthPx = 0)))
        private set

    data class Stroke(
        val points: MutableList<Offset> = mutableListOf(),
        val strokeWidth: Float = 4f
    )

    data class Drawing(
        val strokeList: MutableList<Stroke>,
        val widthPx: Int,
        val heightPx: Int
    )

    fun updateDrawing(stroke: Stroke) {
        val strokeList = state.drawing?.strokeList
        strokeList?.add(stroke)
        strokeList?.let {
            state = state.copy(drawing = state.drawing?.copy(strokeList = it))
        }
    }

    fun updateDrawing(widthPx: Int, heightPx: Int) {
        state = state.copy(drawing = state.drawing?.copy(widthPx = widthPx, heightPx = heightPx))
    }

    fun saveDrawing() {
        state.drawing?.let { nnDrawing ->
            val filePath = DrawingPadUtils().saveStrokesToPng(
                strokeList = nnDrawing.strokeList,
                width = nnDrawing.widthPx,
                height = nnDrawing.heightPx
            )

            state = state.copy(drawingFilePath = filePath)
        }
    }

    fun clearDrawing() {
        state =
            state.copy(drawing = Drawing(strokeList = mutableListOf(), heightPx = 0, widthPx = 0))
    }

    fun closeScreen() {
        onClose(state.taskTag, state.drawingFilePath)
    }
}