package com.kukosin.app.ui

import kotlin.math.abs

internal enum class CoverGestureAxis { TRACK, LYRICS, MINIMIZE }

/** Decide el eje una sola vez después de superar el slop del sistema. */
internal fun classifyCoverGesture(dx: Float, dy: Float, lyricsProgress: Float, touchSlop: Float): CoverGestureAxis? {
    if (maxOf(abs(dx), abs(dy)) <= touchSlop) return null
    return when {
        abs(dx) > abs(dy) -> CoverGestureAxis.TRACK
        lyricsProgress > 0.01f || dy < 0f -> CoverGestureAxis.LYRICS
        else -> CoverGestureAxis.MINIMIZE
    }
}

/** Un fling decidido gana; de lo contrario se usa la distancia recorrida. */
internal fun shouldOpenLyrics(progress: Float, velocityY: Float): Boolean =
    velocityY < -520f || (progress > 0.42f && velocityY < 520f)
