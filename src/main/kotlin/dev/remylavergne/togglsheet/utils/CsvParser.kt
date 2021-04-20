package dev.remylavergne.togglsheet.utils

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import dev.remylavergne.togglsheet.models.TogglCsv
import java.io.File
import java.io.FileReader


object CsvParser {

    fun deserialize(file: File): List<TogglCsv> = readCsvFile(file.path)

    private inline fun <reified T> readCsvFile(fileName: String): List<T> {
        FileReader(fileName).use { reader ->
            return CsvMapper()
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator(';'))
                .readValues<T>(reader)
                .readAll()
                .toList()
        }
    }
}
