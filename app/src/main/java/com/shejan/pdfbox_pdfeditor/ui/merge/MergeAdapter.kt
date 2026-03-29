package com.shejan.pdfbox_pdfeditor.ui.merge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ap.pdf.box.databinding.ItemMergeFileBinding
import java.util.*

class MergeAdapter(
    private val onRemoveClick: (SelectedFile) -> Unit
) : RecyclerView.Adapter<MergeAdapter.ViewHolder>() {

    private val files = mutableListOf<SelectedFile>()

    fun submitList(newFiles: List<SelectedFile>) {
        files.clear()
        files.addAll(newFiles)
        notifyDataSetChanged()
    }

    fun getFiles(): List<SelectedFile> = files

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(files, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(files, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        // Refresh numbers for all items affected by the move
        notifyItemRangeChanged(Math.min(fromPosition, toPosition), files.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMergeFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position], position)
    }

    override fun getItemCount(): Int = files.size

    inner class ViewHolder(private val binding: ItemMergeFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: SelectedFile, position: Int) {
            binding.txtFileNumber.text = (position + 1).toString()
            binding.txtFileName.text = file.name
            binding.txtFileSize.text = file.size
            binding.btnRemove.setOnClickListener { onRemoveClick(file) }
        }
    }
}
