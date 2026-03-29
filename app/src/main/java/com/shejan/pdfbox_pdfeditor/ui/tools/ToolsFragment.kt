package com.shejan.pdfbox_pdfeditor.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ap.pdf.box.R
import com.ap.pdf.box.databinding.FragmentToolsBinding
import com.shejan.pdfbox_pdfeditor.model.Tool
import com.shejan.pdfbox_pdfeditor.ui.adapters.ToolCardsAdapter

class ToolsFragment : Fragment() {
    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!
    private lateinit var toolCardsAdapter: ToolCardsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolsGrid()
    }

    private fun setupToolsGrid() {
        val tools = listOf(
            Tool("merge", getString(R.string.tool_merge), getString(R.string.tool_desc_merge), R.drawable.ic_merge),
            Tool("split", getString(R.string.tool_split), getString(R.string.tool_desc_split), R.drawable.ic_split),
            Tool("compress", getString(R.string.tool_compress), getString(R.string.tool_desc_compress), R.drawable.ic_compress),
            Tool("rotate", getString(R.string.tool_rotate), getString(R.string.tool_desc_rotate), R.drawable.ic_rotate),
            Tool("lock", getString(R.string.tool_lock_unlock), getString(R.string.tool_desc_lock_unlock), R.drawable.ic_lock),
            Tool("pdf_to_image", getString(R.string.tool_pdf_to_image), getString(R.string.tool_desc_pdf_to_image), R.drawable.ic_pdf_to_image),
            Tool("text_extraction", getString(R.string.tool_text_extraction), getString(R.string.tool_desc_text_extraction), R.drawable.ic_text_extraction)
        )

        toolCardsAdapter = ToolCardsAdapter(tools) { tool ->
            val actionId = when (tool.id) {
                "merge" -> R.id.navigation_merge
                "split" -> R.id.navigation_split
                "compress" -> R.id.navigation_compress
                "rotate" -> R.id.navigation_rotate
                "lock" -> R.id.navigation_lock_unlock
                "pdf_to_image" -> R.id.navigation_pdf_to_image
                "text_extraction" -> R.id.navigation_text_extraction
                else -> null
            }
            actionId?.let { findNavController().navigate(it) }
        }

        binding.rvTools.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = toolCardsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
