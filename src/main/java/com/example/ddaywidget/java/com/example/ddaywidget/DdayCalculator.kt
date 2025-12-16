package com.example.ddaywidget

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * 디데이 계산 및 포맷팅 유틸리티
 */
object DdayCalculator {

    /**
     * 이벤트의 디데이 텍스트 생성
     */
    fun getDisplayText(event: Event): String {
        val diff = calculateDifference(event.targetDate)
        
        return when (event.displayFormat) {
            DisplayFormat.D_DAY -> formatDday(diff)
            DisplayFormat.D_PLUS -> formatDplus(diff)
            DisplayFormat.FULL_DATE -> formatFullDate(event.targetDate)
            DisplayFormat.REMAINING -> formatRemaining(diff)
        }
    }

    /**
     * 현재 시간과의 차이 계산 (일 단위)
     */
    private fun calculateDifference(targetDate: Long): Long {
        val now = System.currentTimeMillis()
        val diff = targetDate - now
        return diff / (1000 * 60 * 60 * 24)
    }

    /**
     * D-Day 형식 (D-100, D-Day, D+100)
     */
    private fun formatDday(diff: Long): String {
        return when {
            diff > 0 -> "D-${diff}"
            diff == 0L -> "D-Day"
            else -> "D+${abs(diff)}"
        }
    }

    /**
     * D+ 형식만 (D+100)
     */
    private fun formatDplus(diff: Long): String {
        return "D+${abs(diff)}"
    }

    /**
     * 전체 날짜 형식 (2024년 12월 25일)
     */
    private fun formatFullDate(targetDate: Long): String {
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
        return sdf.format(Date(targetDate))
    }

    /**
     * 남은 시간 형식 (100일 남음)
     */
    private fun formatRemaining(diff: Long): String {
        return when {
            diff > 0 -> "${diff}일 남음"
            diff == 0L -> "오늘"
            else -> "${abs(diff)}일 지남"
        }
    }

    /**
     * 상세 정보 텍스트 (일/시/분 포함)
     */
    fun getDetailedText(targetDate: Long): String {
        val now = System.currentTimeMillis()
        val diff = targetDate - now
        
        val days = diff / (1000 * 60 * 60 * 24)
        val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            diff > 0 -> "${days}일 ${hours}시간 ${minutes}분"
            diff == 0L -> "오늘"
            else -> "${abs(days)}일 ${abs(hours)}시간 전"
        }
    }
}
