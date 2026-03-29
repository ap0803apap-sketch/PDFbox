package com.shejan.pdfbox_pdfeditor.ui.viewer

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ap.pdf.box.databinding.FragmentViewerBinding

class ViewerFragment : Fragment() {
    private var _binding: FragmentViewerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.getString("uri")?.let { uriString ->
            val uri = Uri.parse(uriString)
            loadPdf(uri)
        }
    }

    private fun loadPdf(uri: Uri) {
        binding.pdfView.fromUri(uri)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableAnnotationRendering(true)
            .enableAntialiasing(true)
            .spacing(10)
            .load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

