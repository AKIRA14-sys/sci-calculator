package com.supersci.calculator.gamehub

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class GamePanelOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    companion object {
        const val CHANNEL_ID = "GamePanelOverlayChannel"
        const val NOTIF_ID = 1001

        var crosshairPreset: String = "CROSS" // CROSS, DOT, CIRCLE
        var crosshairSize: Float = 24f
        var crosshairOpacity: Float = 1.0f
        var crosshairThickness: Float = 3f
        var crosshairColor: Int = Color.RED
        var centerDot: Boolean = true

        fun start(context: Context) {
            val intent = Intent(context, GamePanelOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, GamePanelOverlayService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIF_ID, notification)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    private fun showOverlay() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            (crosshairSize * 3).toInt().coerceAtLeast(100),
            (crosshairSize * 3).toInt().coerceAtLeast(100),
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        overlayView = CrosshairView(this)
        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class CrosshairView(context: Context) : View(context) {
        private val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f

            paint.color = crosshairColor
            paint.alpha = (crosshairOpacity * 255).toInt().coerceIn(0, 255)
            paint.strokeWidth = crosshairThickness

            val len = crosshairSize / 2f

            when (crosshairPreset) {
                "CROSS" -> {
                    canvas.drawLine(cx - len, cy, cx + len, cy, paint)
                    canvas.drawLine(cx, cy - len, cx, cy + len, paint)
                }
                "CIRCLE" -> {
                    canvas.drawCircle(cx, cy, len, paint)
                }
                "DOT" -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(cx, cy, crosshairThickness * 1.5f, paint)
                }
            }

            if (centerDot && crosshairPreset != "DOT") {
                val dotPaint = Paint(paint).apply {
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(cx, cy, crosshairThickness * 1.2f, dotPaint)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GamePanel Crosshair Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Super Sci Calculator GamePanel")
            .setContentText("Crosshair visual overlay active")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
