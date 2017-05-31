package com.chen.plugin.core;

/**
 * Created by chenzhaohua on 17/5/27.
 */

public class PluginConstant {

    public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";

    public static final String EXTRA_TARGET_INTENT = "com.morgoo.droidplugin.OldIntent";
    public static final String EXTRA_TARGET_INTENT_URI = "com.morgoo.droidplugin.OldIntent.Uri";
    public static final String EXTRA_TARGET_INFO = "com.morgoo.droidplugin.OldInfo";
    public static final String EXTRA_STUB_INFO = "com.morgoo.droidplugin.NewInfo";
    public static final String EXTRA_TARGET_AUTHORITY = "TargetAuthority";
    public static final String EXTRA_TYPE = "com.morgoo.droidplugin.EXTRA_TYPE";
    public static final String EXTRA_ACTION = "com.morgoo.droidplugin.EXTRA_ACTION";

    public static final int STUB_NO_ACTIVITY_MAX_NUM = 5;
}
