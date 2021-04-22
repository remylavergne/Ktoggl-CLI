package dev.remylavergne.togglsheet

import dev.remylavergne.togglsheet.models.SapExcelData
import dev.remylavergne.togglsheet.sucommand.toCheckbox
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File

class SapExcelTest : FunSpec({

    test("Create empty structure") {
        val sapExcel = SapExcel(
            data = listOf(
                SapExcelData(
                    date = "2021-04-01",
                    project = "Big Pharma",
                    projectDescription = "A really cool project",
                    task = "",
                    taskDescription = "Cleaning",
                    customerName = "Gilead",
                    hours = 1600456,
                    sp = false.toCheckbox(),
                    ticket = "",
                    comment = "",
                )
            )
        )

        sapExcel.generate()

        val extractFile = File("extract.xlsx")

        extractFile.exists() shouldBe false

        // Clean
        if (extractFile.exists()) {
            extractFile.delete()
        }
    }
})