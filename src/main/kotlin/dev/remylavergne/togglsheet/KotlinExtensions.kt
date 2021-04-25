package dev.remylavergne.togglsheet

import kotlin.math.ceil
import kotlin.math.floor


fun Long.toHours(): Double {
    val hourMs = 3_600_000.0
    return "%.2f".format(this / hourMs).toDouble()
}

/**
 * Return hours rounded by quarters
 */
fun Long.millisToSapHours(): Double {
    val hourMs = 3_600_000.0

    val estimateHour: Double = "%.2f".format(this / hourMs).toDouble()

    val decimal: Double = "%.2f".format(estimateHour - estimateHour.toInt()).toDouble()

    return when (decimal) {
        in 0.0..0.12 -> floor(estimateHour * 4) / 4
        in 0.13..0.25 -> ceil(estimateHour * 4) / 4
        in 0.26..0.37 -> floor(estimateHour * 4) / 4
        in 0.38..0.50 -> ceil(estimateHour * 4) / 4
        in 0.51..0.67 -> floor(estimateHour * 4) / 4
        in 0.68..0.75 -> ceil(estimateHour * 4) / 4
        in 0.76..0.87 -> floor(estimateHour * 4) / 4
        in 0.88..1.00 -> ceil(estimateHour * 4) / 4
        else -> estimateHour
    }
}

fun String.iso8601ToSimpleDate(): String {
    return this.split("T").first()
}