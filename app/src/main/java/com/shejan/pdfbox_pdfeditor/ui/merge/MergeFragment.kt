package com.shejan.pdfbox_pdfeditor.ui.merge

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shejan.pdfbox_pdfeditor.core.PdfProcessor
import com.shejan.pdfbox_pdfeditor.databinding.FragmentMergeBinding
import com.shejan.pdfbox_pdfeditor.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MergeFragment : Fragment() {

    private var _binding: FragmentMergeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: MergeAdapter
    private val selectedFiles = mutableListOf<SelectedFile>()

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                if (data.clipData != null) {
                    for (i in 0 until data.clipData!!.itemCount) {
                        addFile(data.clipData!!.getItemAt(i).uri)
                    }
                } else if (data.data != null) {
                    addFile(data.data!!)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMergeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        updateUI()
    }

    private fun setupRecyclerView() {
        adapter = MergeAdapter { file ->
            selectedFiles.remove(file)
            adapter.submitList(selectedFiles.toList())
            updateUI()
        }
        
        binding.rvMergeFiles.layoutManager = LinearLayoutManager(context)
        binding.rvMergeFiles.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(binding.rvMergeFiles)
    }

    private fun setupClickListeners() {

        binding.btnAddFile.setOnClickListener { openFilePicker() }
        binding.btnMerge.setOnClickListener { startMerge() }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        filePickerLauncher.launch(intent)
    }

    private fun addFile(uri: Uri) {
        val fileName = getFileName(uri)
        val fileSize = getFileSize(uri)
        selectedFiles.add(SelectedFile(uri, fileName, fileSize))
        adapter.submitList(selectedFiles.toList())
        updateUI()
    }

    private fun updateUI() {
        if (selectedFiles.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvMergeFiles.visibility = View.GONE
            binding.btnMerge.isEnabled = false
            binding.btnMerge.alpha = 0.5f
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvMergeFiles.visibility = View.VISIBLE
            binding.btnMerge.isEnabled = selectedFiles.size >= 2
            binding.btnMerge.alpha = if (selectedFiles.size >= 2) 1.0f else 0.5f
        }
    }

    private fun startMerge() {
        if (selectedFiles.size < 2) return
        
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnMerge.isEnabled = false
            
            try {
                val inputStreams = selectedFiles.map { context?.contentResolver?.openInputStream(it.uri)!! }
                val outputFolder = File(context?.getExternalFilesDir(null), "Merged")
                if (!outputFolder.exists()) outputFolder.mkdirs()
                
                val outputFile = File(outputFolder, "merged_${System.currentTimeMillis()}.pdf")
                val outputStream = FileOutputStream(outputFile)
                
                PdfProcessor.mergePdfs(inputStreams, outputStream)
                
                Toast.makeText(context, "PDF Merged Successfully!", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Merge Failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnMerge.isEnabled = true
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
