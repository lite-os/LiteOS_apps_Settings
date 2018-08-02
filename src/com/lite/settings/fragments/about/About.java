/*
 * Copyright (C) 2018 LiteOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lite.settings.fragments.about;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.R;

public class About extends SettingsPreferenceFragment {

    public static final String TAG = "About";

    private String LITE_SOURCE = "lite_source";
    private String LITE_DOWNLOAD = "lite_download";
    private String LITE_DONATE = "lite_donate";

    private String MAINTAINER = "lite_maintainer";
    private String MAINTAINER_DONATE = "lite_maintainer_donate";

    private Preference mSourceUrl;
    private Preference mDownloadUrl;
    private Preference mDonate;

    private Preference mMaintainer;
    private Preference mMaintainerDonate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lite_settings_about);

        mSourceUrl = findPreference(LITE_SOURCE);
        mDownloadUrl = findPreference(LITE_DOWNLOAD);
        mDonate = findPreference(LITE_DONATE);

        mMaintainer = findPreference(MAINTAINER);
        mMaintainerDonate = findPreference(MAINTAINER_DONATE);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        String MaintainerUrl = getResources().getString(R.string.lite_maintainer_url);
        String MaintainerDonateUrl = getResources().getString(R.string.lite_maintainer_donate_url);

        if (preference == mSourceUrl) {
            clickUrl("https://github.com/lite-os");
        } else if (preference == mDownloadUrl) {
            clickUrl("https://sourceforge.net/projects/lite-os/files/");
        } else if (preference == mDonate) {
            clickUrl("https://www.paypal.me/alehadruga");
        } else if (preference == mMaintainer) {
            clickUrl(MaintainerUrl);
        } else if (preference == mMaintainerDonate) {
            clickUrl(MaintainerDonateUrl);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void clickUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(intent);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.LITE;
    }
}
