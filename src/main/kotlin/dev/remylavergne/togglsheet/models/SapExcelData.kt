package dev.remylavergne.togglsheet.models


data class SapExcelData(
    val date: String,
    val project: String,
    val projectDescription: String,
    val task: String,
    val taskDescription: String,
    val customerName: String,
    val hours: Double,
    val ticket: String,
    val comment: String,
    val sp: String = "",
)