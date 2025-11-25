package com.jakir.multiselectdragselectanchorselect;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;

public class AppUtil {
    public static PackageInfo pInfo;

    public static int getVersionCode(Context context) {
        pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return pInfo.versionCode;
    }

    public static String getVersionCodename(Context context) {
        pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return pInfo.versionName;
    }


    public static int getActionBarSize(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return actionBarSize;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }

        return result; // return height in px
    }

    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }

        return result; // return height in px
    }

    public static void setMargin(View view, Integer left, Integer top, Integer right, Integer bottom) {

        ViewGroup.LayoutParams lp = view.getLayoutParams();

        if (lp instanceof ViewGroup.MarginLayoutParams params) {
            if (left != null) params.leftMargin = left;     // apply only if provided
            if (top != null) params.topMargin = top;
            if (right != null) params.rightMargin = right;
            if (bottom != null) params.bottomMargin = bottom;

            view.setLayoutParams(params);
        }
    }
}