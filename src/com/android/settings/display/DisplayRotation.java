/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.view.RotationPolicy;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DisplayRotation extends SettingsPreferenceFragment {
    private static final String TAG = "DisplayRotation";

    private static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_LOCKSCREEN_ROTATION = "lockscreen_rotation";
    private static final String ROTATION_0_PREF = "display_rotation_0";
    private static final String ROTATION_90_PREF = "display_rotation_90";
    private static final String ROTATION_180_PREF = "display_rotation_180";
    private static final String ROTATION_270_PREF = "display_rotation_270";

    private SwitchPreference mAccelerometer;
    private SwitchPreference mLockScreenRotation;
    private SwitchPreference mRotation0Pref;
    private SwitchPreference mRotation90Pref;
    private SwitchPreference mRotation180Pref;
    private SwitchPreference mRotation270Pref;

    public static final int ROTATION_0_MODE = 1;
    public static final int ROTATION_90_MODE = 2;
    public static final int ROTATION_180_MODE = 4;
    public static final int ROTATION_270_MODE = 8;

    private ContentObserver mAccelerometerRotationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateAccelerometerRotationSwitch();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.display_rotation);

        PreferenceScreen prefSet = getPreferenceScreen();

        mAccelerometer = (SwitchPreference) findPreference(KEY_ACCELEROMETER);
        mAccelerometer.setPersistent(false);

        mLockScreenRotation = (SwitchPreference) prefSet.findPreference(KEY_LOCKSCREEN_ROTATION);
        boolean lockScreenRotationEnabled = Settings.System.getInt(getContentResolver(),
                        Settings.System.LOCKSCREEN_ROTATION, 0) != 0;
        mLockScreenRotation.setChecked(lockScreenRotationEnabled);

        mRotation0Pref = (SwitchPreference) prefSet.findPreference(ROTATION_0_PREF);
        mRotation90Pref = (SwitchPreference) prefSet.findPreference(ROTATION_90_PREF);
        mRotation180Pref = (SwitchPreference) prefSet.findPreference(ROTATION_180_PREF);
        mRotation270Pref = (SwitchPreference) prefSet.findPreference(ROTATION_270_PREF);

        int mode = Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES,
                allowAllRotations()
                    ?   DisplayRotation.ROTATION_0_MODE
                        | DisplayRotation.ROTATION_90_MODE
                        | DisplayRotation.ROTATION_180_MODE
                        | DisplayRotation.ROTATION_270_MODE
                    :   DisplayRotation.ROTATION_0_MODE
                        | DisplayRotation.ROTATION_90_MODE
                        | DisplayRotation.ROTATION_270_MODE);

        mRotation0Pref.setChecked((mode & ROTATION_0_MODE) != 0);
        mRotation90Pref.setChecked((mode & ROTATION_90_MODE) != 0);
        mRotation180Pref.setChecked((mode & ROTATION_180_MODE) != 0);
        mRotation270Pref.setChecked((mode & ROTATION_270_MODE) != 0);

        boolean hasRotationLock = false;
//        getResources().getBoolean(
//                com.android.internal.R.bool.config_hasRotationLockSwitch);

        if (hasRotationLock) {
            // Disable accelerometer switch, but leave others enabled
            mAccelerometer.setEnabled(false);
            mRotation0Pref.setDependency(null);
            mRotation90Pref.setDependency(null);
            mRotation180Pref.setDependency(null);
            mRotation270Pref.setDependency(null);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        updateState();
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), true,
                mAccelerometerRotationObserver);
    }

    @Override
    public void onPause() {
        super.onPause();

        getContentResolver().unregisterContentObserver(mAccelerometerRotationObserver);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }

    private void updateState() {
        updateAccelerometerRotationSwitch();
    }

    private void updateAccelerometerRotationSwitch() {
        mAccelerometer.setChecked(!RotationPolicy.isRotationLocked(getActivity()));
    }

    private static boolean allowAllRotations() {
        return Resources.getSystem().getBoolean(
                com.android.internal.R.bool.config_allowAllRotations);
    }

    private int getRotationBitmask() {
        int mode = 0;
        if (mRotation0Pref.isChecked()) {
            mode |= ROTATION_0_MODE;
        }
        if (mRotation90Pref.isChecked()) {
            mode |= ROTATION_90_MODE;
        }
        if (mRotation180Pref.isChecked()) {
            mode |= ROTATION_180_MODE;
        }
        if (mRotation270Pref.isChecked()) {
            mode |= ROTATION_270_MODE;
        }
        return mode;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mAccelerometer) {
            RotationPolicy.setRotationLockForAccessibility(getActivity(),
                    !mAccelerometer.isChecked());
        } else if (preference == mRotation0Pref ||
                preference == mRotation90Pref ||
                preference == mRotation180Pref ||
                preference == mRotation270Pref) {
            int mode = getRotationBitmask();
            if (mode == 0) {
                mode |= ROTATION_0_MODE;
                mRotation0Pref.setChecked(true);
            }
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_ANGLES, mode);
        } else if (preference == mLockScreenRotation) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_ROTATION, (mLockScreenRotation.isChecked()) ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preference);
    }
}
