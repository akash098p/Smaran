package com.smaran.app.data.local

import android.content.Context
import com.smaran.app.data.model.HistoryAction
import com.smaran.app.data.model.TaskHistory
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime

class HistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("smaran_history", Context.MODE_PRIVATE)

    fun getAll(): List<TaskHistory> {
        val array = JSONArray(prefs.getString(KEY_HISTORY, "[]") ?: "[]")
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(TaskHistory(
                    id = o.getLong("id"),
                    taskId = o.getLong("taskId"),
                    action = runCatching { HistoryAction.valueOf(o.getString("action")) }.getOrDefault(HistoryAction.CREATED),
                    timestamp = LocalDateTime.parse(o.getString("timestamp")),
                    previousDateTime = o.optString("previousDateTime").takeIf { it.isNotBlank() }?.let { LocalDateTime.parse(it) },
                    newDateTime = o.optString("newDateTime").takeIf { it.isNotBlank() }?.let { LocalDateTime.parse(it) }
                ))
            }
        }.sortedByDescending { it.timestamp }
    }

    fun add(event: TaskHistory) {
        val events = getAll().toMutableList()
        events.removeAll { it.id == event.id }
        events.add(event)
        save(events)
    }

    fun record(taskId: Long, action: HistoryAction, previous: LocalDateTime? = null, next: LocalDateTime? = null) {
        add(TaskHistory(System.currentTimeMillis(), taskId, action, LocalDateTime.now(), previous, next))
    }

    fun hasActionSince(taskId: Long, action: HistoryAction, since: LocalDateTime): Boolean =
        getAll().any { it.taskId == taskId && it.action == action && it.timestamp.isAfter(since) }

    private fun save(events: List<TaskHistory>) {
        val array = JSONArray()
        events.forEach { e ->
            array.put(JSONObject().apply {
                put("id", e.id)
                put("taskId", e.taskId)
                put("action", e.action.name)
                put("timestamp", e.timestamp.toString())
                put("previousDateTime", e.previousDateTime?.toString() ?: "")
                put("newDateTime", e.newDateTime?.toString() ?: "")
            })
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    companion object { private const val KEY_HISTORY = "history" }
}
