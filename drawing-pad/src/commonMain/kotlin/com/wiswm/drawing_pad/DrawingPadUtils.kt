package com.wiswm.drawing_pad

expect class DrawingPadUtils() {
    fun saveStrokesToPng(strokeList: List<DrawingPadViewModel.Stroke>, width: Int, height: Int): String?
    fun deleteFolder(): Boolean
}