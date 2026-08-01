package com.quickadd.vault

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WidgetConfigureActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.activity_widget_configure)

        widgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        if (VaultPrefs.getVaultUri(this) == null) {
            Toast.makeText(this, "Open the app first and select your vault folder.", Toast.LENGTH_LONG).show()
        }

        val folderInput = findViewById<EditText>(R.id.folderInput)
        val hint = findViewById<TextView>(R.id.configHint)
        hint.text = "Which folder in your vault should this widget save into? Leave blank for the vault root.\nExample: Anatomy or Inbox/MBBS"

        findViewById<Button>(R.id.saveConfigButton).setOnClickListener {
            val folder = folderInput.text.toString().trim().trim('/')
            VaultPrefs.setWidgetFolder(this, widgetId, folder)

            val appWidgetManager = AppWidgetManager.getInstance(this)
            VaultWidgetProvider.updateWidget(this, appWidgetManager, widgetId)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}
