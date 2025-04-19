package com.mucheng.mucute.client.overlay

import androidx.compose.runtime.Composable

object OverlayRenderer {
    private val renders = mutableMapOf<String, @Composable () -> Unit>()

    // Add a render to the overlay list
    fun addRender(key: String, render: @Composable () -> Unit) {
        renders[key] = render
    }

    // Remove a render by key
    fun removeRender(key: String) {
        renders.remove(key)
    }

    // Render all added overlays
    @Composable
    fun RenderAll() {
        renders.values.forEach { it() }
    }
}
