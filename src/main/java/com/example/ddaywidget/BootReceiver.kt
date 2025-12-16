package com.example.ddaywidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 기기 부팅 후 위젯 업데이트 알람을 재설정하는 리시버
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 위젯 업데이트 알람 재설정
            val updateIntent = Intent(context, DdayWidgetProvider::class.java).apply {
                action = DdayWidgetProvider.ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(updateIntent)
        }
    }
}
