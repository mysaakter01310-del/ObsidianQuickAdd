package com.quickadd.vault

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile

class CaptureActivity : AppCompatActivity() {

    private var pickedImageUri: Uri? = null
    private lateinit var folder: String
    private lateinit var imageStatus: TextView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pickedImageUri = uri
            imageStatus.text = "Image attached"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        folder = intent.getStringExtra(EXTRA_FOLDER) ?: ""

        val titleInput = findViewById<EditText>(R.id.titleInput)
        val bodyInput = findViewById<EditText>(R.id.bodyInput)
        imageStatus = findViewById(R.id.imageStatus)
        val folderLabel = findViewById<TextView>(R.id.folderLabel)
        folderLabel.text = if (folder.isBlank()) "Saving to: Vault root" else "Saving to: $folder"

        findViewById<Button>(R.id.attachImageButton).setOnClickListener {
            pickImage.launch("image/*")
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveNote(titleInput.text.toString(), bodyInput.text.toString())
        }

        findViewById<Button>(R.id.cancelButton).setOnClickListener {
            finish()
        }

        titleInput.requestFocus()
    }

    private fun saveNote(title: String, body: String) {
        val vaultUri = VaultPrefs.getVaultUri(this)
        if (vaultUri == null) {
            Toast.makeText(this, "No vault selected. Open Vault Quick Add and set it up first.", Toast.LENGTH_LONG).show()
            return
        }
        if (title.isBlank() && body.isBlank() && pickedImageUri == null) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show()
            return
        }

        val root = DocumentFile.fromTreeUri(this, vaultUri)
        if (root == null || !root.exists()) {
            Toast.makeText(this, "Can't access vault folder. Re-select it in the app.", Toast.LENGTH_LONG).show()
            return
        }

        val targetFolder = VaultWriter.resolveFolder(root, folder)

        var imageFileName: String? = null
        pickedImageUri?.let { uri ->
            imageFileName = VaultWriter.copyImageIntoAttachments(this, targetFolder, uri)
        }

        val fullBody = buildString {
            append(body)
        }

        val ok = VaultWriter.writeNote(
            context = this,
            folder = targetFolder,
            title = title,
            body = fullBody,
            imageFileName = imageFileName
        )

        if (ok) {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Save failed", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_FOLDER = "extra_folder"
        const val EXTRA_WIDGET_ID = "extra_widget_id"
    }
}
