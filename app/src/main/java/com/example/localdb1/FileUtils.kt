package com.example.localdb1

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    fun getPath(context: Context, uri: Uri): String? {
        var filePath: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, displayName)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                outputStream.close()
                inputStream?.close()
                filePath = file.absolutePath
            }
        }
        return filePath
    }
}
