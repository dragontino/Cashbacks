package com.cashbacks.features.share.data.repo

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cashbacks.features.share.domain.repo.ShareDataRepository
import com.opencsv.CSVWriter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.resumeWithException

class ShareDataRepositoryImpl(
    private val database: RoomDatabase,
    private val context: Context
) : ShareDataRepository {
    private companion object {
        const val TAG = "ShareData"
    }

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

            createDatabaseVersionFile(
                readableDatabase = readableDatabase,
                outputPath = context.cacheDir.absolutePath
            ).let { versionFile ->
                try {
                    zipOutputStream.writeFile(versionFile)
                } finally {
                    versionFile.deleteOnExit()
                }
            }

            val tableNames = listOf("Settings", "Categories", "Shops", "Cashbacks", "Cards")
            tableNames.forEach { tableName ->
                var file: File? = null
                try {
                    file = createCsvFileFromTable(
                        readableDatabase = readableDatabase,
                        tableName = tableName,
                        outputPath = context.cacheDir.absolutePath
                    )
                    Log.d(TAG, file.readText())
                    zipOutputStream.writeFile(file)
                } finally {
                    file?.deleteOnExit()
                }
            }

            continuation.resume(zipFile.parentFile?.absolutePath ?: zipFile.absolutePath) { throwable, _, _ ->
                continuation.resumeWithException(throwable)
            }

        } catch (exception: Exception) {
            continuation.resumeWithException(exception)
        } finally {
            zipOutputStream.close()
        }
    }



    private fun createCsvFileFromTable(
        readableDatabase: SupportSQLiteDatabase,
        tableName: String,
        outputPath: String
    ): File {
        val resultFile = File(outputPath, "$tableName.csv")
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


    private fun createDatabaseVersionFile(
        readableDatabase: SupportSQLiteDatabase,
        outputPath: String,
    ): File {
        val version = readableDatabase.version.toString()
        return File(outputPath, "Version.txt").apply {
            writeText(version)
            Log.d(TAG, "Database version: ${readText()}")
        }
    }


    private fun ZipOutputStream.writeFile(file: File) {
        file.inputStream().use { inputStream ->
            putNextEntry(ZipEntry(file.name))
            Log.d(TAG, "${file.name}: ${inputStream.reader().readText()}")
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