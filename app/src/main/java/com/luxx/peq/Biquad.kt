package com.luxx.peq

import kotlin.math.*

enum class FilterType { PEAK, LOW_SHELF, HIGH_SHELF, NOTCH }

data class EqBand(
    val frequency: Float,
    val gain: Float = 0f,
    val q: Float = 1f,
    val type: FilterType = FilterType.PEAK,
    val enabled: Boolean = true
)

object Biquad {
    fun magnitudeDb(band: EqBand, frequency: Double, sampleRate: Double = 48000.0): Double {
        if (!band.enabled || frequency <= 0.0) return 0.0
        val f0 = band.frequency.toDouble().coerceIn(20.0, sampleRate / 2.0 - 1.0)
        val q = band.q.toDouble().coerceIn(0.1, 10.0)
        val a = 10.0.pow(band.gain.toDouble() / 40.0)
        val w0 = 2.0 * PI * f0 / sampleRate
        val c = cos(w0)
        val s = sin(w0)
        val alpha = s / (2.0 * q)
        val (b0, b1, b2, a0, a1, a2) = when (band.type) {
            FilterType.PEAK -> listOf(
                1 + alpha * a, -2 * c, 1 - alpha * a,
                1 + alpha / a, -2 * c, 1 - alpha / a
            )
            FilterType.NOTCH -> listOf(
                1.0, -2 * c, 1.0,
                1 + alpha, -2 * c, 1 - alpha
            )
            FilterType.LOW_SHELF, FilterType.HIGH_SHELF -> {
                val slope = q.coerceIn(0.1, 1.0)
                val shelfAlpha = s / 2.0 * sqrt((a + 1 / a) * (1 / slope - 1) + 2)
                val beta = 2 * sqrt(a) * shelfAlpha
                if (band.type == FilterType.LOW_SHELF) listOf(
                    a * ((a + 1) - (a - 1) * c + beta),
                    2 * a * ((a - 1) - (a + 1) * c),
                    a * ((a + 1) - (a - 1) * c - beta),
                    (a + 1) + (a - 1) * c + beta,
                    -2 * ((a - 1) + (a + 1) * c),
                    (a + 1) + (a - 1) * c - beta
                ) else listOf(
                    a * ((a + 1) + (a - 1) * c + beta),
                    -2 * a * ((a - 1) + (a + 1) * c),
                    a * ((a + 1) + (a - 1) * c - beta),
                    (a + 1) - (a - 1) * c + beta,
                    2 * ((a - 1) - (a + 1) * c),
                    (a + 1) - (a - 1) * c - beta
                )
            }
        }
        val w = 2.0 * PI * frequency / sampleRate
        val z1r = cos(w); val z1i = -sin(w)
        val z2r = cos(2 * w); val z2i = -sin(2 * w)
        val nr = b0 + b1 * z1r + b2 * z2r
        val ni = b1 * z1i + b2 * z2i
        val dr = a0 + a1 * z1r + a2 * z2r
        val di = a1 * z1i + a2 * z2i
        val numerator = hypot(nr, ni)
        val denominator = hypot(dr, di).coerceAtLeast(1e-12)
        return 20.0 * log10((numerator / denominator).coerceAtLeast(1e-12))
    }

    fun combinedDb(bands: List<EqBand>, frequency: Double): Double =
        bands.sumOf { magnitudeDb(it, frequency) }
}