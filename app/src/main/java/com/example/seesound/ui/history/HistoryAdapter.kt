package com.example.seesound.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.seesound.data.local.entity.NoiseRecord
import com.example.seesound.databinding.ItemNoiseRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<NoiseRecord, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoiseRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemNoiseRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        fun bind(record: NoiseRecord) {
            binding.tvDbValue.text = String.format(Locale.getDefault(), "%.1f dB", record.dbValue)
            binding.tvTimestamp.text = dateFormat.format(Date(record.timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NoiseRecord>() {
        override fun areItemsTheSame(oldItem: NoiseRecord, newItem: NoiseRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoiseRecord, newItem: NoiseRecord): Boolean {
            return oldItem == newItem
        }
    }
}
