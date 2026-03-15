package com.shejan.pdfbox_pdfeditor.ui.split

import android.app.Activity
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
import com.shejan.pdfbox_pdfeditor.core.PdfProcessor
import com.shejan.pdfbox_pdfeditor.databinding.FragmentSplitBinding
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class SplitFragment : Fragment() {

    private var _binding: FragmentSplitBinding? = null
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
        _binding = FragmentSplitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSelectFile.setOnClickListener { openFilePicker() }
        binding.btnRemoveFile.setOnClickListener { removeFile() }
        binding.btnSplit.setOnClickListener { startSplit() }
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
        
        try {
            val inputStream = context?.contentResolver?.openInputStream(uri)
            val document = PDDocument.load(inputStream)
            binding.txtFilePages.text = "${document.numberOfPages} Pages"
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.txtFilePages.text = "Error reading pages"
        }

        binding.cardSelectedFile.visibility = View.VISIBLE
        binding.btnSelectFile.visibility = View.GONE
        binding.btnSplit.isEnabled = true
    }

    private fun removeFile() {
        selectedUri = null
        binding.cardSelectedFile.visibility = View.GONE
        binding.btnSelectFile.visibility = View.VISIBLE
        binding.btnSplit.isEnabled = false
    }

    private fun startSplit() {
        val uri = selectedUri ?: return
        val rangeStr = binding.etPageRanges.text.toString()
        
        if (rangeStr.isBlank()) {
            Toast.makeText(context, "Please enter page ranges", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSplit.isEnabled = false
            
            try {
                val ranges = parseRanges(rangeStr)
                if (ranges.isEmpty()) {
                    Toast.makeText(context, "Invalid range format", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val inputStream = context?.contentResolver?.openInputStream(uri)!!
                val splitDocs = PdfProcessor.splitPdf(inputStream, ranges)
                
                val outputFolder = File(context?.getExternalFilesDir(null), "Split")
                if (!outputFolder.exists()) outputFolder.mkdirs()
                
                splitDocs.forEachIndexed { index, doc ->
                    val outputFile = File(outputFolder, "split_${System.currentTimeMillis()}_part${index + 1}.pdf")
                    val outputStream = FileOutputStream(outputFile)
                    doc.save(outputStream)
                    doc.close()
                    outputStream.close()
                }
                
                Toast.makeText(context, "PDF Split Successfully! (${splitDocs.size} files)", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Split Failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSplit.isEnabled = true
            }
        }
    }

    private fun parseRanges(rangeStr: String): List<IntRange> {
        val ranges = mutableListOf<IntRange>()
        val parts = rangeStr.split(",").map { it.trim() }
        
        for (part in parts) {
            try {
                if (part.contains("-")) {
                    val subParts = part.split("-").map { it.trim().toInt() }
                    if (subParts.size == 2) {
                        // PDFBox is 0-indexed for pages in some methods, but users use 1-indexed.
                        // I'll assume users use 1-indexed.
                        ranges.add(IntRange(subParts[0] - 1, subParts[1] - 1))
                    }
                } else {
                    val page = part.toInt()
                    ranges.add(IntRange(page - 1, page - 1))
                }
            } catch (e: Exception) {
                // Ignore invalid parts
            }
        }
        return ranges
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
