package com.quickadd.vault

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    private val pickFolder = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri = result.data?.data ?: return@registerForActivityResult
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            VaultPrefs.setVaultUri(this, uri)
            updateStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        findViewById<Button>(R.id.selectVaultButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            pickFolder.launch(intent)
        }

        updateStatus()
    }

    private fun updateStatus() {
        val uri = VaultPrefs.getVaultUri(this)
        statusText.text = if (uri != null) {
            val doc = DocumentFile.fromTreeUri(this, uri)
            "Vault set: ${doc?.name ?: uri}\n\nNow add a widget to your home screen: long-press home screen -> Widgets -> Vault Quick Add. Each widget you add will ask which folder to save into."
        } else {
            "No vault selected yet. Tap the button below and choose your Obsidian vault's root folder."
        }
    }
}
