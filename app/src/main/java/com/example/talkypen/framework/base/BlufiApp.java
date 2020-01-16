package com.example.talkypen.framework.base;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.talkypen.net.MqttUtil;
import com.example.talkypen.ui.constants.SettingsConstants;

import org.litepal.LitePal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BlufiApp extends Application {
    private static BlufiApp instance;

    private final HashMap<String, Object> mCache = new HashMap<>();

    private SharedPreferences mSettingsShared;

    public static BlufiApp getInstance() {
        if (instance == null) {
            throw new NullPointerException("App instance hasn't registered");
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LitePal.initialize(this);
        instance = this;
        mSettingsShared = getSharedPreferences(SettingsConstants.PREF_SETTINGS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        mCache.clear();
    }

    public boolean settingsPut(String key, Object value) {
        SharedPreferences.Editor editor = mSettingsShared.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else  if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Set) {
            Set set = (Set) value;
            Set<String> newSet = new HashSet<>();
            for (Object object : set) {
                newSet.add((String)object);
            }
            editor.putStringSet(key, newSet);
        } else {
            return false;
        }

        editor.apply();
        return true;
    }

    public Object settingsGet(String key, Object defaultValue) {
        if (defaultValue instanceof String) {
            return mSettingsShared.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return mSettingsShared.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return mSettingsShared.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return mSettingsShared.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Long) {
            return mSettingsShared.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Set) {
            //noinspection unchecked
            return mSettingsShared.getStringSet(key, (Set<String>) defaultValue);
        } else {
            return null;
        }
    }
}
