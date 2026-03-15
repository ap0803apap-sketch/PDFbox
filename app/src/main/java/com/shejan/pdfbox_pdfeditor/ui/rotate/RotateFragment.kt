package com.shejan.pdfbox_pdfeditor.ui.rotate

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
import com.shejan.pdfbox_pdfeditor.databinding.FragmentRotateBinding
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class RotateFragment : Fragment() {

    private var _binding: FragmentRotateBinding? = null
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
        _binding = FragmentRotateBinding.inflate(inflater, container, false)
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
        binding.btnRotate.setOnClickListener { startRotate() }
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
            binding.txtFilePages.text = "Error reading file"
        }

        binding.cardSelectedFile.visibility = View.VISIBLE
        binding.btnSelectFile.visibility = View.GONE
        binding.btnRotate.isEnabled = true
    }

    private fun removeFile() {
        selectedUri = null
        binding.cardSelectedFile.visibility = View.GONE
        binding.btnSelectFile.visibility = View.VISIBLE
        binding.btnRotate.isEnabled = false
    }

    private fun startRotate() {
        val uri = selectedUri ?: return
        val degrees = when (binding.toggleRotation.checkedButtonId) {
            binding.btn90.id -> 90
            binding.btn180.id -> 180
            binding.btn270.id -> 270
            else -> 90
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRotate.isEnabled = false
            
            try {
                val inputStream = context?.contentResolver?.openInputStream(uri)!!
                val outputFolder = File(context?.getExternalFilesDir(null), "Rotated")
                if (!outputFolder.exists()) outputFolder.mkdirs()
                
                val outputFile = File(outputFolder, "rotated_${System.currentTimeMillis()}.pdf")
                val outputStream = FileOutputStream(outputFile)
                
                PdfProcessor.rotatePdf(inputStream, outputStream, degrees)
                
                Toast.makeText(context, "PDF Rotated Successfully!", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Rotation Failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnRotate.isEnabled = true
            }
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
