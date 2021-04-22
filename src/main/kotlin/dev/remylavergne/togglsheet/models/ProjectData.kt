package dev.remylavergne.togglsheet.models


data class ProjectData(
    val infos: String,
) {
    private val regex = Regex("^(\\d{8})(.+)(\\d{6})(.+)\$")

    fun getProjectId(): String {
        return regex.find(infos)?.groupValues?.get(1)?.trim() ?: "No project id found"
    }

    fun getProjectDescription(): String {
        return regex.find(infos)?.groupValues?.get(2)?.trim() ?: "No project description found"
    }

    fun getTaskId(): String {
        return regex.find(infos)?.groupValues?.get(3)?.trim() ?: "No task id found"
    }

    fun getTaskDescription(): String {
        return regex.find(infos)?.groupValues?.get(4)?.trim() ?: "No task description found"
    }
}