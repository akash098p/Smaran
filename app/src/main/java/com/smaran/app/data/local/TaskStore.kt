package com.smaran.app.data.local

import android.content.Context
import com.smaran.app.data.model.Priority
import com.smaran.app.data.model.Task
import com.smaran.app.data.model.HistoryAction
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskStore(context: Context) {
    private val prefs = context.getSharedPreferences("smaran_tasks", Context.MODE_PRIVATE)
    private val history = HistoryStore(context)

    fun getTasks(): List<Task> {
        val raw = prefs.getString(KEY_TASKS, "[]") ?: "[]"
        val array = JSONArray(raw)
        val tasks = buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(Task(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    description = o.optString("description"),
                    date = LocalDate.parse(o.getString("date")),
                    time = LocalTime.parse(o.getString("time")),
                    category = o.optString("category", "Personal"),
                    priority = runCatching { Priority.valueOf(o.optString("priority", "MEDIUM")) }.getOrDefault(Priority.MEDIUM),
                    completed = o.optBoolean("completed", false),
                    recurring = o.optBoolean("recurring", false)
                ))
            }
        }.sortedBy { it.date.atTime(it.time) }

        val now = LocalDateTime.now()
        tasks.filter { !it.completed && it.date.atTime(it.time).isBefore(now) }.forEach { task ->
            val since = task.date.atTime(task.time).minusHours(24)
            if (!history.hasActionSince(task.id, HistoryAction.MISSED, since)) history.record(task.id, HistoryAction.MISSED)
        }
        return tasks
    }

    fun add(task: Task) {
        val tasks = getTasks().toMutableList()
        tasks.removeAll { it.id == task.id }
        tasks.add(task)
        save(tasks)
    }

    fun update(task: Task) = add(task)

    fun delete(id: Long) { save(getTasks().filterNot { it.id == id }) }

    private fun save(tasks: List<Task>) {
        val array = JSONArray()
        tasks.forEach { task -> array.put(JSONObject().apply {
            put("id", task.id)
            put("title", task.title)
            put("description", task.description)
            put("date", task.date.toString())
            put("time", task.time.toString())
            put("category", task.category)
            put("priority", task.priority.name)
            put("completed", task.completed)
            put("recurring", task.recurring)
        }) }
        prefs.edit().putString(KEY_TASKS, array.toString()).apply()
    }

    companion object { private const val KEY_TASKS = "tasks" }
}
