package com.shejan.pdfbox_pdfeditor.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ap.pdf.box.databinding.ItemRecentFileBinding;
import com.shejan.pdfbox_pdfeditor.model.RecentFile;

public class RecentFilesAdapter extends ListAdapter<RecentFile, RecentFilesAdapter.RecentFileViewHolder> {

    private final OnFileClickListener listener;

    public interface OnFileClickListener {
        void onFileClick(RecentFile file);
        void onMoreClick(View view, RecentFile file);
    }

    public RecentFilesAdapter(OnFileClickListener listener) {
        super(new DiffUtil.ItemCallback<RecentFile>() {
            @Override
            public boolean areItemsTheSame(@NonNull RecentFile oldItem, @NonNull RecentFile newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull RecentFile oldItem, @NonNull RecentFile newItem) {
                return oldItem.getFileName().equals(newItem.getFileName()) &&
                        oldItem.getFilePath().equals(newItem.getFilePath()) &&
                        oldItem.getLastOpened() == newItem.getLastOpened();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecentFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecentFileBinding binding = ItemRecentFileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RecentFileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentFileViewHolder holder, int position) {
        RecentFile file = getItem(position);
        holder.bind(file);
    }

    class RecentFileViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecentFileBinding binding;

        public RecentFileViewHolder(ItemRecentFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RecentFile file) {
            binding.txtFileName.setText(file.getFileName());
            binding.txtFileInfo.setText(file.getFileSize() + " • " + android.text.format.DateFormat.format("dd MMM yyyy", file.getLastOpened()));
            
            binding.getRoot().setOnClickListener(v -> listener.onFileClick(file));
            binding.btnMore.setOnClickListener(v -> listener.onMoreClick(v, file));
        }
    }
}
