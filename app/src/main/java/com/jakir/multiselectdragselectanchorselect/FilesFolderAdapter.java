package com.jakir.multiselectdragselectanchorselect;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jakir.dragselect.DragSelectHelper;
import com.jakir.pref.Pref;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FilesFolderAdapter extends RecyclerView.Adapter<FilesFolderAdapter.ViewHolder> {
    private static final Set<File> selectedFiles = new HashSet<>();
    private final Context context;
    private final Set<File> selectedTempFiles = new HashSet<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ActivityFilesFolder activityFilesFolder;
    private final RecyclerView recyclerView;
    private final AtomicInteger lastDragPos = new AtomicInteger(-1);

    // REPLACED File[] with ArrayList<File>
    private final ArrayList<File> filesAndFolders = new ArrayList<>();

    private boolean isGrid;
    private boolean isRangeMode = false;
    private int anchorPosition = -1;
    private static final int MULTI_SELECT_DUAL = 0;
    private static final int MULTI_SELECT_ANCHOR = 1;
    private static final int MULTI_SELECT_DRAG = 2;

    // Constructor now accepts a List<File> (can pass ArrayList)
    public FilesFolderAdapter(ActivityFilesFolder activityFilesFolder, Context context, List<File> initialFiles, RecyclerView recyclerView) {
        this.activityFilesFolder = activityFilesFolder;
        this.context = context;
        if (initialFiles != null) this.filesAndFolders.addAll(initialFiles);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.files_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File file = filesAndFolders.get(position);
        holder.itemView.setTag(file.getPath()); // tag for recycling validation

        holder.threeDot.setVisibility((Pref.getBoolean(Key.multiSelectMode, context) || isGrid || activityFilesFolder.isPasteMode()) ? GONE : VISIBLE);
        holder.selection.setVisibility((Pref.getBoolean(Key.multiSelectMode, context) || isGrid || activityFilesFolder.isPasteMode()) ? VISIBLE : GONE);

        holder.fileName.setText(file.getName());
        holder.icon.setImageResource(R.drawable.ic_select_all_24px);

        holder.itemView.setBackgroundColor(selectedFiles.contains(file) ? ContextCompat.getColor(context, R.color.selection_items_color) : Color.TRANSPARENT);
        holder.selection.setImageDrawable(selectedFiles.contains(file) ? ContextCompat.getDrawable(context, R.drawable.ic_circle_check_svgrepo_com) : null);

        holder.itemView.setOnClickListener(v -> {
            if (Pref.getBoolean(Key.multiSelectMode, context)) {
                toggleSelectionUnified(false, position, holder);
            } else {
                // your "open" logic here
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!activityFilesFolder.isPasteMode()) {
                toggleSelectionUnified(true, position, holder);
            }
            return true;
        });
    }

    // ----------  Multiselect Operation ----------
    public Set<File> getSelectedFiles() {
        return selectedFiles;
    }

    public boolean isMultiSelectMode() {
        return Pref.getBoolean(Key.multiSelectMode, context);
    }

    public void selectAllFiles() {
        selectedFiles.clear();
        selectedFiles.addAll(filesAndFolders); // uses ArrayList
        notifyDataSetChanged();
    }

    public void invertSelection() {
        selectedTempFiles.clear();
        for (File file : filesAndFolders) {
            if (!selectedFiles.contains(file)) {
                selectedTempFiles.add(file);
            }
        }
        selectedFiles.clear();
        selectedFiles.addAll(selectedTempFiles);
        notifyDataSetChanged();
    }

    // Unified toggle method
    private void toggleSelectionUnified(boolean longClick, int position, ViewHolder holder) {
        int method = Pref.getInteger(Key.multiSelectMethod, context); // 0 = dual, 1 = anchor, 2 = drag
        File file = filesAndFolders.get(position);

        if (!longClick) {
            toggleSingleItem(file, position, holder);
        }

        if (longClick) {
            if (method == MULTI_SELECT_ANCHOR || method == MULTI_SELECT_DUAL) {
                if (!isRangeMode) {
                    isRangeMode = true;
                    anchorPosition = position;
                    addFileIfNotSelected(file, position);
                } else {
                    selectRangeByAnchor(anchorPosition, position);
                    isRangeMode = false;
                }
            }

            if (method == MULTI_SELECT_DRAG || method == MULTI_SELECT_DUAL) {
                addFileIfNotSelected(file, position);

                lastDragPos.set(position);
                DragSelectHelper dragHelper = new DragSelectHelper(recyclerView, pos -> {
                    selectRangeByAnchor(lastDragPos.get(), pos);
                    lastDragPos.set(pos);
                    isRangeMode = false;
                });
                recyclerView.post(()-> dragHelper.updateZones(0.10f,0.80f));
                dragHelper.startSelection(position);
            }
        }

        boolean selectedFilesIsEmpty = selectedFiles.isEmpty();
        Pref.setBoolean(Key.multiSelectMode, !selectedFilesIsEmpty, context);
        activityFilesFolder.invalidateOptionsMenu();

        if (selectedFilesIsEmpty) isRangeMode = false;
        if (selectedFilesIsEmpty || longClick) notifyDataSetChanged();
    }

    private void toggleSingleItem(File file, int position, ViewHolder holder) {
        if (selectedFiles.contains(file)) selectedFiles.remove(file);
        else selectedFiles.add(file);

        notifyItemChanged(position);
        AnimHelper.animateSpring(holder.selection, false);
        AnimHelper.animateSpring(holder.icon, false);
    }

    private void addFileIfNotSelected(File file, int position) {
        if (!selectedFiles.contains(file)) {
            selectedFiles.add(file);
            notifyItemChanged(position);
        }
    }

    public void selectRangeByAnchor(int startPos, int endPos) {
        if (startPos == -1) return;
        int min = Math.min(startPos, endPos);
        int max = Math.max(startPos, endPos);

        for (int i = min; i <= max && i < filesAndFolders.size(); i++) {
            addFileIfNotSelected(filesAndFolders.get(i), i);
        }
        activityFilesFolder.invalidateOptionsMenu();
    }

    public void clearSelection(Context context) {
        selectedFiles.clear();
        isRangeMode = false;
        Pref.setBoolean( Key.multiSelectMode, false,context);
        notifyDataSetChanged();
    }

    // ----------  recyclerview update ----------
    // updateData now accepts a List<File>
    public void updateData(List<File> newFiles) {
        filesAndFolders.clear();
        if (newFiles != null) filesAndFolders.addAll(newFiles);
        notifyDataSetChanged();
    }

    public void setGrid(boolean grid) {
        this.isGrid = grid;
    }

    @Override
    public int getItemCount() {
        return filesAndFolders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon, threeDot, selection;
        TextView fileName;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.image_type);
            threeDot = itemView.findViewById(R.id.three_dot);
            selection = itemView.findViewById(R.id.selection);
            fileName = itemView.findViewById(R.id.fname);
        }
    }
}
