package com.chen.easyplugin.core;


import com.chen.easyplugin.utils.LogUtils;

import dalvik.system.DexClassLoader;

/**
 * Created by chenzhaohua on 17/4/7.
 */
public class PluginClassLoader extends DexClassLoader {

    private static final String TAG = "PluginClassLoader";

    private ClassLoader mHostClassLoader; //Host的APK的类加载器, 由Android系统为其生成的PathClassLoader

    public PluginClassLoader(String dexPath, String optimizedDirectory,
                             String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        mHostClassLoader = parent;
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {

        Class<?> clazz = findLoadedClass(className);

        if (clazz == null) {

            ClassLoader systemClassLoader = mHostClassLoader.getParent();

            // 借鉴Felix OSGI的思路：java开头或android系统的类 从parent的类加载器查找 (
            if (className.startsWith("java.") || className.startsWith("javax.")
                    || className.startsWith("android.")) {
                try {
                    clazz = systemClassLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    LogUtils.e(TAG, e.getMessage());
                }
            }
            // 从自身的DEX查找
            if (clazz == null) {
                try {
                    clazz = findClass(className);
                } catch (Exception e) {
                    LogUtils.e(TAG, e.getMessage());
                }
            }

            if (clazz == null) {
                try {
                    clazz = mHostClassLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    clazz = systemClassLoader.loadClass(className);
                }
            }
        }
        if (clazz == null) {
            LogUtils.w(TAG, "Can't find class: " + className);
        }
        return clazz;
    }

}
