package com.shejan.pdfbox_pdfeditor.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shejan.pdfbox_pdfeditor.databinding.ItemToolCardBinding;
import com.shejan.pdfbox_pdfeditor.model.Tool;

import java.util.List;

public class ToolCardsAdapter extends RecyclerView.Adapter<ToolCardsAdapter.ToolViewHolder> {

    private final List<Tool> tools;
    private final OnToolClickListener listener;

    public interface OnToolClickListener {
        void onToolClick(Tool tool);
    }

    public ToolCardsAdapter(List<Tool> tools, OnToolClickListener listener) {
        this.tools = tools;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemToolCardBinding binding = ItemToolCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ToolViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        holder.bind(tools.get(position));
    }

    @Override
    public int getItemCount() {
        return tools.size();
    }

    class ToolViewHolder extends RecyclerView.ViewHolder {
        private final ItemToolCardBinding binding;

        public ToolViewHolder(ItemToolCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Tool tool) {
            binding.txtToolName.setText(tool.getName());
            binding.txtToolDesc.setText(tool.getDescription());
            binding.imgToolIcon.setImageResource(tool.getIconResId());
            binding.getRoot().setOnClickListener(v -> listener.onToolClick(tool));
        }
    }
}
