package dev.remylavergne.togglsheet

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeExactly
import kotlin.math.ceil

class Conversions : FunSpec({

    test("ms to hours rounded") {
        val hourMs = 3_600_000.0
        val dur = 6_045_000

        val estimateHour: Double = dur / hourMs

        println("estimateHour -> $estimateHour")

        val d = ceil(estimateHour * 4) / 4

        d shouldBeExactly 1.75
    }

    test("ms to hours rounded -") {
        val hourMs = 3_600_000.0
        val dur = 5_472_000 // 1.52

        val estimateHour: Double = dur / hourMs

        println("estimateHour -> $estimateHour")

        val d = ceil(estimateHour * 4) / 4

        d shouldBeExactly 1.75

    }
})