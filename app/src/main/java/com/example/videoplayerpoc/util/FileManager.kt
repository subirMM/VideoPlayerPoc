package com.example.videoplayerpoc.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileManagerUtil(private val context: Context) {

    private fun getPrivateFileDirectory(dir: String): File? {
        val directory = File(context.filesDir, dir)
        return if (directory.exists() || directory.mkdirs()) {
            directory
        } else null
    }

    suspend fun createFile(directory: String, ext: String): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat(
                FILE_TIMESTAMP_FORMAT,
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            val fileName = "$timestamp.$ext"
            val filePath = File(getPrivateFileDirectory(directory), fileName).canonicalPath
            return@withContext Pair(filePath, fileName)
        }
    }

    companion object {
        const val FILE_TIMESTAMP_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}