package com.kukosin.app.ui

import androidx.compose.ui.geometry.Rect
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

/** La portada conserva un margen táctil incluso al estar reducida o en pausa. */
internal fun expandedCoverHitBounds(rect: Rect, paddingPx: Float): Rect {
    if (rect.width <= 0f || rect.height <= 0f) return Rect.Zero
    val padding = paddingPx.coerceAtLeast(0f)
    return Rect(rect.left - padding, rect.top - padding, rect.right + padding, rect.bottom + padding)
}

/** Un fling decidido gana; un arrastre moderado también abre las letras. */
internal fun shouldOpenLyrics(progress: Float, velocityY: Float): Boolean =
    velocityY < -520f || (progress > 0.32f && velocityY < 520f)
