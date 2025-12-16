package com.example.ddaywidget

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 위젯 설정을 SharedPreferences에 저장/로드하는 헬퍼 클래스
 */
class WidgetPreferences(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "DdayWidgetPrefs"
        private const val KEY_EVENTS = "events"
        private const val KEY_WIDGET_EVENT = "widget_event_" // + widgetId
        private const val KEY_BACKGROUND_COLOR = "bg_color_"
        private const val KEY_TEXT_COLOR = "text_color_"
        private const val KEY_FONT_SIZE = "font_size_"
        private const val KEY_BACKGROUND_IMAGE = "bg_image_"
        private const val KEY_STICKER_ID = "sticker_id_"
        private const val KEY_FRAME_STYLE = "frame_style_"
        private const val KEY_THEME = "theme_"
    }

    /**
     * 모든 이벤트 저장
     */
    fun saveEvents(events: List<Event>) {
        val json = gson.toJson(events)
        prefs.edit().putString(KEY_EVENTS, json).apply()
    }

    /**
     * 모든 이벤트 로드
     */
    fun loadEvents(): List<Event> {
        val json = prefs.getString(KEY_EVENTS, null) ?: return emptyList()
        val type = object : TypeToken<List<Event>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * 특정 위젯에 연결된 이벤트 ID 저장
     */
    fun saveWidgetEvent(widgetId: Int, eventId: String) {
        prefs.edit().putString(KEY_WIDGET_EVENT + widgetId, eventId).apply()
    }

    /**
     * 특정 위젯에 연결된 이벤트 로드
     */
    fun loadWidgetEvent(widgetId: Int): Event? {
        val eventId = prefs.getString(KEY_WIDGET_EVENT + widgetId, null) ?: return null
        return loadEvents().find { it.id == eventId }
    }

    /**
     * 위젯 배경색 저장
     */
    fun saveBackgroundColor(widgetId: Int, color: Int) {
        prefs.edit().putInt(KEY_BACKGROUND_COLOR + widgetId, color).apply()
    }

    /**
     * 위젯 배경색 로드
     */
    fun loadBackgroundColor(widgetId: Int, defaultColor: Int = 0xFF000000.toInt()): Int {
        return prefs.getInt(KEY_BACKGROUND_COLOR + widgetId, defaultColor)
    }

    /**
     * 위젯 텍스트 색상 저장
     */
    fun saveTextColor(widgetId: Int, color: Int) {
        prefs.edit().putInt(KEY_TEXT_COLOR + widgetId, color).apply()
    }

    /**
     * 위젯 텍스트 색상 로드
     */
    fun loadTextColor(widgetId: Int, defaultColor: Int = 0xFFFFFFFF.toInt()): Int {
        return prefs.getInt(KEY_TEXT_COLOR + widgetId, defaultColor)
    }

    /**
     * 위젯 폰트 크기 저장
     */
    fun saveFontSize(widgetId: Int, size: Float) {
        prefs.edit().putFloat(KEY_FONT_SIZE + widgetId, size).apply()
    }

    /**
     * 위젯 폰트 크기 로드
     */
    fun loadFontSize(widgetId: Int, defaultSize: Float = 16f): Float {
        return prefs.getFloat(KEY_FONT_SIZE + widgetId, defaultSize)
    }

    /**
     * 위젯 배경 이미지 URI 저장
     */
    fun saveBackgroundImage(widgetId: Int, imageUri: String?) {
        if (imageUri != null) {
            prefs.edit().putString(KEY_BACKGROUND_IMAGE + widgetId, imageUri).apply()
        } else {
            prefs.edit().remove(KEY_BACKGROUND_IMAGE + widgetId).apply()
        }
    }

    /**
     * 위젯 배경 이미지 URI 로드
     */
    fun loadBackgroundImage(widgetId: Int): String? {
        return prefs.getString(KEY_BACKGROUND_IMAGE + widgetId, null)
    }

    /**
     * 위젯 스티커 ID 저장
     */
    fun saveStickerId(widgetId: Int, stickerId: Int?) {
        if (stickerId != null) {
            prefs.edit().putInt(KEY_STICKER_ID + widgetId, stickerId).apply()
        } else {
            prefs.edit().remove(KEY_STICKER_ID + widgetId).apply()
        }
    }

    /**
     * 위젯 스티커 ID 로드
     */
    fun loadStickerId(widgetId: Int): Int? {
        return if (prefs.contains(KEY_STICKER_ID + widgetId)) {
            prefs.getInt(KEY_STICKER_ID + widgetId, 0)
        } else {
            null
        }
    }

    /**
     * 위젯 프레임 스타일 저장
     */
    fun saveFrameStyle(widgetId: Int, frameStyle: FrameStyle) {
        prefs.edit().putString(KEY_FRAME_STYLE + widgetId, frameStyle.name).apply()
    }

    /**
     * 위젯 프레임 스타일 로드
     */
    fun loadFrameStyle(widgetId: Int): FrameStyle {
        val styleName = prefs.getString(KEY_FRAME_STYLE + widgetId, FrameStyle.NONE.name)
        return try {
            FrameStyle.valueOf(styleName!!)
        } catch (e: Exception) {
            FrameStyle.NONE
        }
    }

    /**
     * 위젯 테마 저장
     */
    fun saveTheme(widgetId: Int, theme: WidgetTheme) {
        prefs.edit().putString(KEY_THEME + widgetId, theme.name).apply()
    }

    /**
     * 위젯 테마 로드
     */
    fun loadTheme(widgetId: Int): WidgetTheme {
        val themeName = prefs.getString(KEY_THEME + widgetId, WidgetTheme.LIGHT.name)
        return try {
            WidgetTheme.valueOf(themeName!!)
        } catch (e: Exception) {
            WidgetTheme.LIGHT
        }
    }

    /**
     * 특정 위젯 설정 삭제
     */
    fun deleteWidgetConfig(widgetId: Int) {
        prefs.edit()
            .remove(KEY_WIDGET_EVENT + widgetId)
            .remove(KEY_BACKGROUND_COLOR + widgetId)
            .remove(KEY_TEXT_COLOR + widgetId)
            .remove(KEY_FONT_SIZE + widgetId)
            .remove(KEY_BACKGROUND_IMAGE + widgetId)
            .remove(KEY_STICKER_ID + widgetId)
            .remove(KEY_FRAME_STYLE + widgetId)
            .remove(KEY_THEME + widgetId)
            .apply()
    }
}
