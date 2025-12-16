package com.example.ddaywidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * 여러 이벤트를 선택할 수 있는 위젯 설정 Activity
 * 
 * 경로: app/src/main/java/com/example/ddaywidget/MultiWidgetConfigActivity.kt
 */
class MultiWidgetConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var prefs: WidgetPreferences
    private val selectedEventIds = mutableListOf<String>()
    private var selectedImageUri: String? = null
    private var selectedStickerId: Int? = null
    private var selectedTheme: WidgetTheme = WidgetTheme.LIGHT

    // UI 컴포넌트
    private lateinit var eventListView: ListView
    private lateinit var selectedEventsText: TextView
    private lateinit var themeSpinner: Spinner
    private lateinit var selectImageButton: Button
    private lateinit var selectStickerButton: Button
    private lateinit var bgColorSeekBar: SeekBar
    private lateinit var textColorSeekBar: SeekBar
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_widget_config)

        // 위젯 ID 가져오기
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // 잘못된 위젯 ID인 경우 취소
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // 기본 결과를 취소로 설정
        setResult(Activity.RESULT_CANCELED)

        prefs = WidgetPreferences(this)
        
        initViews()
        loadEvents()
        setupListeners()
    }

    private fun initViews() {
        eventListView = findViewById(R.id.event_list_view)
        selectedEventsText = findViewById(R.id.selected_events_text)
        themeSpinner = findViewById(R.id.theme_spinner)
        selectImageButton = findViewById(R.id.select_image_button)
        selectStickerButton = findViewById(R.id.select_sticker_button)
        bgColorSeekBar = findViewById(R.id.bg_color_seekbar)
        textColorSeekBar = findViewById(R.id.text_color_seekbar)
        confirmButton = findViewById(R.id.confirm_button)

        // 테마 스피너 설정
        val themeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("밝은 테마", "어두운 테마", "파스텔", "선명한 색상", "미니멀")
        )
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = themeAdapter

        updateSelectedEventsText()
    }

    private fun loadEvents() {
        val events = prefs.loadEvents()
        
        if (events.isEmpty()) {
            Toast.makeText(this, "먼저 이벤트를 추가해주세요", Toast.LENGTH_SHORT).show()
            // 메인 액티비티로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // 이벤트 리스트 어댑터 설정 (멀티 선택)
        val eventNames = events.map { it.title }.toTypedArray()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            eventNames
        )
        eventListView.adapter = adapter
        eventListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
    }

    private fun setupListeners() {
        // 이벤트 선택
        eventListView.setOnItemClickListener { _, _, position, _ ->
            val events = prefs.loadEvents()
            val selectedEvent = events[position]
            
            if (selectedEventIds.contains(selectedEvent.id)) {
                selectedEventIds.remove(selectedEvent.id)
            } else {
                if (selectedEventIds.size < 3) {
                    selectedEventIds.add(selectedEvent.id)
                } else {
                    Toast.makeText(this, "최대 3개까지 선택 가능합니다", Toast.LENGTH_SHORT).show()
                    eventListView.setItemChecked(position, false)
                }
            }
            
            updateSelectedEventsText()
        }

        // 테마 변경
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedTheme = when (position) {
                    0 -> WidgetTheme.LIGHT
                    1 -> WidgetTheme.DARK
                    2 -> WidgetTheme.PASTEL
                    3 -> WidgetTheme.VIBRANT
                    4 -> WidgetTheme.MINIMAL
                    else -> WidgetTheme.LIGHT
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 배경 이미지 선택 버튼
        selectImageButton.setOnClickListener {
            openImagePicker()
        }
        
        // 스티커 선택 버튼
        selectStickerButton.setOnClickListener {
            showStickerPicker()
        }

        // 확인 버튼
        confirmButton.setOnClickListener {
            saveConfiguration()
        }
    }

    private fun updateSelectedEventsText() {
        if (selectedEventIds.isEmpty()) {
            selectedEventsText.text = "이벤트를 선택하세요 (최소 1개, 최대 3개)"
        } else {
            val events = prefs.loadEvents()
            val selectedNames = selectedEventIds.mapNotNull { id ->
                events.find { it.id == id }?.title
            }
            selectedEventsText.text = "선택된 이벤트: ${selectedNames.joinToString(", ")}"
        }
    }

    /**
     * 이미지 선택기 열기
     */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    
    /**
     * 스티커 선택 다이얼로그 표시
     */
    private fun showStickerPicker() {
        val stickerNames = StickerResources.getAllStickers().map { it.name }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("스티커 선택")
            .setItems(stickerNames) { _, which ->
                val sticker = StickerResources.getAllStickers()[which]
                selectedStickerId = sticker.id
                Toast.makeText(this, "${sticker.name} 선택됨", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("스티커 제거") { _, _ ->
                selectedStickerId = null
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // 영구 권한 요청
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedImageUri = uri.toString()
                Toast.makeText(this, "배경 이미지 선택됨", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveConfiguration() {
        if (selectedEventIds.isEmpty()) {
            Toast.makeText(this, "최소 1개의 이벤트를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 설정 저장
        prefs.saveWidgetEvents(appWidgetId, selectedEventIds)
        
        val bgColor = Color.argb(
            255,
            bgColorSeekBar.progress,
            bgColorSeekBar.progress,
            bgColorSeekBar.progress
        )
        prefs.saveBackgroundColor(appWidgetId, bgColor)

        val textColor = Color.argb(
            255,
            textColorSeekBar.progress,
            textColorSeekBar.progress,
            textColorSeekBar.progress
        )
        prefs.saveTextColor(appWidgetId, textColor)
        
        // 새로운 설정 저장
        prefs.saveBackgroundImage(appWidgetId, selectedImageUri)
        prefs.saveStickerId(appWidgetId, selectedStickerId)
        prefs.saveTheme(appWidgetId, selectedTheme)

        // 위젯 업데이트
        val appWidgetManager = AppWidgetManager.getInstance(this)
        DdayMultiWidgetProvider().onUpdate(
            this,
            appWidgetManager,
            intArrayOf(appWidgetId)
        )

        // 결과 반환
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
    
    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
}
