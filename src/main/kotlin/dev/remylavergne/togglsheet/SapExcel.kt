package dev.remylavergne.togglsheet

import dev.remylavergne.togglsheet.models.SapExcelData
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream

/**
 * This class is responsible for the final Excel generation file
 */
data class SapExcel(
    private val data: List<SapExcelData>,
) {

    private val workbook: Workbook = XSSFWorkbook()
    private val sheet: Sheet = workbook.createSheet("Data")

    private val columns: Map<String, String> = mapOf(
        "WDATE" to "Date",
        "PRJNO" to "Project",
        "PRJTX" to "Project Description",
        "TSKNO" to "Task",
        "TSKTX" to "Task Description",
        "KUNNM" to "Customer Name",
        "RLTIM" to "H.",
        "SPTIM" to "Sp.",
        "TICKT" to "Ticket",
        "LTEXT" to "Comment",
    )

    fun generate() {
        createHeaderKeys()
        createHeader()
        createBody()
        createFile()
    }

    private fun createHeaderKeys() {
        val headerRow = sheet.createRow(0)

        var columnIndex = 0
        for ((key, _) in columns) {
            val cell = headerRow.createCell(columnIndex)
            cell.setCellValue(key)

            columnIndex++
        }

    }

    private fun createHeader() {
        val headerFont: Font = workbook.createFont().apply {
            bold = true
            fontHeightInPoints = 12.toShort()
            color = IndexedColors.RED.getIndex()
        }

        val headerCellStyle = workbook.createCellStyle().apply {
            setFont(headerFont)
        }

        val headerRow = sheet.createRow(1)

        var columnIndex = 0
        for ((_, columnTitle) in columns) {
            val cell = headerRow.createCell(columnIndex)
            cell.setCellValue(columnTitle)
            cell.cellStyle = headerCellStyle

            columnIndex++
        }
    }

    private fun createBody() {
        var rowNum = 2

        for (task in data) {
            val row: Row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(task.date)
            row.createCell(1).setCellValue(task.project)
            row.createCell(2).setCellValue(task.projectDescription)
            row.createCell(3).setCellValue(task.task)
            row.createCell(4).setCellValue(task.taskDescription)
            row.createCell(5).setCellValue(task.customerName)
            row.createCell(6).setCellValue(task.hours)
            row.createCell(7).setCellValue(task.sp)
            row.createCell(8).setCellValue(task.ticket)
            row.createCell(9).setCellValue(task.comment)
        }

    }

    private fun createFile(name: String = "${System.currentTimeMillis()}_${data.first().date}_${data.last().date}.xlsx") {
        val fileOut = FileOutputStream(name)
        workbook.write(fileOut)
        fileOut.close()
    }
}
