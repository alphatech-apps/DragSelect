package com.jakir.multiselectdragselectanchorselect;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jakir.fastscroller.FastScroller;
import com.jakir.pref.Pref;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityFilesFolder extends BaseActivity {
    public static List<File> clipboardList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<File> filesAndFolders = new ArrayList<>();
    private File[] filesAndFoldersFinal;
    private RecyclerView recyclerView;
    private FilesFolderAdapter filesFolderAdapter;
    private ActionBar actionBar;
//     private SwipeRefreshLayout swipeRefreshLayout;

    private MaterialToolbar toolbar;

    //----------- onCreate -----------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_file_folder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        ToolbarFontHelper.setLargeTitle(this, toolbar, true);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(getString(R.string.app_name));

        int statusBarHeight = AppUtil.getStatusBarHeight(this);
        toolbar.setPadding(toolbar.getPaddingLeft(), statusBarHeight, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
        getWindow().setStatusBarColor(ColorUtility.getSemiTransparentColorFrom(this, com.google.android.material.R.attr.colorSurface, 80));


        recyclerView = findViewById(R.id.recyclerView);
//        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
//        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.tolbar_color));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        filesFolderAdapter = new FilesFolderAdapter(this, this, filesAndFolders, recyclerView);
        recyclerView.setAdapter(filesFolderAdapter);

        loadFilesInBackground();

        // Set up SwipeRefreshLayout
//        swipeRefreshLayout.setOnRefreshListener(() -> {
//            swipeRefreshLayout.setRefreshing(false);
//            // Skip if multiSelect on
//            if (Pref.getState(Key.multiSelectMode, this)) return;
//            // otherwise reload files
////            loadFilesAndUpdateRecyclerview(true);
//            loadFilesInBackground(true);
//        });

        handleBackPress();

        showSelectionDialog();

    }


    // ==============================
    // SHOW SELECTION DIALOG (0,1,2)
    // ==============================
    private void showSelectionDialog() {

        String[] options = {"(0) Dual Select ", "(1) Anchor Only ", "(2) Drag Only "};

        new MaterialAlertDialogBuilder(this).setTitle("Select Multi-Select Mode").setCancelable(false) // force user to choose
                .setItems(options, (dialog, which) -> {

                    // which = 0,1,2
                    Pref.setInt(Key.multiSelectMethod, which, this); // save

                    // Now load RecyclerView
                }).show()
        ;
    }
    //----------- Permission -----------


    //----------- Back press behavior -----------
    private void handleBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FilesFolderAdapter adapter = (FilesFolderAdapter) recyclerView.getAdapter();
                if (adapter != null && adapter.isMultiSelectMode()) {
                    adapter.clearSelection(getApplicationContext());
                    invalidateOptionsMenu();
                } else finish();
            }
        });
    }


    //----------- Load Files -----------

    private void loadFilesInBackground() {
        executor.execute(() -> {
            // If you want real files, replace generateDummyFiles(1000) with actual loader
            List<File> files = generateDummyFiles(500);
            // store final array if needed elsewhere
            filesAndFoldersFinal = files.toArray(new File[0]);

            handler.post(() -> {
                // update adapter with the new List
                filesFolderAdapter.updateData(files);
                invalidateOptionsMenu();
            });
        });
    }

    // helper to generate 1000 File objects (dummy paths)
    private List<File> generateDummyFiles(int count) {
        List<File> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            // Create placeholder File objects — replace path with real file path when available
            File f = new File(Environment.getExternalStorageDirectory(), "1000_dummy_" + i + ".txt");
            list.add(f);
        }
        return list;
    }


    //----------- Menu Top -----------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filefolder, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Dynamically update ActionBar title, icons and menu visibility depending on MultiSelect state
        FilesFolderAdapter adapter = (FilesFolderAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            int selectedItem = adapter.getSelectedFiles().size();
            boolean multiselect = adapter.isMultiSelectMode() && !adapter.getSelectedFiles().isEmpty();
            int totalItem = adapter.getItemCount();
            boolean pasteMode = isPasteMode();
            boolean isGrid = Pref.getState(Key.isGrid, this);

            // Multi-select menu visibility
            menu.findItem(R.id.menu_action_select_all).setVisible(multiselect && selectedItem != totalItem && !pasteMode);
            menu.findItem(R.id.menu_action_clear_selection).setVisible(multiselect && !pasteMode);
            menu.findItem(R.id.menu_action_clear_selection).setTitle(multiselect && selectedItem != totalItem && !pasteMode ? "Select Inverse" : "Clear All");
            menu.findItem(R.id.menu_action_clear_selection).setIcon(multiselect && selectedItem != totalItem && !pasteMode ? R.drawable.ic_select_inverse_flip_to_back : R.drawable.ic_select_deselect_24px);

            // ActionBar setup
            actionBar.setDisplayHomeAsUpEnabled(multiselect || pasteMode);
            if (multiselect || pasteMode) actionBar.setHomeAsUpIndicator(R.drawable.ic_close_24px);
            if (multiselect || pasteMode)
                ToolbarFontHelper.setMediumTitleColor(this, toolbar, false);
            else ToolbarFontHelper.setLargeTitleColor(this, toolbar, true);
            actionBar.setTitle(pasteMode ? clipboardList.size() + " in Clipboard" : (multiselect ? selectedItem + " item" + (selectedItem > 1 ? "s" : "") + " selected" : getString(R.string.app_name)));

            List<File> targetFiles;
            if (pasteMode) {
                targetFiles = clipboardList; // already List<File>
            } else if (multiselect) {
                targetFiles = new ArrayList<>(adapter.getSelectedFiles()); // Set<File> → List<File>
            } else {
                // null check
                targetFiles = filesAndFoldersFinal != null ? Arrays.asList(filesAndFoldersFinal) : new ArrayList<>();
            }


        }
//         swipeRefreshLayout.setEnabled(!Pref.getState(Key.multiSelectMode, this));
        updateToolbarScrollBehavior(!Pref.getState(Key.multiSelectMode, this));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        FilesFolderAdapter adapter = (FilesFolderAdapter) recyclerView.getAdapter();
        if (adapter == null) return super.onOptionsItemSelected(item);
        int selectedItem = adapter.getSelectedFiles().size();
        int totalItem = adapter.getItemCount();
        Set<File> selectedFiles = adapter.getSelectedFiles();
        boolean multiSelected = adapter.isMultiSelectMode() && !adapter.getSelectedFiles().isEmpty();
        boolean pasteMode = isPasteMode();
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (multiSelected && !pasteMode) {
                adapter.clearSelection(this);
            } else if (!multiSelected && pasteMode) {
                clipboardList.clear();
                adapter.notifyDataSetChanged();
            }
            invalidateOptionsMenu();
            return true;
        } else if (id == R.id.menu_action_select_all) {
            if (multiSelected && !pasteMode) {
                adapter.selectAllFiles();
                invalidateOptionsMenu();
            }
            return true;
        } else if (id == R.id.menu_action_clear_selection) {
            if (multiSelected && !pasteMode) {
                if (totalItem == selectedItem) adapter.clearSelection(this);
                else adapter.invertSelection();

                invalidateOptionsMenu();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean isPasteMode() {
        return Pref.getState(Key.pastetMode, this) && !clipboardList.isEmpty();
    }


    //----------- extra -----------
    private void updateToolbarScrollBehavior(boolean enable) {
        // Wait for layout to complete
        recyclerView.post(() -> {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null && enable) {
                int total = lm.getItemCount(); // total items
                int visible = lm.getChildCount(); // visible items on screen
                AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
                if (total > visible) {  // // You can adjust number
                    // Enable scroll
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                } else {
                    // Disable scroll
                    params.setScrollFlags(0);
                }
                toolbar.setLayoutParams(params);
            } else {
                AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
                params.setScrollFlags(0);
                toolbar.setLayoutParams(params);
            }
        });

//        FastScroller.attach(recyclerView, null, null, null, null, Color.TRANSPARENT);
        FastScroller.attach(recyclerView);
    }


}