package dev.remylavergne.togglsheet

import com.github.ajalt.clikt.core.NoOpCliktCommand

class KtogglCli : NoOpCliktCommand(
    help = "A Kotlin CLI to interact with Toggl API.",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true,
    name = "ktoggl-cli"
)