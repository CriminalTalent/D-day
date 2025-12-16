package com.example.ddaywidget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 이벤트 목록을 표시하는 RecyclerView 어댑터
 */
class EventAdapter(
    private val events: List<Event>,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.event_title)
        val ddayTextView: TextView = itemView.findViewById(R.id.event_dday)
        val detailTextView: TextView = itemView.findViewById(R.id.event_detail)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.titleTextView.text = event.title
        holder.ddayTextView.text = DdayCalculator.getDisplayText(event)
        holder.detailTextView.text = DdayCalculator.getDetailedText(event.targetDate)

        holder.editButton.setOnClickListener {
            onEditClick(event)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(event)
        }
    }

    override fun getItemCount(): Int = events.size
}
