/*
 * Copyright (C) 2025-2026 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.axion.axthemestore.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

@Composable
fun WaveformSeekBarPreview(
    packageName: String,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val variant = remember(packageName) {
        when {
            packageName.contains("heartbeat") -> WaveformVariant.Heartbeat
            packageName.contains("ocean") -> WaveformVariant.Ocean
            packageName.contains("pulse") -> WaveformVariant.Pulse
            else -> WaveformVariant.Axion
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 8.dp),
    ) {
        val centerY = size.height / 2f
        val amplitude = size.height * 0.38f
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )

        val path = Path()
        val fillPath = Path().apply { moveTo(0f, centerY) }
        for (i in 0..PREVIEW_SAMPLES) {
            val t = i / PREVIEW_SAMPLES.toFloat()
            val x = t * size.width
            val y = centerY - amplitude * variant.valueAt(t)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            fillPath.lineTo(x, y)
        }
        fillPath.lineTo(size.width, centerY)
        fillPath.close()

        if (variant.hasFill) drawPath(fillPath, fillColor)
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

private const val PREVIEW_SAMPLES = 72

private enum class WaveformVariant(val hasFill: Boolean) {
    Axion(hasFill = true) {
        override fun valueAt(t: Float): Float =
            0.52f * sinWave(t, 1.4f) + 0.24f * sinWave(t, 3.1f, 0.18f)
    },
    Pulse(hasFill = false) {
        override fun valueAt(t: Float): Float =
            0.72f * sinWave(t, 2.7f) * (0.55f + 0.45f * sinWave(t, 0.7f, 0.25f))
    },
    Heartbeat(hasFill = false) {
        override fun valueAt(t: Float): Float {
            val beat = gaussian(t, 0.25f, 0.018f) - gaussian(t, 0.31f, 0.012f) * 0.85f +
                    gaussian(t, 0.36f, 0.028f) * 0.45f + gaussian(t, 0.72f, 0.018f) * 0.65f
            return beat.coerceIn(-0.85f, 0.95f)
        }
    },
    Ocean(hasFill = true) {
        override fun valueAt(t: Float): Float =
            0.42f * sinWave(t, 1.0f, 0.08f) + 0.18f * sinWave(t, 2.0f, 0.36f)
    };

    abstract fun valueAt(t: Float): Float
}

private fun sinWave(t: Float, cycles: Float, phase: Float = 0f): Float =
    sin((t + phase) * cycles * 2f * PI).toFloat()

private fun gaussian(t: Float, center: Float, width: Float): Float =
    exp(-((t - center) * (t - center)) / (2f * width * width)).toFloat()
