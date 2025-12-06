package com.jakir.multiselectdragselectanchorselect;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.jakir.pref.Pref;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    public static void setLocal(String languageCode, Context context) {
        Locale locale;
        if (languageCode.equals("system") || languageCode.isEmpty()) {
            locale = new Locale(Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage());
        } else {
            locale = new Locale(languageCode);
        }

        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        Pref.setString("language",languageCode,  context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(Pref.getTheme(this)); //set theme
        setLocal(Pref.getString("language", this), this); // set language
    }
}