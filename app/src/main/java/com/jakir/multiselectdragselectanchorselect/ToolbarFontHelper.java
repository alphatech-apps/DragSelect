package com.jakir.multiselectdragselectanchorselect;

//
// Created by JAKIR HOSSAIN on 8/29/2025.
//

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.FontRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.color.MaterialColors;

public class ToolbarFontHelper {
    public static float fontExtraSmall = 12f;
    public static float fontSmall = 14f;
    public static float fontMedium = 19f;
    public static float fontLarge = 24f;

    // Set Toolbar Title style (Size + Font + Bold/Normal)
    public static void setToolbarTitleStyle(Context context, Toolbar toolbar, float sizeSp, @FontRes int fontResId, boolean isBold) {
        Typeface typeface = null;
        if (fontResId != 0) {
            typeface = ResourcesCompat.getFont(toolbar.getContext(), fontResId);
        }

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView tv) {
                if (tv.getText().equals(toolbar.getTitle())) {
                    tv.setTextSize(sizeSp); // Title size
                    if (typeface != null) {
                        tv.setTypeface(typeface, isBold ? Typeface.BOLD : Typeface.NORMAL);
                    } else {
                        tv.setTypeface(tv.getTypeface(), isBold ? Typeface.BOLD : Typeface.NORMAL);
                    }
                }
            }
        }
    }

    public static void setToolbarTitleStyleColor(Context context, Toolbar toolbar, float sizeSp, @FontRes int fontResId, boolean isBold, int color) {
        Typeface typeface = null;
        if (fontResId != 0) {
            typeface = ResourcesCompat.getFont(toolbar.getContext(), fontResId);
        }

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView tv) {
                if (tv.getText().equals(toolbar.getTitle())) {
                    tv.setTextSize(sizeSp); // Title size
                    tv.setTextColor(color);
                    if (typeface != null) {
                        tv.setTypeface(typeface, isBold ? Typeface.BOLD : Typeface.NORMAL);
                    } else {
                        tv.setTypeface(tv.getTypeface(), isBold ? Typeface.BOLD : Typeface.NORMAL);
                    }
                }

                // Subtitle styling
                if (tv.getText().equals(toolbar.getSubtitle())) {
                    tv.setTextSize(fontExtraSmall); // Subtitle size
                    tv.setTextColor(color);
                    if (typeface != null) {
                        tv.setTypeface(typeface, Typeface.NORMAL);
                    } else {
                        tv.setTypeface(tv.getTypeface(), Typeface.NORMAL);
                    }
                }
            }
        }
    }

    // Shortcut: Small
    public static void setSmallTitle(Context context, Toolbar toolbar, boolean isBold) {
        setToolbarTitleStyle(context, toolbar, fontSmall, 0, isBold);
    }

    // Shortcut: Medium
    public static void setMediumTitle(Context context, Toolbar toolbar, boolean isBold) {
        setToolbarTitleStyle(context, toolbar, fontMedium, 0, isBold);
    }

    // Shortcut: Large
    public static void setLargeTitle(Context context, Toolbar toolbar, boolean isBold) {
        setToolbarTitleStyle(context, toolbar, fontLarge, 0, isBold);
    }

    // Shortcut: Medium
    public static void setMediumTitleColor(Context context, Toolbar toolbar, boolean isBold) {
        int color = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.GRAY);
        setToolbarTitleStyleColor(context, toolbar, fontMedium, 0, isBold, color);
    }

    // Shortcut: Large
    public static void setLargeTitleColor(Context context, Toolbar toolbar, boolean isBold) {
        int color = ContextCompat.getColor(context,R.color.tolbar_color);
        setToolbarTitleStyleColor(context, toolbar, fontLarge, 0, isBold, color);
    }

    // Shortcut: Custom Font + Large Bold
    public static void setCustomFontTitle(Context context, Toolbar toolbar, @FontRes int fontResId, float sizeSp, boolean isBold) {
        setToolbarTitleStyle(context, toolbar, sizeSp, fontResId, isBold);
    }
}

// Usages >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// Small title
//ToolbarFontHelper.setSmallTitle(context,toolbar,false);

// Medium tittle
//ToolbarFontHelper.setMediumTitle(context,toolbar,false);

// Large Bold title
//ToolbarFontHelper.setLargeTitle(context,toolbar,true);

// Custom font + 22sp + Bold
//ToolbarFontHelper.setCustomFontTitle(context,toolbar, R.font.my_custom_font, ToolbarFontHelper.fontLarge, true);