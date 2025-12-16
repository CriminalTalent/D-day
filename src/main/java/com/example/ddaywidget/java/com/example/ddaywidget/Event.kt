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
    val notificationEnabled: Boolean = false,
    val backgroundImageUri: String? = null, // 배경 이미지 URI
    val stickerId: Int? = null, // 스티커 리소스 ID
    val frameStyle: FrameStyle = FrameStyle.NONE,
    val theme: WidgetTheme = WidgetTheme.LIGHT
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

/**
 * 프레임 스타일
 */
enum class FrameStyle {
    NONE,           // 프레임 없음
    ROUND_CORNER,   // 둥근 모서리
    CIRCLE,         // 원형
    HEART,          // 하트 모양
    STAR,           // 별 모양
    POLAROID        // 폴라로이드 스타일
}

/**
 * 위젯 테마
 */
enum class WidgetTheme {
    LIGHT,      // 밝은 테마
    DARK,       // 어두운 테마
    PASTEL,     // 파스텔 톤
    VIBRANT,    // 선명한 색상
    MINIMAL     // 미니멀
}
