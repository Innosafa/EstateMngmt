package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import java.text.NumberFormat
import java.util.Locale

object NotificationHelper {
    private const val CHANNEL_ID = "estateiq_monthly_summary_channel"
    private const val CHANNEL_NAME = "Monthly Financial Summaries"
    private const val NOTIFICATION_ID = 2026

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Monthly summary reports for property owners showing total revenue and maintenance expenditures"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendMonthlySummaryNotification(
        context: Context,
        month: String,
        totalRevenue: Double,
        maintenanceCosts: Double,
        netIncome: Double,
        occupancyRate: Double
    ): Boolean {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }

        val formattedRevenue = "KES ${currencyFormat.format(totalRevenue)}"
        val formattedMaintenance = "KES ${currencyFormat.format(maintenanceCosts)}"
        val formattedNet = "KES ${currencyFormat.format(netIncome)}"
        val formattedOccupancy = "%.1f%%".format(occupancyRate)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigText = """
            📊 Monthly Portfolio Performance ($month):
            • Total Revenue: $formattedRevenue
            • Maintenance Costs: $formattedMaintenance
            • Net Operating Income: $formattedNet
            • Portfolio Occupancy: $formattedOccupancy
            
            Transactions encrypted with your secure master password. Tap to review breakdown in EstateIQ.
        """.trimIndent()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("EstateIQ Owner Summary • $month")
            .setContentText("Revenue: $formattedRevenue | Maintenance: $formattedMaintenance | Net: $formattedNet")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
            return true
        } catch (_: SecurityException) {
            return false
        }
    }
}
