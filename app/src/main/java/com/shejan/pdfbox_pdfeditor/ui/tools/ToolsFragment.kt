package com.shejan.pdfbox_pdfeditor.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shejan.pdfbox_pdfeditor.R
import com.shejan.pdfbox_pdfeditor.databinding.FragmentToolsBinding

class ToolsFragment : Fragment() {
    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.cardMerge.setOnClickListener {
            findNavController().navigate(R.id.navigation_merge)
        }
        
        binding.cardSplit.setOnClickListener {
            findNavController().navigate(R.id.navigation_split)
        }

        binding.cardLock.setOnClickListener {
            findNavController().navigate(R.id.navigation_lock_unlock)
        }

        binding.cardCompress.setOnClickListener {
            findNavController().navigate(R.id.navigation_compress)
        }

        binding.cardPdfToImage.setOnClickListener {
            findNavController().navigate(R.id.navigation_pdf_to_image)
        }

        binding.cardTextExtraction.setOnClickListener {
            findNavController().navigate(R.id.navigation_text_extraction)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

