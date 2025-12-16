package com.example.ddaywidget

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * 메인 Activity - 이벤트 관리
 */
class MainActivity : AppCompatActivity() {

    private lateinit var prefs: WidgetPreferences
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var addEventButton: Button
    private var events = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = WidgetPreferences(this)
        
        initViews()
        loadEvents()
    }

    private fun initViews() {
        eventRecyclerView = findViewById(R.id.event_recycler_view)
        addEventButton = findViewById(R.id.add_event_button)

        // RecyclerView 설정
        eventAdapter = EventAdapter(
            events = events,
            onEditClick = { event -> showEditEventDialog(event) },
            onDeleteClick = { event -> deleteEvent(event) }
        )
        eventRecyclerView.layoutManager = LinearLayoutManager(this)
        eventRecyclerView.adapter = eventAdapter

        // 이벤트 추가 버튼
        addEventButton.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun loadEvents() {
        events.clear()
        events.addAll(prefs.loadEvents())
        eventAdapter.notifyDataSetChanged()
    }

    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.event_title_edit)
        val dateButton = dialogView.findViewById<Button>(R.id.select_date_button)
        val timeButton = dialogView.findViewById<Button>(R.id.select_time_button)

        var selectedCalendar = Calendar.getInstance()

        dateButton.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedCalendar.set(year, month, dayOfMonth)
                    dateButton.text = "${year}년 ${month + 1}월 ${dayOfMonth}일"
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeButton.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedCalendar.set(Calendar.MINUTE, minute)
                    timeButton.text = String.format("%02d:%02d", hourOfDay, minute)
                },
                selectedCalendar.get(Calendar.HOUR_OF_DAY),
                selectedCalendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("이벤트 추가")
            .setView(dialogView)
            .setPositiveButton("추가") { _, _ ->
                val title = titleEditText.text.toString()
                if (title.isNotBlank()) {
                    val newEvent = Event(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        targetDate = selectedCalendar.timeInMillis
                    )
                    addEvent(newEvent)
                } else {
                    Toast.makeText(this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showEditEventDialog(event: Event) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.event_title_edit)
        val dateButton = dialogView.findViewById<Button>(R.id.select_date_button)
        val timeButton = dialogView.findViewById<Button>(R.id.select_time_button)

        // 기존 값 설정
        titleEditText.setText(event.title)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = event.targetDate
        }

        dateButton.text = String.format(
            "%d년 %d월 %d일",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        timeButton.text = String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        dateButton.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    dateButton.text = "${year}년 ${month + 1}월 ${dayOfMonth}일"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeButton.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    timeButton.text = String.format("%02d:%02d", hourOfDay, minute)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("이벤트 수정")
            .setView(dialogView)
            .setPositiveButton("수정") { _, _ ->
                val title = titleEditText.text.toString()
                if (title.isNotBlank()) {
                    val updatedEvent = event.copy(
                        title = title,
                        targetDate = calendar.timeInMillis
                    )
                    updateEvent(updatedEvent)
                } else {
                    Toast.makeText(this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun addEvent(event: Event) {
        events.add(event)
        prefs.saveEvents(events)
        eventAdapter.notifyItemInserted(events.size - 1)
        Toast.makeText(this, "이벤트가 추가되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun updateEvent(event: Event) {
        val index = events.indexOfFirst { it.id == event.id }
        if (index != -1) {
            events[index] = event
            prefs.saveEvents(events)
            eventAdapter.notifyItemChanged(index)
            Toast.makeText(this, "이벤트가 수정되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteEvent(event: Event) {
        AlertDialog.Builder(this)
            .setTitle("이벤트 삭제")
            .setMessage("'${event.title}' 이벤트를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                val index = events.indexOfFirst { it.id == event.id }
                if (index != -1) {
                    events.removeAt(index)
                    prefs.saveEvents(events)
                    eventAdapter.notifyItemRemoved(index)
                    Toast.makeText(this, "이벤트가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
