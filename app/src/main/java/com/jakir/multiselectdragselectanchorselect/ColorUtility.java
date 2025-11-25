package com.jakir.multiselectdragselectanchorselect;

import android.content.Context;
import android.util.TypedValue;

import androidx.core.graphics.ColorUtils;

//
// Created by JAKIR HOSSAIN on 11/21/2025.
//
public class ColorUtility {
    public static int getColorFrom(Context context, int color) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(color, typedValue, true);
        return typedValue.data;
    }

    public static int getSemiTransparentColorFrom(Context context, int color, int percentage) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(color, typedValue, true);
        int colorValue = typedValue.data;
        percentage = Math.min(percentage, 100); // clamp to max 100
        return ColorUtils.setAlphaComponent(colorValue, (255 * percentage) / 100);
    }
}
