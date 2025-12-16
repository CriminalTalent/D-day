package com.example.ddaywidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.*

/**
 * 디데이 위젯 프로바이더
 */
class DdayWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.ddaywidget.UPDATE_WIDGET"
        private const val UPDATE_INTERVAL = 60 * 1000L // 1분마다 업데이트
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 모든 위젯 업데이트
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        
        // 주기적 업데이트 스케줄링
        scheduleUpdate(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // 위젯 삭제 시 설정 제거
        val prefs = WidgetPreferences(context)
        for (appWidgetId in appWidgetIds) {
            prefs.deleteWidgetConfig(appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // 첫 위젯 추가 시 알람 설정
        scheduleUpdate(context)
    }

    override fun onDisabled(context: Context) {
        // 마지막 위젯 제거 시 알람 취소
        cancelUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_UPDATE_WIDGET) {
            // 커스텀 업데이트 액션
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DdayWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    /**
     * 개별 위젯 업데이트
     */
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = WidgetPreferences(context)
        val event = prefs.loadWidgetEvent(appWidgetId) ?: return

        // RemoteViews 생성
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // 이벤트 정보 표시
        views.setTextViewText(R.id.widget_title, event.title)
        views.setTextViewText(R.id.widget_dday, DdayCalculator.getDisplayText(event))
        views.setTextViewText(R.id.widget_detail, DdayCalculator.getDetailedText(event.targetDate))

        // 테마 적용
        applyTheme(views, prefs.loadTheme(appWidgetId))

        // 배경 이미지 적용
        val bgImageUri = prefs.loadBackgroundImage(appWidgetId)
        if (bgImageUri != null) {
            try {
                val bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    android.net.Uri.parse(bgImageUri)
                )
                views.setImageViewBitmap(R.id.widget_background_image, bitmap)
                views.setViewVisibility(R.id.widget_background_image, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.widget_overlay, android.view.View.VISIBLE)
            } catch (e: Exception) {
                views.setViewVisibility(R.id.widget_background_image, android.view.View.GONE)
                views.setViewVisibility(R.id.widget_overlay, android.view.View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.widget_background_image, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_overlay, android.view.View.GONE)
            
            // 배경색 적용 (이미지가 없을 때만)
            val bgColor = prefs.loadBackgroundColor(appWidgetId)
            views.setInt(R.id.widget_container, "setBackgroundColor", bgColor)
        }

        // 스티커 적용
        val stickerId = prefs.loadStickerId(appWidgetId)
        if (stickerId != null) {
            val stickerItem = StickerResources.getStickerById(stickerId)
            if (stickerItem != null) {
                views.setImageViewResource(R.id.widget_sticker, stickerItem.resourceId)
                views.setViewVisibility(R.id.widget_sticker, android.view.View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_sticker, android.view.View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.widget_sticker, android.view.View.GONE)
        }

        // 텍스트 색상 및 크기 적용
        val textColor = prefs.loadTextColor(appWidgetId)
        val fontSize = prefs.loadFontSize(appWidgetId)

        views.setTextColor(R.id.widget_title, textColor)
        views.setTextColor(R.id.widget_dday, textColor)
        views.setTextColor(R.id.widget_detail, textColor)
        views.setFloat(R.id.widget_dday, "setTextSize", fontSize)

        // 프레임 스타일 적용
        applyFrameStyle(context, views, prefs.loadFrameStyle(appWidgetId))

        // 클릭 시 MainActivity 열기
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("eventId", event.id)
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        // 위젯 업데이트
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * 테마 적용
     */
    private fun applyTheme(views: RemoteViews, theme: WidgetTheme) {
        val (bgColor, textColor) = when (theme) {
            WidgetTheme.LIGHT -> Pair(0xFFFFFFFF.toInt(), 0xFF000000.toInt())
            WidgetTheme.DARK -> Pair(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
            WidgetTheme.PASTEL -> Pair(0xFFFFF5F5.toInt(), 0xFF4A4A4A.toInt())
            WidgetTheme.VIBRANT -> Pair(0xFFFF6B6B.toInt(), 0xFFFFFFFF.toInt())
            WidgetTheme.MINIMAL -> Pair(0xFFF5F5F5.toInt(), 0xFF333333.toInt())
        }
        // 테마는 배경 이미지가 없을 때만 적용됨
    }

    /**
     * 프레임 스타일 적용
     */
    private fun applyFrameStyle(context: Context, views: RemoteViews, frameStyle: FrameStyle) {
        // 프레임 스타일에 따라 배경 drawable 변경
        // 실제 구현 시 drawable 리소스가 필요
        when (frameStyle) {
            FrameStyle.NONE -> {} // 프레임 없음
            FrameStyle.ROUND_CORNER -> {
                // views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.frame_round_corner)
            }
            FrameStyle.CIRCLE -> {
                // views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.frame_circle)
            }
            FrameStyle.HEART -> {
                // views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.frame_heart)
            }
            FrameStyle.STAR -> {
                // views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.frame_star)
            }
            FrameStyle.POLAROID -> {
                // views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.frame_polaroid)
            }
        }
    }

    /**
     * 주기적 업데이트 스케줄링
     */
    private fun scheduleUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DdayWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 다음 분의 시작 시간 계산
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 반복 알람 설정
        alarmManager.setRepeating(
            AlarmManager.RTC,
            calendar.timeInMillis,
            UPDATE_INTERVAL,
            pendingIntent
        )
    }

    /**
     * 업데이트 알람 취소
     */
    private fun cancelUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DdayWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}
