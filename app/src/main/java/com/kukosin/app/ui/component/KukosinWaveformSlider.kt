// KukosinWaveformSlider.kt — réplica del GIF + paleta Kukosin
// Ubicación: app/src/main/java/com/kukosin/app/ui/component/KukosinWaveformSlider.kt
// Reemplaza el Slider del NowPlayingSheet (posición + volumen siguen separados)

package com.kukosin.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kukosin.app.ui.theme.*
import kotlin.math.pow
import kotlin.random.Random

/**
 * Slider de onda con estética 1:1 del GIF + tema Kukosin.
 *
 * Visual del GIF que replica:
 * ┌─────────────────────────────────────────────┐
 * │  ▒▒▒▒▓▓▓▓████ │ ████▓▓▓▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ← onda continua que se DESPLAZA
 * │              │  ← aguja central Gold (1.6dp) con glow
 * │     ◁◁  ◀◀  ▶  ▶▶  ▷▷      ← 5 botones flotando POR ENCIMA de la onda
 * │ 0:00                          3:53        ← tiempos en esquinas
 * └─────────────────────────────────────────────┘
 *
 * - La onda NO es estática: progress mueve el contenido bajo la aguja fija.
 * - Botones flotan encima (zIndex): no empujan la onda, van superpuestos.
 * - Colores Kukosin: Cream sólido (ya reproducido), Cream 28% (por reproducir), Gold para aguja/play.
 */
@Composable
fun KukosinWaveformSlider(
    progress: Float,                 // 0f..1f, animado desde controller.currentPosition
    onSeek: (Float) -> Unit,         // seek real al soltar
    modifier: Modifier = Modifier,
    waveform: List<Float>? = null,   // 150-200 valores 0..1. Null = demo bonito
    isPlaying: Boolean = true,
    onPlayPause: () -> Unit = {},
    onPrev: () -> Unit = {},
    onNext: () -> Unit = {},
    onShuffle: () -> Unit = {},
    onRepeat: () -> Unit = {},
    // Paleta Kukosin — sigue el AccentTheme (Gold cambia con el tono del usuario)
    playedColor: Color = Cream,                       // sólido como en el GIF pero crema de la app
    remainingColor: Color = Cream.copy(alpha = 0.22f),// translúcido, se distingue perfecto sobre Ink
    centerLineColor: Color = Gold,                   // aguja en acento (oro por defecto)
    durationLabel: String = "", // pásale formatTime(duration) si quieres mostrar durac.
    positionLabel: String = "", // pásale formatTime(position) — se muestra en esquina izq
) {
    val clamped = progress.coerceIn(0f, 1f)
    var dragProgress by remember { mutableStateOf<Float?>(null) }
    val displayProgress = dragProgress ?: clamped

    // Onda larga para que el scroll se sienta infinito (como el GIF)
    val bars = remember(waveform) {
        val base = waveform ?: generateKukosinWaveform(180)
        if (base.size >= 160) base else expandWaveform(base, 180)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(138.dp)                // alto total del bloque (igual que el GIF: onda 64 + controles 44)
            .clip(RoundedCornerShape(18.dp))
            .background(
                // Fondo del slot: Ink con degradado cálido sutil (como el GIF pero en Ink)
                Brush.verticalGradient(
                    colors = listOf(
                        InkElevated.copy(alpha = 0.95f),
                        Ink.copy(alpha = 1f),
                    )
                )
            )
            .border(1.dp, Line.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
    ) {
        // ── 1. ONDA (detrás) ──────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(bars, displayProgress) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2f
                        // Tap sutil: mueve desde la posición actual, no salto absoluto a 0
                        val delta = (offset.x - centerX) * 0.45f / size.width
                        val newP = (displayProgress + delta).coerceIn(0f, 1f)
                        onSeek(newP)
                    }
                }
                .pointerInput(bars) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragProgress = displayProgress },
                        onDragEnd = {
                            dragProgress?.let { onSeek(it.coerceIn(0f, 1f)) }
                            dragProgress = null
                        },
                        onDragCancel = { dragProgress = null }
                    ) { change, dragAmount ->
                        change.consume()
                        // Velocidad más suave: 0.45x para control fino
                        val delta = -dragAmount * 0.45f / size.width
                        dragProgress = (dragProgress!! + delta).coerceIn(0f, 1f)
                    }
                }
        ) {
            val barW = 3.2.dp.toPx()
            val gap = 2.4.dp.toPx()
            val slot = barW + gap
            val contentW = bars.size * slot - gap
            val w = size.width
            val h = size.height
            // zona de la onda: ocupa de y=10dp a y=78dp (dejando aire para tiempos abajo)
            val waveTop = 12.dp.toPx()
            val waveH = 78.dp.toPx()
            val waveCenterY = waveTop + waveH / 2f
            val centerX = w / 2f

            // Mapeo scroll: 0 → inicio de onda en el centro, 1 → final en el centro
            val startX = centerX - displayProgress * contentW

            // brillo sutil detrás de la aguja (como el glow del GIF)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        centerLineColor.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                    startX = centerX - 28.dp.toPx(),
                    endX = centerX + 28.dp.toPx(),
                ),
                topLeft = Offset(centerX - 28.dp.toPx(), waveTop),
                size = Size(56.dp.toPx(), waveH),
            )

            bars.forEachIndexed { i, amp ->
                val hFrac = 0.14f + amp.coerceIn(0f, 1f) * 0.86f
                val bh = waveH * hFrac
                val x = startX + i * slot
                if (x + barW < -8.dp.toPx() || x > w + 8.dp.toPx()) return@forEachIndexed

                val played = (x + barW / 2f) <= centerX
                val c = if (played) playedColor else remainingColor

                // barra con esquinas bien redondeadas (igual que el GIF)
                drawRoundRect(
                    color = c,
                    topLeft = Offset(x, waveCenterY - bh / 2f),
                    size = Size(barW, bh),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                )
            }

            // AGUJA CENTRAL — línea Gold fina, exactamente al centro (como el GIF)
            // Sombra/glow para que destaque sobre la onda
            drawRoundRect(
                color = centerLineColor.copy(alpha = 0.22f),
                topLeft = Offset(centerX - 1.4.dp.toPx(), waveTop + 2.dp.toPx()),
                size = Size(2.8.dp.toPx(), waveH - 4.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx()),
            )
            drawRoundRect(
                color = centerLineColor,
                topLeft = Offset(centerX - 0.8.dp.toPx(), waveTop + 4.dp.toPx()),
                size = Size(1.6.dp.toPx(), waveH - 8.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx()),
            )
        }

        // ── 2. CONTROLES FLOTANDO ENCIMA DE LA ONDA (zIndex 2) ─────────────
        // Como en el GIF: 5 botones centrados, superpuestos a la onda
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 4.dp) // ligeramente arriba del centro para quedar sobre la onda
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 1) Ghost izquierda (rewind largo) — muy sutil, como en el GIF
            IconButton(
                onClick = onShuffle, // en tu app puede ser shuffle o rewind 10s
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Cream.copy(alpha = 0.42f)),
            ) {
                Icon(Icons.Rounded.FastRewind, null, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(4.dp))

            // 2) Anterior — círculo InkElevated con borde Gold tenue (estilo Kukosin)
            IconButton(
                onClick = onPrev,
                modifier = Modifier
                    .size(48.dp)
                    .background(InkElevated, CircleShape)
                    .border(1.dp, Gold.copy(alpha = 0.35f), CircleShape),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Cream),
            ) {
                Icon(Icons.Rounded.SkipPrevious, "Anterior", modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(10.dp))

            // 3) Play/Pause — grande, Gold sólido + icono Ink (idéntico a tu FilledIconButton 72dp del sheet)
            // Reducido a 56dp para que flote bien sobre la onda sin taparla toda (el sheet lo tiene en 72dp fuera de la onda)
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .background(Gold, CircleShape)
                    .border(1.dp, GoldDim, CircleShape),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Ink),
            ) {
                Icon(
                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (isPlaying) "Pausar" else "Reproducir",
                    modifier = Modifier.size(34.dp),
                )
            }

            Spacer(Modifier.width(10.dp))

            // 4) Siguiente
            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(48.dp)
                    .background(InkElevated, CircleShape)
                    .border(1.dp, Gold.copy(alpha = 0.35f), CircleShape),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Cream),
            ) {
                Icon(Icons.Rounded.SkipNext, "Siguiente", modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(4.dp))

            // 5) Ghost derecha
            IconButton(
                onClick = onRepeat,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Cream.copy(alpha = 0.42f)),
            ) {
                Icon(Icons.Rounded.FastForward, null, modifier = Modifier.size(20.dp))
            }
        }

        // ── 3. TIEMPOS EN ESQUINAS INFERIORES (como el GIF 0:00 / 3:53) ─────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (positionLabel.isNotEmpty()) positionLabel else "0:00",
                color = Cream,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
            Text(
                text = if (durationLabel.isNotEmpty()) durationLabel else "3:53",
                color = Cream.copy(alpha = 0.72f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

// ────────────────────────── Helpers ─────────────────────────────────────

private fun generateKukosinWaveform(count: Int): List<Float> {
    val rnd = Random(42) // seed fijo para que la forma sea estable y no "baila"
    return List(count) { i ->
        val t = i / count.toFloat()
        val env = kotlin.math.sin(t * Math.PI).toFloat() * 0.28f + 0.72f
        val low = (kotlin.math.sin(t * Math.PI * 6) * 0.22
                + kotlin.math.sin(t * Math.PI * 13) * 0.14
                + kotlin.math.sin(t * Math.PI * 22) * 0.06).toFloat()
        val noise = rnd.nextFloat() * 0.38f
        ((0.48f + low + noise) * env).coerceIn(0.10f, 1f)
    }
}

private fun expandWaveform(src: List<Float>, target: Int): List<Float> {
    if (src.size >= target) return src
    return List(target) { i ->
        val f = i * src.size.toFloat() / target
        val lo = f.toInt().coerceIn(src.indices)
        val hi = (lo + 1).coerceIn(src.indices)
        val frac = f - lo
        src[lo] * (1 - frac) + src[hi] * frac
    }
}

fun pcmToKukosinWaveform(pcm: ShortArray, bars: Int = 180): List<Float> {
    if (pcm.isEmpty()) return generateKukosinWaveform(bars)
    val perBar = (pcm.size.toFloat() / bars).toInt().coerceAtLeast(1)
    var globalMax = 1f
    val rms = FloatArray(bars) { b ->
        val s = b * perBar
        val e = minOf(s + perBar, pcm.size)
        if (s >= e) return@FloatArray 0f
        var sum = 0.0
        for (k in s until e) { val v = pcm[k] / 32768f; sum += v * v }
        kotlin.math.sqrt(sum / (e - s)).toFloat().also { if (it > globalMax) globalMax = it }
    }
    return rms.map { ((it / globalMax).coerceIn(0f, 1f).toDouble().pow(0.60)).toFloat() }
}

// ────────────────────────── Uso drop-in en NowPlayingSheet ──────────────
/*
// En NowPlayingSheet, reemplaza el Slider actual por:
var seeking by remember { mutableStateOf(false) }
var seekProgress by remember { mutableFloatStateOf(0f) }
val rawProgress = if (duration > 0) position.toFloat() / duration else 0f
val progress = if (seeking) seekProgress else rawProgress

KukosinWaveformSlider(
    progress = progress,
    onSeek = { p ->
        val target = (p * duration).toLong()
        controller.seekTo(target)
        seeking = false
    },
    waveform = remember(track.uri) { /* pcmToKukosinWaveform(decode(track), 180) */ null },
    isPlaying = isPlaying,
    onPlayPause = { if (controller.isPlaying) controller.pause() else controller.play() },
    onPrev = { controller.seekToPrevious() },
    onNext = { controller.seekToNext() },
    positionLabel = formatTime(if (seeking) (seekProgress * duration).toLong() else position),
    durationLabel = formatTime(duration),
    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
)

// Si arrastras, actualiza seekProgress en el gesture (ya lo hace el componente con dragProgress),
// pero si quieres preview sin commit hasta soltar, intercepta:
LaunchedEffect(seeking) { ... }

El Volumen sigue con su Slider separado abajo (no se toca).
*/
