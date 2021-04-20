package dev.remylavergne.togglsheet.sucommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import dev.remylavergne.togglsheet.OutputData

class Outlook : CliktCommand(
    help = "Export Toggl timesheet to Outlook Calendar",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true
) {
    private val input by option("-i", "--input").file(mustExist = true, canBeDir = false)
        .help(help = "The Toggl CSV extract for a month")

    override fun run() {
        val outputData = OutputData(input!!)
    }
}