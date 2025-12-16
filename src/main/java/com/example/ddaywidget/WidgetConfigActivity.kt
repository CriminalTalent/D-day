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
    private var selectedImageUri: String? = null
    private var selectedStickerId: Int? = null
    private var selectedFrameStyle: FrameStyle = FrameStyle.NONE
    private var selectedTheme: WidgetTheme = WidgetTheme.LIGHT

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
    
    // 새로운 UI 컴포넌트
    private lateinit var selectImageButton: Button
    private lateinit var selectStickerButton: Button
    private lateinit var frameStyleSpinner: Spinner
    private lateinit var themeSpinner: Spinner
    private lateinit var previewBackgroundImage: ImageView
    private lateinit var previewSticker: ImageView

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
        
        // 새로운 UI 컴포넌트
        selectImageButton = findViewById(R.id.select_image_button)
        selectStickerButton = findViewById(R.id.select_sticker_button)
        frameStyleSpinner = findViewById(R.id.frame_style_spinner)
        themeSpinner = findViewById(R.id.theme_spinner)
        previewBackgroundImage = findViewById(R.id.preview_background_image)
        previewSticker = findViewById(R.id.preview_sticker)

        // 표시 형식 스피너 설정
        val formatAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("D-Day 형식", "D+ 형식", "전체 날짜", "남은 시간")
        )
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        displayFormatSpinner.adapter = formatAdapter
        
        // 프레임 스타일 스피너 설정
        val frameAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("프레임 없음", "둥근 모서리", "원형", "하트", "별", "폴라로이드")
        )
        frameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frameStyleSpinner.adapter = frameAdapter
        
        // 테마 스피너 설정
        val themeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("밝은 테마", "어두운 테마", "파스텔", "선명한 색상", "미니멀")
        )
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = themeAdapter
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
        
        // 프레임 스타일 변경
        frameStyleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedFrameStyle = when (position) {
                    0 -> FrameStyle.NONE
                    1 -> FrameStyle.ROUND_CORNER
                    2 -> FrameStyle.CIRCLE
                    3 -> FrameStyle.HEART
                    4 -> FrameStyle.STAR
                    5 -> FrameStyle.POLAROID
                    else -> FrameStyle.NONE
                }
                updatePreview()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
                updatePreview()
                Toast.makeText(this, "${sticker.name} 선택됨", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("스티커 제거") { _, _ ->
                selectedStickerId = null
                updatePreview()
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
                updatePreview()
                Toast.makeText(this, "배경 이미지 선택됨", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
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

        // 배경 이미지 적용
        if (selectedImageUri != null) {
            try {
                val bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    android.net.Uri.parse(selectedImageUri)
                )
                previewBackgroundImage.setImageBitmap(bitmap)
                previewBackgroundImage.visibility = android.view.View.VISIBLE
                // 이미지 있을 때는 배경색 적용 안함
                previewContainer.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            } catch (e: Exception) {
                previewBackgroundImage.visibility = android.view.View.GONE
                applyBackgroundColor()
            }
        } else {
            previewBackgroundImage.visibility = android.view.View.GONE
            applyBackgroundColor()
        }
        
        // 스티커 적용
        if (selectedStickerId != null) {
            val stickerItem = StickerResources.getStickerById(selectedStickerId!!)
            if (stickerItem != null) {
                previewSticker.setImageResource(stickerItem.resourceId)
                previewSticker.visibility = android.view.View.VISIBLE
            } else {
                previewSticker.visibility = android.view.View.GONE
            }
        } else {
            previewSticker.visibility = android.view.View.GONE
        }

        // 텍스트 색상 적용
        val textColor = Color.argb(
            255,
            textColorSeekBar.progress,
            textColorSeekBar.progress,
            textColorSeekBar.progress
        )
        previewTitle.setTextColor(textColor)
        previewDday.setTextColor(textColor)

        // 폰트 크기 적용 (12~32sp)
        val fontSize = 12f + (fontSizeSeekBar.progress / 5f)
        previewDday.textSize = fontSize
        
        // 테마 적용 (배경 이미지가 없을 때만)
        if (selectedImageUri == null) {
            applyThemeToPreview()
        }
    }
    
    /**
     * 배경색 적용
     */
    private fun applyBackgroundColor() {
        val bgColor = Color.argb(
            255,
            bgColorSeekBar.progress,
            bgColorSeekBar.progress,
            bgColorSeekBar.progress
        )
        previewContainer.setBackgroundColor(bgColor)
    }
    
    /**
     * 테마를 미리보기에 적용
     */
    private fun applyThemeToPreview() {
        val (bgColor, textColor) = when (selectedTheme) {
            WidgetTheme.LIGHT -> Pair(
                Color.rgb(255, 255, 255),
                Color.rgb(0, 0, 0)
            )
            WidgetTheme.DARK -> Pair(
                Color.rgb(0, 0, 0),
                Color.rgb(255, 255, 255)
            )
            WidgetTheme.PASTEL -> Pair(
                Color.rgb(255, 245, 245),
                Color.rgb(74, 74, 74)
            )
            WidgetTheme.VIBRANT -> Pair(
                Color.rgb(255, 107, 107),
                Color.rgb(255, 255, 255)
            )
            WidgetTheme.MINIMAL -> Pair(
                Color.rgb(245, 245, 245),
                Color.rgb(51, 51, 51)
            )
        }
        
        previewContainer.setBackgroundColor(bgColor)
        previewTitle.setTextColor(textColor)
        previewDday.setTextColor(textColor)
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
        
        // 새로운 설정 저장
        prefs.saveBackgroundImage(appWidgetId, selectedImageUri)
        prefs.saveStickerId(appWidgetId, selectedStickerId)
        prefs.saveFrameStyle(appWidgetId, selectedFrameStyle)
        prefs.saveTheme(appWidgetId, selectedTheme)

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
