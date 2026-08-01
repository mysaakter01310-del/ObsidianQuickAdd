package com.quickadd.vault

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class VaultWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            VaultPrefs.removeWidgetFolder(context, widgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val folder = VaultPrefs.getWidgetFolder(context, widgetId)
            val label = if (folder.isBlank()) "Vault Inbox" else folder

            val views = RemoteViews(context.packageName, R.layout.widget_vault)
            views.setTextViewText(R.id.widgetLabel, label)

            val intent = Intent(context, CaptureActivity::class.java).apply {
                putExtra(CaptureActivity.EXTRA_FOLDER, folder)
                putExtra(CaptureActivity.EXTRA_WIDGET_ID, widgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
