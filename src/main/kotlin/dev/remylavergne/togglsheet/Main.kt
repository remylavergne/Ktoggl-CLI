package dev.remylavergne.togglsheet

import com.github.ajalt.clikt.core.subcommands
import dev.remylavergne.togglsheet.subcommand.Sap
import dev.remylavergne.togglsheet.subcommand.Timesheet


fun main(args: Array<String>) =
    KtogglCli().subcommands(Timesheet().subcommands(Sap())).main(args)
