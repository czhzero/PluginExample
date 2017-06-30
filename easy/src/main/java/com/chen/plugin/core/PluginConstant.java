package com.chen.plugin.core;

/**
 * Created by chenzhaohua on 17/5/27.
 */

public class PluginConstant {

    public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";

    public static final String EXTRA_TARGET_INTENT = "com.chen.plugin.OldIntent";
    public static final String EXTRA_TARGET_INTENT_URI = "com.chen.plugin.OldIntent.Uri";
    public static final String EXTRA_TARGET_INFO = "com.chen.plugin.OldInfo";
    public static final String EXTRA_STUB_INFO = "com.chen.plugin.NewInfo";
    public static final String EXTRA_TARGET_AUTHORITY = "TargetAuthority";
    public static final String EXTRA_TYPE = "com.chen.plugin.EXTRA_TYPE";
    public static final String EXTRA_ACTION = "com.chen.plugin.EXTRA_ACTION";

    public static final int STUB_NO_ACTIVITY_MAX_NUM = 5;
}
