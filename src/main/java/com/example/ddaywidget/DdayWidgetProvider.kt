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
 * 여러 디데이를 표시하는 위젯 프로바이더 (최대 20개, 스티커 색상 커스터마이징 포함)
 * 
 * 📁 경로: app/src/main/java/com/example/ddaywidget/DdayMultiWidgetProvider.kt
 */
class DdayMultiWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.ddaywidget.UPDATE_MULTI_WIDGET"
        private const val UPDATE_INTERVAL = 60 * 1000L // 1분마다 업데이트
        
        // 20개의 이벤트 컨테이너 ID
        private val EVENT_CONTAINER_IDS = listOf(
            R.id.event_1_container, R.id.event_2_container, R.id.event_3_container,
            R.id.event_4_container, R.id.event_5_container, R.id.event_6_container,
            R.id.event_7_container, R.id.event_8_container, R.id.event_9_container,
            R.id.event_10_container, R.id.event_11_container, R.id.event_12_container,
            R.id.event_13_container, R.id.event_14_container, R.id.event_15_container,
            R.id.event_16_container, R.id.event_17_container, R.id.event_18_container,
            R.id.event_19_container, R.id.event_20_container
        )
        
        private val EVENT_TITLE_IDS = listOf(
            R.id.event_1_title, R.id.event_2_title, R.id.event_3_title,
            R.id.event_4_title, R.id.event_5_title, R.id.event_6_title,
            R.id.event_7_title, R.id.event_8_title, R.id.event_9_title,
            R.id.event_10_title, R.id.event_11_title, R.id.event_12_title,
            R.id.event_13_title, R.id.event_14_title, R.id.event_15_title,
            R.id.event_16_title, R.id.event_17_title, R.id.event_18_title,
            R.id.event_19_title, R.id.event_20_title
        )
        
        private val EVENT_DDAY_IDS = listOf(
            R.id.event_1_dday, R.id.event_2_dday, R.id.event_3_dday,
            R.id.event_4_dday, R.id.event_5_dday, R.id.event_6_dday,
            R.id.event_7_dday, R.id.event_8_dday, R.id.event_9_dday,
            R.id.event_10_dday, R.id.event_11_dday, R.id.event_12_dday,
            R.id.event_13_dday, R.id.event_14_dday, R.id.event_15_dday,
            R.id.event_16_dday, R.id.event_17_dday, R.id.event_18_dday,
            R.id.event_19_dday, R.id.event_20_dday
        )
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
            val componentName = ComponentName(context, DdayMultiWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    /**
     * 개별 위젯 업데이트 (여러 이벤트 표시, 최대 20개)
     */
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = WidgetPreferences(context)
        val events = prefs.loadWidgetEvents(appWidgetId)
        
        if (events.isEmpty()) return

        // RemoteViews 생성
        val views = RemoteViews(context.packageName, R.layout.widget_layout_multi)

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
                
                // 🎨 스티커 색상 커스터마이징 적용
                if (prefs.isStickerColorEnabled(appWidgetId)) {
                    val stickerColor = prefs.loadStickerColor(appWidgetId)
                    views.setInt(R.id.widget_sticker, "setColorFilter", stickerColor)
                    views.setInt(R.id.widget_sticker, "setImageAlpha", 255)
                } else {
                    // 기본 색상 사용 (colorFilter 제거)
                    views.setInt(R.id.widget_sticker, "setColorFilter", 0)
                }
            } else {
                views.setViewVisibility(R.id.widget_sticker, android.view.View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.widget_sticker, android.view.View.GONE)
        }

        // 텍스트 색상
        val textColor = prefs.loadTextColor(appWidgetId)

        // 최대 20개의 이벤트 표시
        for (i in 0 until 20) {
            if (i < events.size) {
                val event = events[i]
                views.setTextViewText(EVENT_TITLE_IDS[i], event.title)
                views.setTextViewText(EVENT_DDAY_IDS[i], DdayCalculator.getDisplayText(event))
                views.setTextColor(EVENT_TITLE_IDS[i], textColor)
                views.setTextColor(EVENT_DDAY_IDS[i], textColor)
                views.setViewVisibility(EVENT_CONTAINER_IDS[i], android.view.View.VISIBLE)
            } else {
                views.setViewVisibility(EVENT_CONTAINER_IDS[i], android.view.View.GONE)
            }
        }

        // 클릭 시 MainActivity 열기
        val intent = Intent(context, MainActivity::class.java)
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
     * 주기적 업데이트 스케줄링
     */
    private fun scheduleUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DdayMultiWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1, // 다른 ID 사용
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
        val intent = Intent(context, DdayMultiWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}
