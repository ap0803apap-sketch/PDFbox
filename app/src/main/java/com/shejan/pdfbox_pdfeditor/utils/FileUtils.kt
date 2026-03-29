package com.shejan.pdfbox_pdfeditor.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "document.pdf"
    }

    fun autoSaveFile(context: Context, sourceFile: File, originalName: String, processName: String) {
        try {
            val appName = "PDF box by AP"
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appFolder = File(downloadDir, appName)
            val processFolder = File(appFolder, processName)
            
            if (!processFolder.exists()) {
                processFolder.mkdirs()
            }

            val nameWithoutExt = originalName.substringBeforeLast(".")
            val timeStamp = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
            val newFileName = "${nameWithoutExt}_${processName}_${timeStamp}_PDF_box_by_AP.pdf"
            val destFile = File(processFolder, newFileName)

            sourceFile.inputStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(context, "Saved to: ${destFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Auto-save failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
