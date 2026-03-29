package com.shejan.pdfbox_pdfeditor.ui.extraction

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ap.pdf.box.databinding.FragmentTextExtractionBinding
import com.shejan.pdfbox_pdfeditor.core.PdfProcessor
import kotlinx.coroutines.launch

class TextExtractionFragment : Fragment() {

    private var _binding: FragmentTextExtractionBinding? = null
    private val binding get() = _binding!!
    
    private var selectedUri: Uri? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleFilePicked(uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTextExtractionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {

        binding.btnSelectFile.setOnClickListener { openFilePicker() }
        binding.btnRemoveFile.setOnClickListener { removeFile() }
        binding.btnExtract.setOnClickListener { startExtraction() }
        binding.btnCopy.setOnClickListener { copyToClipboard() }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        filePickerLauncher.launch(intent)
    }

    private fun handleFilePicked(uri: Uri) {
        selectedUri = uri
        binding.txtFileName.text = getFileName(uri)
        binding.cardSelectedFile.visibility = View.VISIBLE
        binding.btnSelectFile.visibility = View.GONE
        binding.btnExtract.isEnabled = true
    }

    private fun removeFile() {
        selectedUri = null
        binding.cardSelectedFile.visibility = View.GONE
        binding.btnSelectFile.visibility = View.VISIBLE
        binding.btnExtract.isEnabled = false
    }

    private fun startExtraction() {
        val uri = selectedUri ?: return

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.selectionContainer.visibility = View.GONE
            
            try {
                val inputStream = context?.contentResolver?.openInputStream(uri)!!
                val text = PdfProcessor.extractText(inputStream)
                
                binding.txtExtracted.text = if (text.isNullOrBlank()) "No text found in this PDF." else text
                binding.scrollText.visibility = View.VISIBLE
                binding.btnCopy.visibility = View.VISIBLE
                
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Extraction Failed: ${e.message}", Toast.LENGTH_LONG).show()
                binding.selectionContainer.visibility = View.VISIBLE
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun copyToClipboard() {
        val text = binding.txtExtracted.text.toString()
        if (text.isNotEmpty()) {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Extracted Text", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "Unknown.pdf"
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) name = it.getString(nameIndex)
            }
        }
        return name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
