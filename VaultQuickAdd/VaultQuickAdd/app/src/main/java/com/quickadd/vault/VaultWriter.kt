package com.quickadd.vault

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VaultWriter {

    // Finds or creates each segment of a folder path under root, e.g. "Anatomy/Cardio"
    fun resolveFolder(root: DocumentFile, path: String): DocumentFile {
        var current = root
        if (path.isBlank()) return current
        for (segment in path.split("/").filter { it.isNotBlank() }) {
            val existing = current.findFile(segment)
            current = if (existing != null && existing.isDirectory) {
                existing
            } else {
                current.createDirectory(segment) ?: current
            }
        }
        return current
    }

    fun sanitizeFileName(name: String): String {
        val cleaned = name.trim().replace(Regex("[\\\\/:*?\"<>|]"), "-")
        return cleaned.ifBlank { "" }
    }

    fun timestampName(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault())
        return fmt.format(Date())
    }

    // Copies an image (given its content Uri) into <folder>/Attachments/ and returns the file name used
    fun copyImageIntoAttachments(context: Context, folder: DocumentFile, imageUri: Uri): String? {
        val attachments = resolveFolder(folder, "Attachments")
        val ext = context.contentResolver.getType(imageUri)?.substringAfterLast("/") ?: "jpg"
        val fileName = "img-${System.currentTimeMillis()}.$ext"
        val mime = context.contentResolver.getType(imageUri) ?: "image/*"
        val target = attachments.createFile(mime, fileName) ?: return null

        context.contentResolver.openInputStream(imageUri)?.use { input ->
            context.contentResolver.openOutputStream(target.uri)?.use { output ->
                input.copyTo(output)
            }
        }
        return target.name
    }

    // Writes the markdown note. Returns true on success.
    fun writeNote(
        context: Context,
        folder: DocumentFile,
        title: String,
        body: String,
        imageFileName: String?
    ): Boolean {
        val safeTitle = sanitizeFileName(title)
        val fileName = if (safeTitle.isNotBlank()) "$safeTitle.md" else "${timestampName()}.md"

        // Avoid overwriting an existing note with the same title
        var finalName = fileName
        var counter = 1
        while (folder.findFile(finalName) != null) {
            val base = fileName.removeSuffix(".md")
            finalName = "$base ($counter).md"
            counter++
        }

        val file = folder.createFile("text/markdown", finalName) ?: return false

        val content = StringBuilder()
        content.append(body.trim())
        if (imageFileName != null) {
            content.append("\n\n")
            content.append("![[$imageFileName]]")
        }

        context.contentResolver.openOutputStream(file.uri)?.use { output ->
            output.write(content.toString().toByteArray())
        } ?: return false

        return true
    }
}
