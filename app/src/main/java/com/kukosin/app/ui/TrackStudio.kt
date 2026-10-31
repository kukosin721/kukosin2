package com.kukosin.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kukosin.app.data.analysis.AudioAnalysis
import com.kukosin.app.data.analysis.AuthenticityVerdict
import com.kukosin.app.data.analysis.EmbeddedLyrics
import com.kukosin.app.data.analysis.LyricLine
import com.kukosin.app.data.analysis.LyricWord
import com.kukosin.app.ui.theme.Cream
import com.kukosin.app.ui.theme.CreamMuted
import com.kukosin.app.ui.theme.Gold
import com.kukosin.app.ui.theme.Ink
import com.kukosin.app.ui.theme.InkSoft

@Composable
fun LyricsPanel(
    lyrics: EmbeddedLyrics?,
    positionMs: Long,
    modifier: Modifier = Modifier,
    onLineClick: ((Long) -> Unit)? = null,
    onScrollActivity: ((Boolean) -> Unit)? = null,
) {
    if (lyrics == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(InkSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Sin letras embebidas",
                    color = Cream,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Este archivo no trae USLT/SYLT, Vorbis LYRICS ni ©lyr.",
                    color = CreamMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    val synced = lyrics.synced
    val fade = Brush.verticalGradient(
        0f to Ink,
        0.08f to Color.Transparent,
        0.88f to Color.Transparent,
        1f to Ink,
    )

    if (synced.isEmpty()) {
        val plainListState = rememberLazyListState()
        ObserveUserScroll(plainListState, onScrollActivity)
        LazyColumn(
            state = plainListState,
            modifier = modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(fade)
                },
            contentPadding = PaddingValues(start = 12.dp, end = 8.dp, top = 28.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            item { LyricsSourceChip("${lyrics.source} · texto") }
            item {
                Text(
                    text = lyrics.plainText,
                    color = Cream,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        return
    }

    val active = synced.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (active + 1).coerceIn(0, synced.size),
    )
    ObserveUserScroll(listState, onScrollActivity)
    val lastActive = remember { mutableIntStateOf(active) }
    LaunchedEffect(active) {
        if (active != lastActive.intValue) {
            lastActive.intValue = active
            runCatching {
                listState.animateScrollToItem((active + 1).coerceAtMost(synced.size))
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                drawRect(fade)
            },
        contentPadding = PaddingValues(start = 12.dp, end = 8.dp, top = 48.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        item {
            LyricsSourceChip(
                if (synced.any { it.isKaraoke }) "${lyrics.source} · karaoke" else "${lyrics.source} · sincronizada",
            )
        }
        itemsIndexed(synced, key = { index, line -> "${line.timeMs}-$index" }) { index, line ->
            LyricVerse(
                line = line,
                nextTimeMs = synced.getOrNull(index + 1)?.timeMs,
                positionMs = positionMs,
                isActive = index == active,
                isPast = index < active,
                onSeek = onLineClick,
            )
        }
    }
}

private val LyricsEasing = CubicBezierEasing(0.32f, 0f, 0.16f, 1f)

@Composable
private fun ObserveUserScroll(
    listState: LazyListState,
    onScrollActivity: ((Boolean) -> Unit)?,
) {
    LaunchedEffect(listState, onScrollActivity) {
        if (onScrollActivity == null) return@LaunchedEffect
        listState.interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is DragInteraction.Start -> onScrollActivity(true)
                is DragInteraction.Stop, is DragInteraction.Cancel -> onScrollActivity(false)
                else -> Unit
            }
        }
    }
}

@Composable
private fun LyricsSourceChip(label: String) {
    Surface(color = InkSoft, shape = RoundedCornerShape(20.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Gold.copy(alpha = 0.85f),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun LyricVerse(
    line: LyricLine,
    nextTimeMs: Long?,
    positionMs: Long,
    isActive: Boolean,
    isPast: Boolean,
    onSeek: ((Long) -> Unit)?,
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.965f,
        animationSpec = tween(durationMillis = 1100, easing = LyricsEasing),
        label = "verseScale",
    )
    val blurRadius by animateDpAsState(
        targetValue = if (isActive) 0.dp else 2.dp,
        animationSpec = tween(durationMillis = 1200, easing = LyricsEasing),
        label = "verseBlur",
    )
    val color by animateColorAsState(
        targetValue = when {
            isActive -> Gold
            isPast -> Cream.copy(alpha = 0.28f)
            else -> CreamMuted.copy(alpha = 0.38f)
        },
        animationSpec = tween(durationMillis = 1100, easing = LyricsEasing),
        label = "verseColor",
    )
    val fontSize by animateFloatAsState(
        targetValue = if (isActive) 28f else 22f,
        animationSpec = tween(durationMillis = 1100, easing = LyricsEasing),
        label = "verseFontSize",
    )
    val lineHeight by animateFloatAsState(
        targetValue = if (isActive) 36f else 30f,
        animationSpec = tween(durationMillis = 1100, easing = LyricsEasing),
        label = "verseLineHeight",
    )
    val drift by animateDpAsState(
        targetValue = if (isActive) 0.dp else 1.dp,
        animationSpec = tween(durationMillis = 1100, easing = LyricsEasing),
        label = "verseDrift",
    )
    val words = if (line.words.isNotEmpty()) line.words else listOf(LyricWord(line.timeMs, line.text))
    val wordIndex = if (isActive) words.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0) else -1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = drift.toPx()
                transformOrigin = TransformOrigin(0f, 0.5f)
            }
            .clickable(enabled = onSeek != null) { onSeek?.invoke(line.timeMs) }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        if (line.isKaraoke) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Center,
            ) {
                words.forEachIndexed { i, word ->
                    val nextWord = words.getOrNull(i + 1)?.timeMs ?: nextTimeMs
                    KaraokeWord(
                        word = word,
                        nextTimeMs = nextWord,
                        positionMs = positionMs,
                        sung = isPast || (isActive && i < wordIndex),
                        current = isActive && i == wordIndex,
                        upcoming = !isPast && (!isActive || i > wordIndex),
                        dimmed = !isActive,
                        onSeek = onSeek,
                    )
                }
            }
        } else {
            Text(
                text = line.text,
                color = color,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = fontSize.sp,
                    lineHeight = lineHeight.sp,
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun KaraokeWord(
    word: LyricWord,
    nextTimeMs: Long?,
    positionMs: Long,
    sung: Boolean,
    current: Boolean,
    upcoming: Boolean,
    dimmed: Boolean,
    onSeek: ((Long) -> Unit)?,
) {
    val duration = ((nextTimeMs ?: (word.timeMs + 800L)) - word.timeMs).coerceAtLeast(80L)
    val progress = if (current) {
        ((positionMs - word.timeMs).toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val restColor = when {
        dimmed && sung -> Cream.copy(alpha = 0.28f)
        dimmed -> CreamMuted.copy(alpha = 0.38f)
        sung -> Cream
        upcoming -> CreamMuted.copy(alpha = 0.42f)
        else -> CreamMuted.copy(alpha = 0.42f)
    }
    val wordScale by animateFloatAsState(
        targetValue = if (current) 1.05f else 1f,
        animationSpec = tween(durationMillis = 700, easing = LyricsEasing),
        label = "wordScale",
    )
    val style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, lineHeight = 34.sp)

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = wordScale
                scaleY = wordScale
            }
            .clickable(enabled = onSeek != null) { onSeek?.invoke(word.timeMs) }
            .padding(horizontal = 2.dp, vertical = 2.dp),
    ) {
        Text(
            text = word.text,
            color = if (current) CreamMuted.copy(alpha = 0.45f) else restColor,
            fontWeight = if (current || sung) FontWeight.Bold else FontWeight.Medium,
            style = style,
            textAlign = TextAlign.Start,
        )
        if (current) {
            Text(
                text = word.text,
                color = Gold,
                fontWeight = FontWeight.ExtraBold,
                style = style,
                textAlign = TextAlign.Start,
                modifier = Modifier.drawWithContent {
                    clipRect(right = size.width * progress) {
                        this@drawWithContent.drawContent()
                    }
                },
            )
        }
    }
}

@Composable
fun AnalysisPanel(
    analysis: AudioAnalysis?,
    modifier: Modifier = Modifier,
    onAnalyzeClick: (() -> Unit)? = null,
    scrollable: Boolean = true,
    /** false para streams de red (YouTube Music): no hay PCM local que analizar. */
    canAnalyze: Boolean = true,
) {
    if (analysis?.isAnalyzing == true) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Gold)
                Spacer(Modifier.height(12.dp))
                Text("Decodificando PCM y analizando espectro FFT…", color = CreamMuted)
            }
        }
        return
    }

    val hasSpectrum = analysis?.spectrumDb?.isNotEmpty() == true
    val verdictColor = when (analysis?.verdict) {
        AuthenticityVerdict.AUTHENTIC_HIRES, AuthenticityVerdict.AUTHENTIC_CD -> Gold
        AuthenticityVerdict.AUTHENTIC_LOSSY -> Color(0xFF64B5F6)
        AuthenticityVerdict.UPSAMPLED, AuthenticityVerdict.LOSSY_TRANSCODE -> Color(0xFFE07A5F)
        else -> CreamMuted
    }

    @Composable
    fun AnalysisContent() {
        Surface(
            color = InkSoft,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (hasSpectrum) verdictColor else Gold, RoundedCornerShape(5.dp)),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (hasSpectrum) analysis?.verdictTitle.orEmpty() else "Verificación de Autenticidad Hi-Res",
                            color = if (hasSpectrum) verdictColor else Gold,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    when {
                        !canAnalyze -> "Este audio llega por streaming desde YouTube Music, así que no hay muestras PCM locales para analizar. El análisis espectral y de autenticidad funciona con archivos del dispositivo (FLAC, WAV, MP3, etc.)."
                        hasSpectrum -> analysis?.verdictDetail.orEmpty()
                        else -> "Analiza las muestras PCM y el espectro de frecuencias FFT para verificar si el archivo es 100% auténtico, si fue upsampleado o si es un transcode de MP3/AAC."
                    },
                    color = Cream,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (canAnalyze) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onAnalyzeClick?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (hasSpectrum) "Re-analizar Espectro y Autenticidad" else "🔍 Verificar Autenticidad y Espectro",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        if (hasSpectrum && analysis != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Espectro de Frecuencia (FFT 4096)", color = Gold, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Corte: ${analysis.cutoffHz} Hz",
                        color = Color(0xFFE07A5F),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                SpectrumChart(
                    magnitudes = analysis.spectrumDb,
                    frequencies = analysis.spectrumHz,
                    cutoffHz = analysis.cutoffHz.toFloat(),
                    nyquistHz = analysis.nyquistHz.toFloat(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Pico: ${"%.1f".format(analysis.peakDb)} dBFS", color = CreamMuted, style = MaterialTheme.typography.labelSmall)
                    Text("RMS: ${"%.1f".format(analysis.rmsDb)} dBFS", color = CreamMuted, style = MaterialTheme.typography.labelSmall)
                    Text("DR: ${"%.1f".format(analysis.dynamicRangeDb)} dB", color = Gold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Parámetros Técnicos y Metadatos Extraídos", color = Gold, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

        val technicalList = analysis?.technical?.ifEmpty { analysis.tags }.orEmpty()
        technicalList.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InkSoft.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(label, color = CreamMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.42f))
                Text(value, color = Cream, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.58f))
            }
        }
    }

    if (scrollable) {
        LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { AnalysisContent() }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalysisContent()
        }
    }
}

@Composable
fun SpectrumChart(
    magnitudes: List<Float>,
    frequencies: List<Float>,
    cutoffHz: Float,
    nyquistHz: Float,
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Ink, RoundedCornerShape(12.dp))
            .padding(6.dp),
    ) {
        if (magnitudes.isEmpty()) return@Canvas
        val minDb = -90f
        val maxDb = magnitudes.max().coerceAtLeast(-10f)
        val barWidth = size.width / magnitudes.size

        magnitudes.forEachIndexed { index, db ->
            val h = ((db - minDb) / (maxDb - minDb)).coerceIn(0f, 1f) * size.height
            val color = if (frequencies.getOrNull(index)?.let { it <= cutoffHz } == true) {
                Gold.copy(alpha = 0.85f)
            } else {
                CreamMuted.copy(alpha = 0.35f)
            }
            drawRect(
                color = color,
                topLeft = Offset(index * barWidth, size.height - h),
                size = Size(barWidth * 0.85f, h),
            )
        }

        fun xForHz(hz: Float): Float {
            if (nyquistHz <= 0f) return 0f
            return (hz / nyquistHz).coerceIn(0f, 1f) * size.width
        }

        if (cutoffHz > 0f) {
            val x = xForHz(cutoffHz)
            drawLine(Color(0xFFE07A5F), Offset(x, 0f), Offset(x, size.height), strokeWidth = 3f)
        }

        if (nyquistHz > 20000f) {
            val x = xForHz(20000f)
            drawLine(CreamMuted.copy(alpha = 0.5f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5f)
        }
    }
}
