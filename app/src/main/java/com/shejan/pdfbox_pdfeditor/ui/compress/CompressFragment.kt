package com.shejan.pdfbox_pdfeditor.ui.compress

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
import com.shejan.pdfbox_pdfeditor.databinding.FragmentCompressBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class CompressFragment : Fragment() {

    private var _binding: FragmentCompressBinding? = null
    private val binding get() = _binding!!
    
    private var selectedUri: Uri? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleFilePicked(uri)
            }
        }
    }

    private var compressedFile: File? = null
    private var originalSize: Long = 0

    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { destUri ->
            saveFileToUri(destUri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSelectFile.setOnClickListener { openFilePicker() }
        binding.btnRemoveFile.setOnClickListener { removeFile() }
        binding.btnCompress.setOnClickListener { startCompress() }
        binding.btnDownload.setOnClickListener { downloadFile() }
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
        originalSize = getFileSizeBytes(uri)
        binding.txtFileSize.text = formatFileSize(originalSize)

        binding.cardSelectedFile.visibility = View.VISIBLE
        binding.btnSelectFile.visibility = View.GONE
        binding.btnCompress.isEnabled = true
        binding.resultContainer.visibility = View.GONE
    }

    private fun removeFile() {
        selectedUri = null
        binding.cardSelectedFile.visibility = View.GONE
        binding.btnSelectFile.visibility = View.VISIBLE
        binding.btnCompress.isEnabled = false
        binding.resultContainer.visibility = View.GONE
        compressedFile = null
    }

    private fun startCompress() {
        val uri = selectedUri ?: return
        val quality = when (binding.toggleCompression.checkedButtonId) {
            binding.btnLow.id -> 0.8f
            binding.btnMedium.id -> 0.5f
            binding.btnHigh.id -> 0.2f
            else -> 0.5f
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnCompress.isEnabled = false
            
            try {
                val inputStream = context?.contentResolver?.openInputStream(uri)!!
                val outputFolder = File(context?.getExternalFilesDir(null), "Compressed")
                if (!outputFolder.exists()) outputFolder.mkdirs()
                
                val outputFile = File(outputFolder, "compressed_${System.currentTimeMillis()}.pdf")
                val outputStream = FileOutputStream(outputFile)
                
                PdfProcessor.compressPdf(inputStream, outputStream, quality)
                
                compressedFile = outputFile
                showResult(outputFile)
                
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Compression Failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnCompress.isEnabled = true
            }
        }
    }

    private fun showResult(file: File) {
        val compressedSize = file.length()
        binding.tvOriginalSize.text = formatFileSize(originalSize)
        binding.tvCompressedSize.text = formatFileSize(compressedSize)
        
        val savings = originalSize - compressedSize
        val savingsPercent = if (originalSize > 0) (savings * 100 / originalSize) else 0
        binding.tvSavings.text = "You saved ${formatFileSize(savings)} (${savingsPercent}%)"
        
        binding.resultContainer.visibility = View.VISIBLE
        Toast.makeText(context, "PDF Compressed Successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun downloadFile() {
        val fileName = "compressed_${binding.txtFileName.text}"
        saveFileLauncher.launch(fileName)
    }

    private fun saveFileToUri(destUri: Uri) {
        lifecycleScope.launch {
            try {
                val sourceFile = compressedFile ?: return@launch
                context?.contentResolver?.openOutputStream(destUri)?.use { output ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, "File saved successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileSizeBytes(uri: Uri): Long {
        var size: Long = 0
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) size = it.getLong(sizeIndex)
            }
        }
        return size
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

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
