package dev.remylavergne.togglsheet.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import dev.remylavergne.ktoggl.USER_AGENT
import dev.remylavergne.ktoggl.report.KtogglReportApi
import dev.remylavergne.ktoggl.report.models.BaseDetails
import dev.remylavergne.ktoggl.report.models.TimeEntry
import dev.remylavergne.ktoggl.report.service.ApiResult
import dev.remylavergne.togglsheet.SapExcel
import dev.remylavergne.togglsheet.iso8601ToSimpleDate
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
    private val groupProjectByDay: Boolean by option(
        "-g",
        "--group",
        help = "Groupe les mêmes projets sur la même journée"
    ).flag("--no-group")


    override fun run() {

        if (apiKey.isEmpty() || workspaceId.isEmpty()) {
            throw Exception("Manadatory informations are missing")
        }

        val ktogglReportApi = KtogglReportApi {
            account {
                apiToken(apiKey)
            }
        }

        val apiResult: ApiResult<BaseDetails> = runBlocking {
            return@runBlocking ktogglReportApi.detailsWithoutPaging {
                userAgent(USER_AGENT) // TODO: Make optional
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
                val excelData: List<SapExcelData> = formatData(apiResult.data)
                generateExcelFile(excelData)
                displayReport(apiResult.data)
            }
            is ApiResult.Error -> throw Exception("Unable to retrieve informations from Toggl API...")
        }
    }

    /**
     * Génère les données pour remplir le fichier Excel
     */
    private fun formatData(baseDetails: BaseDetails): List<SapExcelData> {
        return if (groupProjectByDay) {
            baseDetails.toSapExcelGroupedData()
        } else {
            baseDetails.toSapExcelData()
        }

    }

    private fun generateExcelFile(data: List<SapExcelData>) {

        SapExcel(data).generate()

        TermUi.echo("\n-> (${data.last().date.iso8601ToSimpleDate()} to ${data.first().date.iso8601ToSimpleDate()}) Excel generation done with ${data.count()} entries")
    }

    /**
     * Affiche dans la console, un résumé de la génération avec des informations sur les différences de temps par jour
     */
    private fun displayReport(data: BaseDetails) {
        val totalHours = data.totalGrand?.toHours() ?: 0
        val entriesPerDay: Map<String, List<TimeEntry>> = data.data.groupBy { timeEntry: TimeEntry ->
            timeEntry.start.iso8601ToSimpleDate()
        }.filter { map -> map.value.isNotEmpty() }

        TermUi.echo("\n-> Summary")
        TermUi.echo("${entriesPerDay.size} days exported") // TODO: Calcul faux car compte les week-end // Sortir les jours à 0h / tasks ?
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
 * Transform a [BaseDetails] API response to an [SapExcelData] list
 * This list is used to generate SAP timesheets Excel file
 */
fun BaseDetails.toSapExcelData(): List<SapExcelData> {
    return this.data.map {

        val projectData = ProjectData(it.project ?: "")

        SapExcelData(
            date = it.start.iso8601ToSimpleDate(),
            project = projectData.getProjectId(),
            projectDescription = projectData.getProjectDescription(),
            task = projectData.getTaskId(),
            taskDescription = projectData.getTaskDescription(),
            customerName = it.client ?: "",
            hours = it.dur.millisToSapHours(),
            sp = false.toCheckbox(),
            ticket = "",
            comment = it.description,
        )
    }
}

/**
 * Transform a [BaseDetails] API response to an [SapExcelData] list
 * Entries
 * This list is used to generate SAP timesheets Excel file
 */
fun BaseDetails.toSapExcelGroupedData(): List<SapExcelData> {

    val groupByDayAndEntries: Collection<List<TimeEntry>> = this.data.groupByDayWithEntries().values

    return groupByDayAndEntries.map { dayEntries: List<TimeEntry> ->

        val groupByDailyProject = dayEntries.groupBy { it.pid }.values

        groupByDailyProject.map { projectTimeEntries: List<TimeEntry> ->
            val projectHoursSum: Long = projectTimeEntries.sumOf { it.dur }

            val projectData = ProjectData(projectTimeEntries.first().project ?: "")

            SapExcelData(
                date = projectTimeEntries.first().start.iso8601ToSimpleDate(),
                project = projectData.getProjectId(),
                projectDescription = projectData.getProjectDescription(),
                task = projectData.getTaskId(),
                taskDescription = projectData.getTaskDescription(),
                customerName = projectTimeEntries.first().client ?: "",
                hours = projectHoursSum.millisToSapHours(),
                sp = false.toCheckbox(),
                ticket = "",
                comment = projectTimeEntries.first().description,
            )
        }
    }.flatten()
}


/**
 * Group all entries by day.
 */
fun List<TimeEntry>.groupByDay(): Map<String, List<TimeEntry>> {
    return this.groupBy { timeEntry: TimeEntry ->
        timeEntry.start.iso8601ToSimpleDate()
    }
}

/**
 * Group all entries by day, with only days with entries. Empty days are ignored.
 */
fun List<TimeEntry>.groupByDayWithEntries(): Map<String, List<TimeEntry>> {
    return this.groupBy { timeEntry: TimeEntry ->
        timeEntry.start.iso8601ToSimpleDate()
    }.filter { map -> map.value.isNotEmpty() }
}

fun Boolean.toCheckbox(): String {
    return if (this) {
        "X"
    } else {
        ""
    }
}