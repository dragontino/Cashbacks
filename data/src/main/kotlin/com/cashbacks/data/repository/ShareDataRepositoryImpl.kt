package com.cashbacks.data.repository

import android.os.Environment
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cashbacks.data.room.AppDatabase
import com.cashbacks.domain.repository.ShareDataRepository
import com.opencsv.CSVWriter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.collections.listOf
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ShareDataRepositoryImpl(private val database: AppDatabase) : ShareDataRepository {
    override suspend fun importData(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun exportData(): Result<String> {
        return try {
            val resultPath = exportDatabaseIntoZip()
            Result.success(resultPath)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }


    private suspend fun exportDatabaseIntoZip(): String = suspendCancellableCoroutine { continuation ->
        val archiveFileName = createFileNameFromCurrentTime() + ".zip"
        val exportDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Cashbacks"
        )

        if (exportDir.exists().not()) {
            exportDir.mkdirs()
        }

        val zipFile = File(exportDir, archiveFileName)
        val zipOutputStream = ZipOutputStream(zipFile.outputStream())

        try {
            val readableDatabase = database.openHelper.readableDatabase

            createDatabaseVersionFile(readableDatabase).let { versionFile ->
                try {
                    zipOutputStream.writeFile(versionFile)
                } finally {
                    versionFile.delete()
                }
            }

            val tableNames = listOf("Settings", "Categories", "Shops", "Cashbacks", "Cards")
            tableNames.forEach { tableName ->
                var file: File? = null
                try {
                    file = createCsvFileFromTable(
                        readableDatabase = readableDatabase,
                        tableName = tableName
                    )
                    Log.d("ShareData", file.readText())
                    zipOutputStream.writeFile(file)
                } finally {
                    file?.delete()
                }
            }

        } catch (exception: Exception) {
            continuation.resumeWithException(exception)
        }

        continuation.resume(zipFile.absolutePath)
    }



    private fun createCsvFileFromTable(
        readableDatabase: SupportSQLiteDatabase,
        tableName: String
    ): File {
        val resultFile = File("$tableName.csv")
        CSVWriter(resultFile.writer()).use { writer ->
            readableDatabase.query("SELECT * FROM $tableName").use { cursor ->
                writer.writeNext(cursor.columnNames)
                while (cursor.moveToNext()) {
                    val data = Array(cursor.columnCount) { cursor.getString(it) }
                    writer.writeNext(data)
                }
            }
        }

        return resultFile
    }


    private fun createDatabaseVersionFile(readableDatabase: SupportSQLiteDatabase): File {
        val version = readableDatabase.version.toString()
        return File("Version.txt").apply {
            writeText(version)
            Log.d("ShareData", "Database version: ${readText()}")
        }
    }


    private fun ZipOutputStream.writeFile(file: File) = use { zipOut ->
        file.inputStream().use { inputStream ->
            zipOut.putNextEntry(ZipEntry(file.name))
            Log.d("ShareData", "${file.name}: ${inputStream.reader().readText()}")
            inputStream.copyTo(this)
            closeEntry()
        }
    }


    private fun createFileNameFromCurrentTime(): String {
        val nowDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val format = kotlinx.datetime.LocalDateTime.Format {
            year()
            monthNumber()
            dayOfMonth()
            char('_')
            hour()
            minute()
            second()
        }
        return "Cashbacks_backup_${format.format(nowDateTime)}"
    }
}