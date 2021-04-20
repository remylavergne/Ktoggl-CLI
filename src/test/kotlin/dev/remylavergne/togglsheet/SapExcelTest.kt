package dev.remylavergne.togglsheet

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.LocalDate

class SapExcelTest : FunSpec({

    test("Create empty structure") {
        val sapExcel = SapExcel(
            data = listOf(
                SapExcelDataImpl(
                    date = LocalDate.parse("2021-04-01"),
                    project = "Big Pharma",
                    projectDescription = "A really cool project",
                    task = "",
                    taskDescription = "Cleaning",
                    customerName = "Gilead",
                    supplyHours = 4.0,
                    sp = false,
                    ticket = "",
                    comment = "",
                )
            )
        )

        sapExcel.generate()

        val extractFile = File("extract.xlsx")

        extractFile.exists() shouldBe true

        // Clean
        if (extractFile.exists()) {
            extractFile.delete()
        }
    }
})