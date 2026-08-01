package com.quickadd.vault

import android.content.Context
import android.net.Uri

object VaultPrefs {
    private const val PREFS = "vault_quick_add_prefs"
    private const val KEY_VAULT_URI = "vault_uri"
    private const val KEY_FOLDER_PREFIX = "widget_folder_"

    fun setVaultUri(context: Context, uri: Uri) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_VAULT_URI, uri.toString())
            .apply()
    }

    fun getVaultUri(context: Context): Uri? {
        val s = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_VAULT_URI, null) ?: return null
        return Uri.parse(s)
    }

    fun setWidgetFolder(context: Context, widgetId: Int, folder: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FOLDER_PREFIX + widgetId, folder)
            .apply()
    }

    fun getWidgetFolder(context: Context, widgetId: Int): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_FOLDER_PREFIX + widgetId, "") ?: ""
    }

    fun removeWidgetFolder(context: Context, widgetId: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_FOLDER_PREFIX + widgetId)
            .apply()
    }
}
