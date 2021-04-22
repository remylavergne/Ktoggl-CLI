package dev.remylavergne.togglsheet

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KotlinExtensionsTest : FunSpec({

    /**
     * SAP Quarter rounding
     */

    test("First quarter -> rounging floor") {

        val h3683000 = 3683000 // 1.0230555555555556

        val sapQuarterRound = h3683000.toLong().millisToSapHours()

        sapQuarterRound shouldBe 1.00
    }

    test("First quarter -> rounging ceil") {

        val h3683000 = 4139999 // 1.15

        val sapQuarterRound = h3683000.toLong().millisToSapHours()

        sapQuarterRound shouldBe 1.25
    }

    test("Second quarter -> rounding floor") {

        val h3683000 = 8424000 // 2.34

        val sapQuarterRound = h3683000.toLong().millisToSapHours()

        sapQuarterRound shouldBe 2.25
    }

    test("Second quarter -> rounding ceil") {

        val h3683000 = 8712000 // 2.42

        val sapQuarterRound = h3683000.toLong().millisToSapHours()

        sapQuarterRound shouldBe 2.50
    }

    test("Third quarter -> rounding floor") {

        val h3683000 = 5868000 // 1.63

        val sapQuarterRound = h3683000.toLong().millisToSapHours()

        sapQuarterRound shouldBe 1.5
    }

    test("Third quarter -> rounding ceil") {

        val h3683000 = 6192000 // 1.72

        val sapQuarterRound = h3683000.toLong().millisToSapHours()

        sapQuarterRound shouldBe 1.75
    }

    test("Fourth quarter -> rounding floor") {

        val h3683000 = 6588000 // 1.83

        val sapQuarterRound = h3683000.toLong().millisToSapHours()

        sapQuarterRound shouldBe 1.75
    }

    test("Fourth quarter -> rounding ceil") {

        val h3683000 = 6912000 // 1.92

        val sapQuarterRound = h3683000.toLong().millisToSapHours()

        sapQuarterRound shouldBe 2.0
    }

    test("Perfect value") {

        val h3683000 = 3_600_000 // 1

        val sapQuarterRound = h3683000.toLong().millisToSapHours()

        sapQuarterRound shouldBe 1.0
    }
})