package com.shejan.pdfbox_pdfeditor.ui.recent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shejan.pdfbox_pdfeditor.R
import com.shejan.pdfbox_pdfeditor.databinding.FragmentRecentBinding
import com.shejan.pdfbox_pdfeditor.model.RecentFile
import com.shejan.pdfbox_pdfeditor.ui.home.RecentFilesAdapter

class RecentFragment : Fragment() {
    private var _binding: FragmentRecentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecentViewModel
    private lateinit var adapter: RecentFilesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(RecentViewModel::class.java)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = RecentFilesAdapter(object : RecentFilesAdapter.OnFileClickListener {
            override fun onFileClick(file: RecentFile) {
                navigateToViewer(Uri.parse(file.filePath))
            }

            override fun onMoreClick(view: View, file: RecentFile) {
                showRecentFileMenu(view, file)
            }
        })
        binding.rvAllRecent.layoutManager = LinearLayoutManager(context)
        binding.rvAllRecent.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.allRecentFiles.observe(viewLifecycleOwner) { recentFiles ->
            if (recentFiles == null || recentFiles.isEmpty()) {
                binding.rvAllRecent.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvAllRecent.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
                adapter.submitList(recentFiles)
            }
        }
    }

    private fun showRecentFileMenu(view: View, file: RecentFile) {
        val popup = PopupMenu(context, view)
        popup.menu.add(getString(R.string.menu_open))
        popup.menu.add(getString(R.string.menu_share))
        popup.menu.add(getString(R.string.menu_delete))

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                getString(R.string.menu_open) -> navigateToViewer(Uri.parse(file.filePath))
                getString(R.string.menu_share) -> shareFile(file)
                getString(R.string.menu_delete) -> viewModel.deleteRecentFile(file)
            }
            true
        }
        popup.show()
    }

    private fun navigateToViewer(uri: Uri) {
        val bundle = Bundle()
        bundle.putString("uri", uri.toString())
        findNavController().navigate(R.id.action_recent_to_viewer, bundle)
    }

    private fun shareFile(file: RecentFile) {
        var uri = Uri.parse(file.filePath)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"

        if (uri.scheme == "file") {
            val fileObj = java.io.File(uri.path!!)
            uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".fileprovider",
                fileObj
            )
        }

        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

