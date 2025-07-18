package com.retrivedmods.wclient.render

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1f) {
    companion object {
        val RED = Color(1f, 0f, 0f)
        val BLACK = Color(0f, 0f, 0f)
        val WHITE = Color(1f, 1f, 1f)

        fun fromRGB(r: Int, g: Int, b: Int, a: Int = 255): Color {
            return Color(r / 255f, g / 255f, b / 255f, a / 255f)
        }
    }
}
