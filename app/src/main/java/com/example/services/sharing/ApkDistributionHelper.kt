package com.example.services.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.CallEntity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ApkDistributionHelper {

    /**
     * Shares the app's own APK file directly to other devices via WhatsApp,
     * Telegram, Bluetooth, Google Drive, Email, or Nearby Share / Quick Share
     * without needing Google Play Store or Play Console!
     */
    fun shareApkFile(context: Context) {
        try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (!sourceApk.exists()) {
                Toast.makeText(context, "Unable to locate base APK file", Toast.LENGTH_LONG).show()
                return
            }

            // Copy to app's cache directory with a user-friendly name
            val cacheDir = File(context.cacheDir, "apks")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val targetApk = File(cacheDir, "CallShield-AI-v1.0.apk")
            if (!targetApk.exists() || targetApk.length() != sourceApk.length()) {
                FileInputStream(sourceApk).use { input ->
                    FileOutputStream(targetApk).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                targetApk
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "CallShield AI - Autonomous Call Screener & Firewall")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Install CallShield AI directly! Autonomous on-device spam blocking, AI call screening, and threat intelligence. No Play Store required.\n\nInstructions to install:\n1. Download/open the APK\n2. Allow 'Install Unknown Apps'\n3. Enjoy CallShield AI!"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share CallShield APK via...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Error sharing APK: ${e.localizedMessage ?: "Unknown error"}. You can also download the APK directly from AI Studio project settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Exports call logs into a formatted CSV report and shares it via system share sheet.
     */
    fun exportCallLogsCsv(context: Context, calls: List<CallEntity>) {
        try {
            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val csvFile = File(cacheDir, "callshield_logs_$timestamp.csv")

            csvFile.bufferedWriter().use { writer ->
                writer.write("ID,Timestamp,Phone Number,Caller Name,Direction,Action,Category,Risk Level,Threat Score,Duration (s),Summary\n")
                calls.forEach { call ->
                    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(call.timestamp))
                    val cleanSummary = call.summary.replace("\"", "\"\"")
                    val cleanCaller = call.callerName.replace("\"", "\"\"")
                    writer.write(
                        "\"${call.id}\",\"$dateStr\",\"${call.phoneNumber}\",\"$cleanCaller\",\"${call.direction.name}\",\"${call.action.name}\",\"${call.category.displayName}\",\"${call.riskLevel.name}\",\"${call.threatScore}\",\"${call.durationSeconds}\",\"$cleanSummary\"\n"
                    )
                }
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", csvFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "CallShield Security & Call Logs Export")
                putExtra(Intent.EXTRA_TEXT, "Exported ${calls.size} call records from CallShield AI.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Call Logs via...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export logs: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
