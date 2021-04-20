package dev.remylavergne.togglsheet

import com.github.ajalt.clikt.core.subcommands
import dev.remylavergne.togglsheet.sucommand.Outlook
import dev.remylavergne.togglsheet.sucommand.SAP


fun main(args: Array<String>) =
    TogglSheet().subcommands(SAP(), Outlook()).main(args)
