package dev.remylavergne.togglsheet.subcommand

import com.github.ajalt.clikt.core.NoOpCliktCommand


class Timesheet : NoOpCliktCommand(
    help = "Generate timesheet for various platforms",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true,
    name = "timesheet"
)