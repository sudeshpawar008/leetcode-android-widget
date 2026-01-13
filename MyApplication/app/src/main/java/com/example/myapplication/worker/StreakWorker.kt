package com.example.myapplication.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.R
import com.example.myapplication.widget.LeetCodeWidgetProvider

data class LeetCodeStats(
    val streak: Int,
    val solved: Int,
    val total: Int,
    val rank: Int
)

class StreakWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val stats = fetchStatsSafe("sudeshpawar008")

            val prefs = applicationContext.getSharedPreferences(
                "leetcode_widget",
                Context.MODE_PRIVATE
            )

            prefs.edit()
                .putInt("streak", stats.streak)
                .putInt("solved", stats.solved)
                .putInt("total", stats.total)
                .putInt("rank", stats.rank)
                .apply()

            updateAllWidgets()

            Result.success()
        } catch (e: Exception) {
            Result.success()
        }
    }


    private fun updateAllWidgets() {
        val prefs = applicationContext.getSharedPreferences(
            "leetcode_widget",
            Context.MODE_PRIVATE
        )

        val streak = prefs.getInt("streak", 0)
        val solved = prefs.getInt("solved", 0)
        val total = prefs.getInt("total", 0)
        val rank = prefs.getInt("rank", 0)

        val views = RemoteViews(
            applicationContext.packageName,
            R.layout.widget_leetcode
        )

        views.setTextViewText(
            R.id.lineOneText,
            "$streak | $solved/$total"
        )

        views.setTextViewText(
            R.id.lineTwoText,
            "RANK $rank"
        )


        val manager = AppWidgetManager.getInstance(applicationContext)
        val component = ComponentName(
            applicationContext,
            com.example.myapplication.widget.LeetCodeWidgetProvider::class.java
        )

        manager.updateAppWidget(component, views)
    }

    private fun fetchStatsSafe(username: String): LeetCodeStats {
        val client = okhttp3.OkHttpClient()

        val request = okhttp3.Request.Builder()
            .url("https://leetcode-stats-api.herokuapp.com/$username")
            .get()
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return LeetCodeStats(0, 0, 0, 0)

        val json = org.json.JSONObject(body)

        return LeetCodeStats(
            streak = json.optInt("streak", 0),
            solved = json.optInt("totalSolved", 0),
            total = json.optInt("totalQuestions", 0),
            rank = json.optInt("ranking", 0)
        )
    }

    companion object {
        fun runOnce(context: Context) {
            val request = OneTimeWorkRequestBuilder<StreakWorker>().build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "leetcode_widget_refresh",   // UNIQUE NAME
                    ExistingWorkPolicy.REPLACE,  // cancel old, run new
                    request
                )
        }
    }

}
