package com.shejan.pdfbox_pdfeditor.ui.security

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
import com.shejan.pdfbox_pdfeditor.databinding.FragmentLockUnlockBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class LockUnlockFragment : Fragment() {

    private var _binding: FragmentLockUnlockBinding? = null
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
        _binding = FragmentLockUnlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {

        binding.btnSelectFile.setOnClickListener { openFilePicker() }
        binding.btnRemoveFile.setOnClickListener { removeFile() }
        binding.btnApply.setOnClickListener { startAction() }
        
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == binding.btnLockMode.id) {
                    binding.tilPassword.hint = "New Password"
                    binding.btnApply.text = "Lock Now"
                } else {
                    binding.tilPassword.hint = "Current Password"
                    binding.btnApply.text = "Unlock Now"
                }
            }
        }
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
        binding.txtFileSize.text = getFileSize(uri)

        binding.cardSelectedFile.visibility = View.VISIBLE
        binding.btnSelectFile.visibility = View.GONE
        binding.btnApply.isEnabled = true
    }

    private fun removeFile() {
        selectedUri = null
        binding.cardSelectedFile.visibility = View.GONE
        binding.btnSelectFile.visibility = View.VISIBLE
        binding.btnApply.isEnabled = false
    }

    private fun startAction() {
        val uri = selectedUri ?: return
        val password = binding.etPassword.text.toString()
        val isLockMode = binding.toggleMode.checkedButtonId == binding.btnLockMode.id
        
        if (password.isEmpty()) {
            Toast.makeText(context, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnApply.isEnabled = false
            
            try {
                val inputStream = context?.contentResolver?.openInputStream(uri)!!
                val outputFolder = File(context?.getExternalFilesDir(null), "Security")
                if (!outputFolder.exists()) outputFolder.mkdirs()
                
                val outputFile = File(outputFolder, "${if (isLockMode) "locked" else "unlocked"}_${System.currentTimeMillis()}.pdf")
                val outputStream = FileOutputStream(outputFile)
                
                if (isLockMode) {
                    PdfProcessor.protectPdf(inputStream, outputStream, password)
                } else {
                    PdfProcessor.unprotectPdf(inputStream, outputStream, password)
                }
                
                Toast.makeText(context, "PDF ${if (isLockMode) "Locked" else "Unlocked"} Successfully!", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Action Failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnApply.isEnabled = true
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

    private fun getFileSize(uri: Uri): String {
        var size: Long = 0
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) size = it.getLong(sizeIndex)
            }
        }
        return formatFileSize(size)
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
