package com.shejan.pdfbox_pdfeditor.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.ap.pdf.box.R;
import com.ap.pdf.box.databinding.FragmentHomeBinding;
import com.shejan.pdfbox_pdfeditor.model.RecentFile;
import com.shejan.pdfbox_pdfeditor.model.Tool;
import com.shejan.pdfbox_pdfeditor.ui.adapters.RecentFilesAdapter;
import com.shejan.pdfbox_pdfeditor.ui.adapters.ToolCardsAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private RecentFilesAdapter recentFilesAdapter;
    private ToolCardsAdapter toolCardsAdapter;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleFilePicked(uri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecentFilesList();
        setupQuickToolsGrid();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecentFilesList() {
        recentFilesAdapter = new RecentFilesAdapter(new RecentFilesAdapter.OnFileClickListener() {
            @Override
            public void onFileClick(RecentFile file) {
                navigateToViewer(Uri.parse(file.getFilePath()));
            }

            @Override
            public void onMoreClick(View view, RecentFile file) {
                showRecentFileMenu(view, file);
            }
        });
        binding.rvRecentFiles.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecentFiles.setAdapter(recentFilesAdapter);
    }

    private void setupQuickToolsGrid() {
        List<Tool> tools = new ArrayList<>();
        tools.add(new Tool("compress", getString(R.string.tool_compress), getString(R.string.tool_desc_compress), R.drawable.ic_compress));
        tools.add(new Tool("merge", getString(R.string.tool_merge), getString(R.string.tool_desc_merge), R.drawable.ic_merge));
        tools.add(new Tool("text_extraction", getString(R.string.tool_text_extraction), getString(R.string.tool_desc_text_extraction), R.drawable.ic_text_extraction));
        tools.add(new Tool("pdf_to_image", getString(R.string.tool_pdf_to_image), getString(R.string.tool_desc_pdf_to_image), R.drawable.ic_pdf_to_image));

        toolCardsAdapter = new ToolCardsAdapter(tools, tool -> {
            switch (tool.getId()) {
                case "merge":
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_merge);
                    break;
                case "split":
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_split);
                    break;
                case "compress":
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_compress);
                    break;
                case "rotate":
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_rotate);
                    break;
                case "lock":
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_lock_unlock);
                    break;
                case "pdf_to_image":
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_pdf_to_image);
                    break;
                case "text_extraction":
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_text_extraction);
                    break;


                default:
                    Snackbar.make(binding.getRoot(), "Opening " + tool.getName(), Snackbar.LENGTH_SHORT).show();
                    break;
            }
        });

        binding.rvQuickTools.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvQuickTools.setAdapter(toolCardsAdapter);
    }

    private void setupClickListeners() {
        binding.btnOpenPdf.setOnClickListener(v -> openFilePicker());

        binding.btnPdfTools.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_tools);
            }
        });

        binding.btnToolsSeeAll.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_tools);
            }
        });

        binding.btnRecentSeeAll.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });

        binding.tvRecentFilesHeader.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });


        binding.btnSearch.setOnClickListener(v -> {
            boolean isVisible = binding.cardSearch.getVisibility() == View.VISIBLE;
            binding.cardSearch.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        binding.btnSearchClose.setOnClickListener(v -> {
            binding.etSearch.setText("");
            binding.cardSearch.setVisibility(View.GONE);
        });

        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                if (query.isEmpty()) {
                    observeViewModel(); // Default list
                } else {
                    viewModel.searchRecentFiles(query).observe(getViewLifecycleOwner(), recentFiles -> {
                        recentFilesAdapter.submitList(recentFiles);
                    });
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }


    private void observeViewModel() {
        viewModel.getRecentFiles().observe(getViewLifecycleOwner(), recentFiles -> {
            if (recentFiles == null || recentFiles.isEmpty()) {
                binding.rvRecentFiles.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvRecentFiles.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                recentFilesAdapter.submitList(recentFiles);
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        filePickerLauncher.launch(intent);
    }

    private void handleFilePicked(Uri uri) {
        // Take persistable URI permission to access the file later from recents
        try {
            getContext().getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        String fileName = "Unknown PDF";
        long fileSize = 0;

        try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIndex != -1) fileName = cursor.getString(nameIndex);
                if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex);
            }
        }

        String sizeStr = formatFileSize(fileSize);
        RecentFile recentFile = new RecentFile(fileName, uri.toString(), sizeStr, System.currentTimeMillis());
        viewModel.insertRecentFile(recentFile);

        navigateToViewer(uri);
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void showRecentFileMenu(View view, RecentFile file) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenu().add(getString(R.string.menu_open));
        popup.getMenu().add(getString(R.string.menu_share));
        popup.getMenu().add(getString(R.string.menu_delete));

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals(getString(R.string.menu_open))) {
                navigateToViewer(Uri.parse(file.getFilePath()));
            } else if (item.getTitle().equals(getString(R.string.menu_share))) {
                shareFile(file);
            } else if (item.getTitle().equals(getString(R.string.menu_delete))) {
                viewModel.deleteRecentFile(file);
            }
            return true;
        });
        popup.show();
    }

    private void navigateToViewer(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri.toString());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_home_to_viewer, bundle);
    }


    private void shareFile(RecentFile file) {
        Uri uri = Uri.parse(file.getFilePath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");

        if (uri.getScheme().equals("file")) {
            java.io.File fileObj = new java.io.File(uri.getPath());
            uri = androidx.core.content.FileProvider.getUriForFile(getContext(),
                getContext().getPackageName() + ".fileprovider", fileObj);
        }

        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share PDF"));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
