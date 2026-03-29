package com.shejan.pdfbox_pdfeditor.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ap.pdf.box.databinding.ItemPdfToolBinding

import com.shejan.pdfbox_pdfeditor.model.PdfTool

class PdfToolAdapter(
    private val tools: List<PdfTool>,
    private val onToolClick: (PdfTool) -> Unit
) : RecyclerView.Adapter<PdfToolAdapter.ToolViewHolder>() {

    inner class ToolViewHolder(private val binding: ItemPdfToolBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: PdfTool) {
            binding.toolName.text = tool.name
            binding.toolDescription.text = tool.description
            binding.toolIcon.setImageResource(tool.icon)

            binding.root.setOnClickListener {
                onToolClick(tool)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemPdfToolBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        holder.bind(tools[position])
    }

    override fun getItemCount(): Int = tools.size
}