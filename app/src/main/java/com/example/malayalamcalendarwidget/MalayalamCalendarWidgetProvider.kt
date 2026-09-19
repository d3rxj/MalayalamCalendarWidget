package com.example.malayalamcalendarwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.Calendar

/**
 * Widget shows the Malayalam month name, day-of-month, and Kollavarsham (Malayalam era) year.
 * No Gregorian date, no day-of-week, no Malayalam numerals - all per spec.
 * Tapping the widget does nothing (no click listener is attached).
 */
class MalayalamCalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
        scheduleNextMidnightRefresh(context)
    }

    override fun onEnabled(context: Context) {
        scheduleNextMidnightRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_MIDNIGHT_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, MalayalamCalendarWidgetProvider::class.java)
            )
            for (id in ids) {
                updateWidget(context, manager, id)
            }
            scheduleNextMidnightRefresh(context)
        }
    }

    companion object {
        private const val ACTION_MIDNIGHT_REFRESH =
            "com.example.malayalamcalendarwidget.ACTION_MIDNIGHT_REFRESH"

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val date = MalayalamCalendarCalculator.getMalayalamDate()
            val views = RemoteViews(context.packageName, R.layout.widget_malayalam_calendar)
            views.setTextViewText(R.id.tv_month_day, "${date.monthName} ${date.day}")
            views.setTextViewText(R.id.tv_year, date.kollavarshamYear.toString())
            manager.updateAppWidget(widgetId, views)
        }

        /**
         * android:updatePeriodMillis is throttled by the OS and isn't reliable for hitting
         * the exact midnight rollover, so we also schedule a one-shot alarm for the next
         * midnight to force a refresh right when the date changes.
         */
        private fun scheduleNextMidnightRefresh(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, MalayalamCalendarWidgetProvider::class.java).apply {
                action = ACTION_MIDNIGHT_REFRESH
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextMidnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 5)
                set(Calendar.MILLISECOND, 0)
            }

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC,
                nextMidnight.timeInMillis,
                pendingIntent
            )
        }
    }
}
