package com.example.myapplication.widget

import com.example.myapplication.worker.StreakWorker
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.myapplication.R

class LeetCodeWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.example.myapplication.REFRESH_WIDGET"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences(
            "leetcode_widget",
            Context.MODE_PRIVATE
        )

        val streak = prefs.getInt("streak", 0)
        val solved = prefs.getInt("solved", 0)
        val total = prefs.getInt("total", 0)
        val rank = prefs.getInt("rank", 0)

        for (id in appWidgetIds) {

            val intent = Intent(context, LeetCodeWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(
                context.packageName,
                R.layout.widget_leetcode
            )

            // ✅ MATRIX STYLE (MATCHES WORKER)
            views.setTextViewText(
                R.id.lineOneText,
                "${streak} DAYS  |  ${solved}/${total}"
            )

            views.setTextViewText(
                R.id.lineTwoText,
                if (rank > 0) rank.toString() else "—"
            )

            views.setOnClickPendingIntent(
                R.id.refreshIcon,
                pendingIntent
            )

            appWidgetManager.updateAppWidget(id, views)
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_REFRESH) {
            StreakWorker.runOnce(context)
        }
    }

}
