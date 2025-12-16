package com.example.ddaywidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * 위젯 설정 Activity
 * 위젯 추가 시 나타나는 설정 화면
 */
class WidgetConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var prefs: WidgetPreferences
    private var selectedEvent: Event? = null

    // UI 컴포넌트
    private lateinit var eventSpinner: Spinner
    private lateinit var displayFormatSpinner: Spinner
    private lateinit var bgColorSeekBar: SeekBar
    private lateinit var textColorSeekBar: SeekBar
    private lateinit var fontSizeSeekBar: SeekBar
    private lateinit var previewContainer: LinearLayout
    private lateinit var previewTitle: TextView
    private lateinit var previewDday: TextView
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)

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
        eventSpinner = findViewById(R.id.event_spinner)
        displayFormatSpinner = findViewById(R.id.display_format_spinner)
        bgColorSeekBar = findViewById(R.id.bg_color_seekbar)
        textColorSeekBar = findViewById(R.id.text_color_seekbar)
        fontSizeSeekBar = findViewById(R.id.font_size_seekbar)
        previewContainer = findViewById(R.id.preview_container)
        previewTitle = findViewById(R.id.preview_title)
        previewDday = findViewById(R.id.preview_dday)
        confirmButton = findViewById(R.id.confirm_button)

        // 표시 형식 스피너 설정
        val formatAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("D-Day 형식", "D+ 형식", "전체 날짜", "남은 시간")
        )
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        displayFormatSpinner.adapter = formatAdapter
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

        // 이벤트 스피너 설정
        val eventNames = events.map { it.title }
        val eventAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            eventNames
        )
        eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        eventSpinner.adapter = eventAdapter
    }

    private fun setupListeners() {
        // 이벤트 선택
        eventSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val events = prefs.loadEvents()
                selectedEvent = events.getOrNull(position)
                updatePreview()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 표시 형식 변경
        displayFormatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updatePreview()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 배경색 변경
        bgColorSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updatePreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 텍스트 색상 변경
        textColorSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updatePreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 폰트 크기 변경
        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updatePreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 확인 버튼
        confirmButton.setOnClickListener {
            saveConfiguration()
        }
    }

    private fun updatePreview() {
        val event = selectedEvent ?: return

        // 선택된 형식으로 이벤트 업데이트
        val displayFormat = when (displayFormatSpinner.selectedItemPosition) {
            0 -> DisplayFormat.D_DAY
            1 -> DisplayFormat.D_PLUS
            2 -> DisplayFormat.FULL_DATE
            3 -> DisplayFormat.REMAINING
            else -> DisplayFormat.D_DAY
        }

        val updatedEvent = event.copy(
            displayFormat = displayFormat
        )

        // 프리뷰 업데이트
        previewTitle.text = updatedEvent.title
        previewDday.text = DdayCalculator.getDisplayText(updatedEvent)

        // 색상 적용
        val bgColor = Color.argb(
            255,
            bgColorSeekBar.progress,
            bgColorSeekBar.progress,
            bgColorSeekBar.progress
        )
        val textColor = Color.argb(
            255,
            textColorSeekBar.progress,
            textColorSeekBar.progress,
            textColorSeekBar.progress
        )

        previewContainer.setBackgroundColor(bgColor)
        previewTitle.setTextColor(textColor)
        previewDday.setTextColor(textColor)

        // 폰트 크기 적용 (12~32sp)
        val fontSize = 12f + (fontSizeSeekBar.progress / 5f)
        previewDday.textSize = fontSize
    }

    private fun saveConfiguration() {
        val event = selectedEvent ?: return

        // 설정 저장
        prefs.saveWidgetEvent(appWidgetId, event.id)
        
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

        val fontSize = 12f + (fontSizeSeekBar.progress / 5f)
        prefs.saveFontSize(appWidgetId, fontSize)

        // 위젯 업데이트
        val appWidgetManager = AppWidgetManager.getInstance(this)
        DdayWidgetProvider().onUpdate(
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
}
