package com.jakir.dragselect;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by JAKIR HOSSAIN on 11/25/2025.
 ***********************************************************************************************/

public class DragSelectHelper {

    private final RecyclerView recyclerView;
    private final RangeSelector rangeSelector;
    private final Handler autoScrollHandler = new Handler();
    private final AtomicInteger autoScrollSpeed = new AtomicInteger(42);
    private boolean isSelecting = false;
    private int lastPos = -1;
    // Auto scroll runnable
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isSelecting) return;

            recyclerView.scrollBy(0, autoScrollSpeed.get());

            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            int firstVisible = lm != null ? lm.findFirstVisibleItemPosition() : 0;
            int lastVisible = lm != null ? lm.findLastVisibleItemPosition() : 0;
            int pos = autoScrollSpeed.get() > 0 ? lastVisible - 1 : firstVisible + 1;
            if (pos != RecyclerView.NO_POSITION && pos != lastPos) {
                rangeSelector.onRangeSelect(pos);
                lastPos = pos;
            }

            autoScrollHandler.postDelayed(this, 16);
        }
    };
    private float topZone, bottomZone;
    private float lastTouchY = 0;

    public DragSelectHelper(RecyclerView recyclerView, RangeSelector selector) {
        this.recyclerView = recyclerView;
        this.rangeSelector = selector;
        setupTouchListener();
    }

    // -----------------------------
    // Start selection from outside
    // -----------------------------
    public void startSelection(int startPos) {
        isSelecting = true;
        lastPos = startPos;
    }

    // Stop selection
    public void stopSelection() {
        isSelecting = false;
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    // -----------------------------------
    // Touch listener setup
    // -----------------------------------
    private void setupTouchListener() {

        recyclerView.setOnTouchListener((v, e) -> {

            lastTouchY = e.getY();

            switch (e.getActionMasked()) {

                case MotionEvent.ACTION_MOVE:
                    if (isSelecting) {

                        View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (child != null) {
                            int pos = recyclerView.getChildAdapterPosition(child);
                            if (pos != RecyclerView.NO_POSITION && pos != lastPos) {
                                rangeSelector.onRangeSelect(pos);
                                lastPos = pos;
                            }
                        }

                        if (lastTouchY < topZone) {
                            autoScrollSpeed.set(-42);
                            if (!autoScrollHandler.hasCallbacks(autoScrollRunnable))
                                autoScrollHandler.post(autoScrollRunnable);

                        } else if (lastTouchY > bottomZone) {
                            autoScrollSpeed.set(42);
                            if (!autoScrollHandler.hasCallbacks(autoScrollRunnable))
                                autoScrollHandler.post(autoScrollRunnable);

                        } else {
                            autoScrollHandler.removeCallbacks(autoScrollRunnable);
                        }

                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopSelection();
                    break;
            }

            return false;
        });
    }

    // Must call after layout load
    public void updateZones() {
        topZone = recyclerView.getHeight() * 0.10f;
        bottomZone = recyclerView.getHeight() * 0.70f;
    }

    // External dependencies
    public interface RangeSelector {
        void onRangeSelect(int position); // adapter.selectRange1(pos)
    }
}

