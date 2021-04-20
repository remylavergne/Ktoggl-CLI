package dev.remylavergne.togglsheet

import com.github.ajalt.clikt.core.NoOpCliktCommand

class TogglSheet : NoOpCliktCommand(
    help = "An easy CLI to generate your timesheet from Toggl",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true,
    name = "togglsheet"
)