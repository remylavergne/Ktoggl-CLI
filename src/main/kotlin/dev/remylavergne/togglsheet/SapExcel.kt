package dev.remylavergne.togglsheet

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.time.LocalDate


data class SapExcel(
    private val data: List<SapExcelData>,
) {

    private val workbook: Workbook = XSSFWorkbook()
    private val sheet: Sheet = workbook.createSheet("SAP")
    private val columns: List<String> = listOf(
        "Date",
        "Project",
        "Project Description",
        "Task",
        "Task Description",
        "Customer Name",
        "H.",
        "Sp.",
        "Ticket",
        "Comment"
    )

    fun generate() {
        createHeader()
        createBody()
        createFile()
    }

    private fun createBody() {
        var rowNum = 1

        for (task in data) {
            val row: Row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(task.date)
            row.createCell(1).setCellValue(task.project)
            row.createCell(2).setCellValue(task.projectDescription)
            row.createCell(3).setCellValue(task.task)
            row.createCell(4).setCellValue(task.taskDescription)
            row.createCell(5).setCellValue(task.customerName)
            row.createCell(6).setCellValue(task.supplyHours)
            row.createCell(7).setCellValue(task.sp)
            row.createCell(8).setCellValue(task.ticket)
            row.createCell(9).setCellValue(task.comment)
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

        val headerRow = sheet.createRow(0)

        for (i in columns.indices) {
            val cell = headerRow.createCell(i)
            cell.setCellValue(columns[i])
            cell.cellStyle = headerCellStyle
        }
    }

    private fun createFile(name: String = "${System.currentTimeMillis()}_${data.first().date}_${data.last().date}.xlsx") {
        val fileOut = FileOutputStream(name)
        workbook.write(fileOut)
        fileOut.close()
    }
}

interface SapExcelData {
    val date: LocalDate
    val project: String
    val projectDescription: String
    val task: String
    val taskDescription: String
    val customerName: String
    val supplyHours: Double
    val sp: Boolean
    val ticket: String
    val comment: String
}

data class SapExcelDataImpl(
    override val date: LocalDate,
    override val project: String,
    override val projectDescription: String,
    override val task: String,
    override val taskDescription: String,
    override val customerName: String,
    override val supplyHours: Double,
    override val ticket: String,
    override val comment: String,
    override val sp: Boolean = false,
) : SapExcelData
