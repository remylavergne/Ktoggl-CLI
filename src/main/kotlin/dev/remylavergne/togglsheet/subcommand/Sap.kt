package dev.remylavergne.togglsheet.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import dev.remylavergne.ktoggl.report.KtogglReportApi
import dev.remylavergne.ktoggl.report.models.BaseDetails
import dev.remylavergne.ktoggl.report.models.TimeEntry
import dev.remylavergne.ktoggl.report.service.ApiResult
import dev.remylavergne.ktoggl.v8.KtogglV8Api
import dev.remylavergne.ktoggl.v8.Tags
import dev.remylavergne.togglsheet.SapExcel
import dev.remylavergne.togglsheet.iso8601ToSimpleDate
import dev.remylavergne.togglsheet.millisToSapHours
import dev.remylavergne.togglsheet.models.ProjectData
import dev.remylavergne.togglsheet.models.SapExcelData
import dev.remylavergne.togglsheet.toHours
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class Sap : CliktCommand(
    help = "Generate an Excel file to import your timesheet easily in SAP CATS.",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true
) {
    private val apiKey: String by option(
        "-a",
        "--api-key",
        help = "Your Toggl API key. Available in your profile parameters"
    ).default("")
    private val workspaceId: String by option(
        "-w",
        "--workspace",
        help = "The workspace whose data you want to access."
    ).default("")
    private val since: String by option(
        "-s",
        "--since",
        help = "(Optional) ISO 8601 date (YYYY-MM-DD) format. Defaults to today - 6 days."
    ).default("")
    private val until: String by option(
        "-u",
        "--until",
        help = "(Optional) ISO 8601 date (YYYY-MM-DD) format. Note: Maximum date span (until - since) is one year. Defaults to today, unless since is in future or more than year ago, in this case until is since + 6 days."
    ).default("")
    private val tags: List<String> by option(
        "-t",
        "--tag",
        help = "(Optional) Tag name, which assigned for the time entry"
    ).multiple()

    private val hoursPerDay: Int by option(
        "-h",
        "--hours",
        help = "(Optional) Hours daily worked (default: 8)"
    ).int().default(8)
    private val groupProjectByDay: Boolean by option(
        "-g",
        "--group-entries",
        help = "Groupe les mêmes projets sur la même journée"
    ).flag("--no-group-entries") // TODO: Make group default

    private val userAgent: String = "ktoggl-cli"


    override fun run() {

        if (apiKey.isEmpty() || workspaceId.isEmpty()) {
            throw Exception("API key, or workspace id, missing")
        }

        val tagIds = mutableListOf<Int>()
        if (tags.isNotEmpty()) {
            val ktogglV8Api = KtogglV8Api {
                account {
                    apiToken(apiKey)
                }
            }

            runBlocking {
                val apiResultTags = ktogglV8Api.workspace(workspaceId).getTags()

                when (apiResultTags) {
                    is ApiResult.Success -> {
                        val tagIdsFound: List<Int> =
                            apiResultTags.data.filter { t: Tags -> tags.any { it == t.name } }.map { it.id }
                        tagIds.addAll(tagIdsFound)
                    }
                    is ApiResult.Error -> throw Exception("Error while retrieving tags data from Toggl API. Please retry, or, contact me at: lavergne.remy@gmail.com")
                }
            }
        }

        val ktogglReportApi = KtogglReportApi {
            account {
                apiToken(apiKey)
            }
        }

        val apiResult: ApiResult<BaseDetails> = runBlocking {
            return@runBlocking ktogglReportApi.detailsWithoutPaging {
                workspaceId(workspaceId)
                userAgent(userAgent)

                if (since.isNotEmpty()) {
                    since(LocalDate.parse(since))
                }

                if (until.isNotEmpty()) {
                    until(LocalDate.parse(until))
                }

                if (tagIds.isNotEmpty()) {
                    tagsIds(tagIds)
                }
            }
        }

        when (apiResult) {
            is ApiResult.Success -> {
                val excelData: List<SapExcelData> = formatData(apiResult.data)
                generateExcelFile(excelData)
                displayReport(apiResult.data)
            }
            is ApiResult.Error -> throw Exception("Error while retrieving data from Toggl API. Please retry, or, contact me at: lavergne.remy@gmail.com")
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
        val entriesPerDay: Map<String, List<TimeEntry>> = data.data.groupByDayWithEntries()
        val totalHours = data.data.map { it.dur }.sum().toHours()

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
                else -> {
                }
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
 * Every duplicate day entries are grouped to a single one (hours and description)
 * This list is used to generate SAP timesheets Excel file
 */
fun BaseDetails.toSapExcelGroupedData(): List<SapExcelData> {

    val groupByDayAndEntries: Collection<List<TimeEntry>> = this.data.groupByDayWithEntries().values

    return groupByDayAndEntries.map { dayEntries: List<TimeEntry> ->

        val groupByDailyProject = dayEntries.groupBy { it.pid }.values

        groupByDailyProject.map { projectTimeEntries: List<TimeEntry> ->
            // Concat informations
            val projectHoursSum: Long = projectTimeEntries.sumOf { it.dur }
            val projectDescriptions = projectTimeEntries.map { it.description }.toSet().joinToString(" ; ")

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
                comment = projectDescriptions,
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