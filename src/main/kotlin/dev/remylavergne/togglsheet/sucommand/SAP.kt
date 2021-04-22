package dev.remylavergne.togglsheet.sucommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import dev.remylavergne.ktoggl.USER_AGENT
import dev.remylavergne.ktoggl.report.KtogglReportApi
import dev.remylavergne.ktoggl.report.models.BaseDetailed
import dev.remylavergne.ktoggl.report.models.TimeEntry
import dev.remylavergne.ktoggl.report.service.ApiResult
import dev.remylavergne.togglsheet.SapExcel
import dev.remylavergne.togglsheet.iso8601ToYMD
import dev.remylavergne.togglsheet.millisToSapHours
import dev.remylavergne.togglsheet.models.ProjectData
import dev.remylavergne.togglsheet.models.SapExcelData
import dev.remylavergne.togglsheet.toHours
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class SAP : CliktCommand(
    help = "Generate an .xlsx with your timesheet for SAP",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true
) {
    private val apiKey: String by option(
        "-a",
        "--api-key",
        help = "API Toggl key"
    ).default("")
    private val workspaceId: String by option(
        "-w",
        "--workspace",
        help = "The workspace ID targeted"
    ).default("")
    private val since: String by option(
        "-s",
        "--since",
        help = "Date de début de l'extract des données (ex: 2021-03-01). Par défaut, les données de la dernière semaine sont renvoyées."
    ).default("")
    private val until: String by option(
        "-u",
        "--until",
        help = "Date de fin de l'extract des données (ex: 2021-03-01)."
    ).default("")
    private val hoursPerDay: Int by option(
        "-h",
        "--hours",
        help = "Nombre d'heure à prester par jour."
    ).int().default(8)


    override fun run() {

        if (apiKey.isEmpty() || workspaceId.isEmpty()) {
            throw Exception("Manadatory informations are missing")
        }

        val ktogglReportApi = KtogglReportApi {
            account {
                apiToken(apiKey)
            }
        }

        val apiResult: ApiResult<BaseDetailed> = runBlocking {
            return@runBlocking ktogglReportApi.detailsWithoutPaging {
                userAgent(USER_AGENT)
                workspaceId(workspaceId)

                if (since.isNotEmpty()) {
                    since(LocalDate.parse(since))
                }

                if (until.isNotEmpty()) {
                    until(LocalDate.parse(since))
                }
            }
        }

        when (apiResult) {
            is ApiResult.Success -> {
                generateExcelFile(apiResult.data)
                displayReport(apiResult.data)
            }
            is ApiResult.Error -> throw Exception("Unable to retrieve informations from Toggl API...")
        }
    }

    private fun generateExcelFile(data: BaseDetailed) {
        val toSapExcelData = data.toSapExcelData()

        SapExcel(toSapExcelData).generate()

        TermUi.echo("\n-> (${toSapExcelData.last().date.iso8601ToYMD()} to ${toSapExcelData.first().date.iso8601ToYMD()}) Excel generation done with ${data.data.count()} entries")
    }

    private fun displayReport(data: BaseDetailed) {
        val totalHours = data.totalGrand?.toHours() ?: 0
        val entriesPerDay = data.data.groupBy { timeEntry: TimeEntry ->
            timeEntry.start.iso8601ToYMD()
        }

        TermUi.echo("\n-> Summary")
        TermUi.echo("${entriesPerDay.size} days exported")
        TermUi.echo("Total: $totalHours hours")
        TermUi.echo("Total expected: ${hoursPerDay * entriesPerDay.size} hours\n")


        TermUi.echo("-> Differences based on $hoursPerDay hours shift / day:")
        entriesPerDay.forEach { (date: String, entries: List<TimeEntry>) ->
            val totalHoursDone: Long = entries.map { timeEntry: TimeEntry ->
                timeEntry.dur
            }.sum()

            val hoursRounded: Double = totalHoursDone.millisToSapHours()

            val diffHours = hoursPerDay - hoursRounded

            when {
                diffHours < 0.0 -> TermUi.echo("- $date: + ${diffHours * -1}")
                diffHours > 0.0 -> TermUi.echo("- $date: - $diffHours")
                else -> TermUi.echo("- $date: $diffHours")
            }
        }
    }
}

/**
 * Transform a [BaseDetailed] API response to an [SapExcelData] list
 * This list is used to generate SAP timesheets Excel file
 */
fun BaseDetailed.toSapExcelData(): List<SapExcelData> {

    return this.data.map {

        val projectData = ProjectData(it.project)

        SapExcelData(
            date = it.start.iso8601ToYMD(),
            project = projectData.getProjectId(),
            projectDescription = projectData.getProjectDescription(),
            task = projectData.getTaskId(),
            taskDescription = projectData.getTaskDescription(),
            customerName = it.client ?: "",
            hours = it.dur,
            sp = false.toCheckbox(),
            ticket = "",
            comment = it.description,
        )
    }
}

fun Boolean.toCheckbox(): String {
    return if (this) {
        "X"
    } else {
        ""
    }
}