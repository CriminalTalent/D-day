package com.example.ddaywidget

/**
 * 스티커 리소스 관리
 * 
 * 경로: app/src/main/java/com/example/ddaywidget/StickerResources.kt
 */
object StickerResources {
    
    /**
     * 귀여운 스티커 카테고리
     */
    val CUTE_STICKERS = listOf(
        StickerItem(1, "하트", R.drawable.sticker_heart),
        StickerItem(2, "별", R.drawable.sticker_star),
        StickerItem(3, "토끼", R.drawable.sticker_rabbit),
        StickerItem(4, "곰", R.drawable.sticker_bear),
        StickerItem(5, "고양이", R.drawable.sticker_cat),
        StickerItem(6, "강아지", R.drawable.sticker_dog),
        StickerItem(7, "꽃", R.drawable.sticker_flower),
        StickerItem(8, "무지개", R.drawable.sticker_rainbow)
    )
    
    /**
     * 미니멀 아이콘 카테고리
     */
    val MINIMAL_ICONS = listOf(
        StickerItem(101, "체크", R.drawable.icon_check),
        StickerItem(102, "달력", R.drawable.icon_calendar),
        StickerItem(103, "시계", R.drawable.icon_clock),
        StickerItem(104, "알람", R.drawable.icon_alarm),
        StickerItem(105, "깃발", R.drawable.icon_flag),
        StickerItem(106, "트로피", R.drawable.icon_trophy),
        StickerItem(107, "하트", R.drawable.icon_heart_minimal),
        StickerItem(108, "별", R.drawable.icon_star_minimal)
    )
    
    /**
     * 모든 스티커 반환
     */
    fun getAllStickers(): List<StickerItem> {
        return CUTE_STICKERS + MINIMAL_ICONS
    }
    
    /**
     * ID로 스티커 찾기
     */
    fun getStickerById(id: Int): StickerItem? {
        return getAllStickers().find { it.id == id }
    }
}

/**
 * 스티커 아이템 데이터 클래스
 */
data class StickerItem(
    val id: Int,
    val name: String,
    val resourceId: Int
)
