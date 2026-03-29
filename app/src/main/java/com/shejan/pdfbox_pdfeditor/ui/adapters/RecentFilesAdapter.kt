package com.shejan.pdfbox_pdfeditor.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ap.pdf.box.databinding.ItemRecentFileBinding
import com.shejan.pdfbox_pdfeditor.model.RecentFile

class RecentFilesAdapter(private val listener: OnFileClickListener) :
    ListAdapter<RecentFile, RecentFilesAdapter.RecentFileViewHolder>(RecentFileDiffCallback()) {

    interface OnFileClickListener {
        fun onFileClick(file: RecentFile)
        fun onMoreClick(view: View, file: RecentFile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentFileViewHolder {
        val binding = ItemRecentFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentFileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentFileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecentFileViewHolder(private val binding: ItemRecentFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: RecentFile) {
            binding.txtFileName.text = file.fileName
            binding.txtFileInfo.text = "${file.fileSize} • ${android.text.format.DateFormat.format("dd MMM yyyy", file.lastOpened)}"

            binding.root.setOnClickListener { listener.onFileClick(file) }
            binding.btnMore.setOnClickListener { listener.onMoreClick(it, file) }
        }
    }

    class RecentFileDiffCallback : DiffUtil.ItemCallback<RecentFile>() {
        override fun areItemsTheSame(oldItem: RecentFile, newItem: RecentFile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentFile, newItem: RecentFile): Boolean {
            return oldItem == newItem
        }
    }
}