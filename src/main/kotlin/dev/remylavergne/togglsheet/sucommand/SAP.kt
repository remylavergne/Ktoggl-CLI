package dev.remylavergne.togglsheet.sucommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import dev.remylavergne.ktoggl.API_KEY
import dev.remylavergne.ktoggl.USER_AGENT
import dev.remylavergne.ktoggl.WORKSPACE_ID
import dev.remylavergne.ktoggl.report.KtogglReportApi
import dev.remylavergne.ktoggl.report.models.BaseDetailed
import dev.remylavergne.ktoggl.report.service.ApiResult
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class SAP : CliktCommand(
    help = "Generate an .xlsx with your timesheet to import into SAP",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true
) {
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


    override fun run() {
        // Call API pour récupérer le mois en question
        val ktogglReportApi = KtogglReportApi {
            account {
                apiToken(API_KEY)
            }
        }

        val apiResult: ApiResult<BaseDetailed> = runBlocking {
            return@runBlocking ktogglReportApi.detailsWithoutPaging {
                userAgent(USER_AGENT)
                workspaceId(WORKSPACE_ID)

                if (since.isNotEmpty()) {
                    since(LocalDate.parse(since))
                }

                if (until.isNotEmpty()) {
                    until(LocalDate.parse(since))
                }
            }
        }

        when (apiResult) {
            is ApiResult.Success -> generateExcelFile(apiResult.data)
            is ApiResult.Error -> throw Exception("Unable to retrieve informations from Toggl API...")
        }


        // val outputData = OutputData(input!!)

    }

    private fun generateExcelFile(data: BaseDetailed) {
        TermUi.echo("Nombre de tâches extraites : ${data.data.count()}")
    }
}