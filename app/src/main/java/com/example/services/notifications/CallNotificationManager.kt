package com.example.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object CallNotificationManager {

    const val CHANNEL_CALL_PROTECTION = "callshield_protection"
    const val CHANNEL_RISK_ALERTS = "callshield_threats"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val protectionChannel = NotificationChannel(
                CHANNEL_CALL_PROTECTION,
                "Call Protection & Screening",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when unwanted calls are blocked or screened"
            }

            val threatChannel = NotificationChannel(
                CHANNEL_RISK_ALERTS,
                "Security & Risk Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts when potential scam indicators are detected"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(protectionChannel)
            manager?.createNotificationChannel(threatChannel)
        }
    }

    fun notifyBlockedCall(context: Context, phoneNumber: String, reason: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CALL_PROTECTION)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🛡 CallShield AI — Call Blocked")
            .setContentText("Blocked incoming call from $phoneNumber")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Blocked caller $phoneNumber\nReason: $reason"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(phoneNumber.hashCode(), notification)
        }
    }

    fun notifyScreeningActive(context: Context, phoneNumber: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CALL_PROTECTION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🛡 CallShield AI — Screening Active")
            .setContentText("AI Assistant is screening caller $phoneNumber")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(phoneNumber.hashCode() + 1, notification)
        }
    }

    fun notifyScreenedSummary(
        context: Context,
        caller: String,
        purpose: String,
        action: String,
        hasRisk: Boolean
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channel = if (hasRisk) CHANNEL_RISK_ALERTS else CHANNEL_CALL_PROTECTION
        val title = if (hasRisk) "⚠ CallShield AI — Potential Risk Alert" else "🛡 CallShield AI — Call Summary"
        val body = "Caller: $caller\nPurpose: $purpose\nAction: $action\nTap to view full assessment."

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Call from $caller ($action)")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (hasRisk) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(caller.hashCode() + 2, notification)
        }
    }
}
