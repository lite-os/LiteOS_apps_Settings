/*
 *  Copyright (C) 2018 LiteOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.lite.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.PowerManager;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import android.provider.SearchIndexableResource;
import android.provider.Settings;

import android.view.View;
import android.util.Log;
import android.app.AlertDialog;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.custom.CustomUtils;

import com.lite.settings.preference.CustomSeekBarPreference;
import com.lite.settings.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class NavigationBar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "NavigationBar";

    private static final String KEYS_SHOW_NAVBAR_KEY = "navigation_bar_show";
    private static final String KEY_CATEGORY_BRIGHTNESS = "button_backlight";
    private static final String KEY_BUTTON_BRIGHTNESS = "button_brightness";
    private static final String KEY_BUTTON_MANUAL_BRIGHTNESS_NEW = "button_manual_brightness_new";
    private static final String KEY_BUTTON_BACKLIGHT_TOUCH = "button_backlight_on_touch_only";
    private static final String KEY_BUTTON_TIMEOUT = "button_timeout";

    private Handler mHandler;

    private int mDeviceDefaultButtonBrightness;

    private boolean mDeviceHasVariableButtonBrightness;

    private IPowerManager mPowerService; 
    private ListPreference mBacklightTimeout;
    private SwitchPreference mEnableNavBar;
    private SwitchPreference mButtonBrightness;
    private SystemSettingSwitchPreference mButtonBacklightTouch;
    private CustomSeekBarPreference mManualButtonBrightness;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigationbar);

        mHandler = new Handler();

        final Resources res = getActivity().getResources();
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mDeviceHasVariableButtonBrightness = res.getBoolean(
                com.android.internal.R.bool.config_deviceHasVariableButtonBrightness);

        mDeviceDefaultButtonBrightness = res.getInteger(
                com.android.internal.R.integer.config_buttonBrightnessSettingDefault);

        mEnableNavBar = (SwitchPreference) prefSet.findPreference(
                KEYS_SHOW_NAVBAR_KEY);

        boolean showNavBarDefault = CustomUtils.deviceSupportNavigationBar(getActivity());
        boolean showNavBar = Settings.System.getInt(resolver,
                    Settings.System.NAVIGATION_BAR_SHOW, showNavBarDefault ? 1:0) == 1;
        mEnableNavBar.setChecked(showNavBar);

        final PreferenceCategory brightnessCategory =
                (PreferenceCategory) prefSet.findPreference(KEY_CATEGORY_BRIGHTNESS);

        mButtonBacklightTouch =
                (SystemSettingSwitchPreference) findPreference(KEY_BUTTON_BACKLIGHT_TOUCH);

        mManualButtonBrightness = (CustomSeekBarPreference) findPreference(KEY_BUTTON_MANUAL_BRIGHTNESS_NEW);
        if (mManualButtonBrightness != null) {
            int ManualButtonBrightness = Settings.System.getInt(resolver,
                    Settings.System.BUTTON_BRIGHTNESS, mDeviceDefaultButtonBrightness);
            mManualButtonBrightness.setValue(ManualButtonBrightness / 1);
            mManualButtonBrightness.setOnPreferenceChangeListener(this);
        }

        mButtonBrightness = (SwitchPreference) findPreference(KEY_BUTTON_BRIGHTNESS);
        if (mButtonBrightness != null) {
            mButtonBrightness.setChecked((Settings.System.getInt(resolver,
                    Settings.System.BUTTON_BRIGHTNESS, 1) == 1));
            mButtonBrightness.setOnPreferenceChangeListener(this);
        }

        mBacklightTimeout = (ListPreference) findPreference(KEY_BUTTON_TIMEOUT);
        if (mBacklightTimeout != null) {
            int BacklightTimeout = Settings.System.getInt(resolver,
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 5000);
            mBacklightTimeout.setValue(Integer.toString(BacklightTimeout));
            mBacklightTimeout.setSummary(mBacklightTimeout.getEntry());
            mBacklightTimeout.setOnPreferenceChangeListener(this);
        }

        if (mDeviceHasVariableButtonBrightness) {
            brightnessCategory.removePreference(mButtonBrightness);
        } else {
            brightnessCategory.removePreference(mManualButtonBrightness);
        }
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);

        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mEnableNavBar) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW, checked ? 1:0);
            return true;
        }
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        final String key = preference.getKey();

        if (preference == mBacklightTimeout) {
            String BacklightTimeout = (String) newValue;
            int BacklightTimeoutValue = Integer.parseInt(BacklightTimeout);
            Settings.System.putInt(resolver,
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, BacklightTimeoutValue);
            int BacklightTimeoutIndex = mBacklightTimeout.findIndexOfValue(BacklightTimeout);
            mBacklightTimeout.setSummary(mBacklightTimeout.getEntries()[BacklightTimeoutIndex]);
            return true;
        } else if (preference == mManualButtonBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BUTTON_BRIGHTNESS, value * 1);
            return true;
        } else if (preference == mButtonBrightness) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BUTTON_BRIGHTNESS, value ? 1 : 0);
            return true;
        } 
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.LITE;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.navigationbar;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
