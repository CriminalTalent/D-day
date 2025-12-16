package com.example.ddaywidget

import java.io.Serializable

/**
 * 디데이 이벤트 데이터 클래스
 */
data class Event(
    val id: String,
    val title: String,
    val targetDate: Long, // timestamp
    val displayFormat: DisplayFormat = DisplayFormat.D_DAY,
    val notificationEnabled: Boolean = false
) : Serializable

/**
 * 날짜 표시 형식
 */
enum class DisplayFormat {
    D_DAY,      // D-100, D-Day, D+100
    D_PLUS,     // D+100 형식만
    FULL_DATE,  // 2024년 12월 25일
    REMAINING   // 100일 남음
}
