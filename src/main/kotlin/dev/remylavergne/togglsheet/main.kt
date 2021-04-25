package dev.remylavergne.togglsheet

import com.github.ajalt.clikt.core.subcommands
import dev.remylavergne.togglsheet.subcommand.Outlook
import dev.remylavergne.togglsheet.subcommand.SAP


fun main(args: Array<String>) =
    TogglSheet().subcommands(SAP(), Outlook()).main(args)
