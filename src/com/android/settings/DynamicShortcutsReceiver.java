/*
 * Copyright (C) 2017 Yank555.lu's Android Open Source Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;

import java.util.Collections;

public final class DynamicShortcutsReceiver extends BroadcastReceiver {
    public static final String PACKAGE_NAME_MAGISK = "com.topjohnwu.magisk";
    private static final String MAGISK_SHORTCUT = "magiskShortcut";

    @Override
    public void onReceive(Context context, Intent intent) {
        ShortcutManager manager = context.getSystemService(ShortcutManager.class);
        Intent magiskIntent =
                context.getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME_MAGISK);
        if (magiskIntent != null) {
            ShortcutInfo shortcut = new ShortcutInfo.Builder(context, MAGISK_SHORTCUT)
                    .setIntent(magiskIntent)
                    .setShortLabel(context.getString(R.string.magisk_shortcut_title))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_magisk))
                    .build();
            manager.setDynamicShortcuts(Collections.singletonList(shortcut));
        } else {
            manager.disableShortcuts(Collections.singletonList(MAGISK_SHORTCUT));
            manager.removeAllDynamicShortcuts();
        }
    }
}
