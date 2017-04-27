package com.chen.easyplugin.pm;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.chen.easyplugin.core.Bundle;
import com.chen.easyplugin.core.PluginContextThemeWrapper;
import com.chen.easyplugin.core.PluginException;
import com.chen.easyplugin.core.PluginInfo;
import com.chen.easyplugin.core.PluginModule;
import com.chen.easyplugin.core.PluginPackageInfo;
import com.chen.easyplugin.utils.LogUtils;
import com.chen.easyplugin.utils.ReflectUtils;


import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by chenzhaohua on 17/4/7.
 */
public class PluginManager {
    private static final String TAG = "PluginManager";
    private static PluginManager instance;
    private Context mHostContext;
    private Object activityThread;

    /**
     * 正在运行的plugin service
     */
    private SparseArray<String> runningPluginServices = new SparseArray<String>();
    private final int MAX_SERVICE_NUM = 5;
    private final String PLUGIN_SERVICE_CLASSNAME = "com.chen.easyplugin.stub.BundleServiceContainer$Proxy";


    /**
     * 正在运行的SingleTask模式的activity
     */
    private SparseArray<String> runningSingleTaskActivities = new SparseArray<String>();
    private final int MAX_ACTIVITY_NUM = 5;
    private final String PLUGIN_SINGLETASK_ACTIVITY_CLASSNAME = "com.chen.easyplugin.stub.ActivityStub$SingleTaskStub";

    private ActivityManager am;


    private volatile Map<String, Bundle> installedBundles = new HashMap<String, Bundle>();

    private String cachePath;

    private PluginManager() {

    }

    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    public void init(Context hostContext) {

        String processName = getCurProcessName(hostContext);
        if (processName != null && processName.contains(":")) {
            Log.i("PluginManager", "Bundle Framework will not start in other process: " + processName);
            return;
        }
        if (!(hostContext instanceof Application)) {

        }
        this.mHostContext = hostContext;

        String rootPath = hostContext.getFilesDir().getAbsolutePath();
        cachePath = rootPath + "/bundles";
        File cacheDir = new File(cachePath);
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                throw new IllegalStateException("Unable to create bundles dir");
            }
        }

        Realm.init(hostContext);

        loadPlugin();
    }

    private static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private void loadPlugin() {

        ArrayList<PluginInfo> exceptionBundles = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();//Realm.getInstance(mHostContext);
        RealmResults<PluginInfo> list = realm.where(PluginInfo.class).findAll();

        if (list != null && list.size() > 0) {
            for (PluginInfo info : list) {
                File bundlePathFile = new File(info.getPluginPath() + "/version" + info.getVersion());
                if (!bundlePathFile.exists()) {
                    LogUtils.e(TAG, "[loadPlugin] exception bundle:" + info.getPackageName());
                    exceptionBundles.add(info);
                    continue;
                }
                Bundle bundle = new Bundle(PluginUtils.copyNewBundleInfo(info));
                installedBundles.put(info.getPackageName(), bundle);
            }
        }

        // 清除掉数据库的残留信息
        for (PluginInfo info : exceptionBundles) {
            removePluginInfo(info);
        }
        realm.close();
    }

    public Context getHostContext() {
        return mHostContext;
    }

    public Object getActivityThread() {
        if (activityThread == null) {
            try {
                activityThread = ReflectUtils.invoke(Class.forName("android.app.ActivityThread"),
                        null, "currentActivityThread");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return activityThread;
    }

    public Bundle getBundleByPackageName(String packageName) {
        return installedBundles.get(packageName);
    }

    /**
     * 获取SparseArray中和className相同值的Index
     *
     * @param array
     * @param className
     * @return
     */
    private int getSparseArrayIndexOfValue(SparseArray<String> array, String className) {
        if (TextUtils.isEmpty(className)) {
            return -1;
        }
        if (array != null && array.size() > 0) {
            for (int i = 0; i < array.size(); i++) {
                Integer key = array.keyAt(i);
                String value = array.get(key);
                if (className.equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public String chooseProxySingleTaskActivity(ComponentName componentName) {
        String result = "";
        if (componentName == null) {
            return result;
        }
        String className = componentName.getClassName();
        if (TextUtils.isEmpty(className)) {
            return result;
        }
        int index = getSparseArrayIndexOfValue(runningSingleTaskActivities, className);
        if (index >= 0 && index < MAX_ACTIVITY_NUM) { //如果此className已运行
            return PLUGIN_SINGLETASK_ACTIVITY_CLASSNAME + String.valueOf(index + 1);
        }
        // 从1~MAX_SINGLETASK中找出一个不在runningPluginServices的keys中的序号
        for (int i = 0; i < MAX_ACTIVITY_NUM; i++) {
            if (runningSingleTaskActivities.valueAt(i) == null ||
                    !(runningSingleTaskActivities.valueAt(i) instanceof String)) {
                runningSingleTaskActivities.put(i, className);
                return PLUGIN_SINGLETASK_ACTIVITY_CLASSNAME + String.valueOf(i + 1);
            }
        }
        LogUtils.i(TAG, "[chooseProxySingleTaskActivity] Don't find can use Proxy Activity.");
        return result;
    }

    public void removeProxySingleTaskActivity(String className) {
        int index = getSparseArrayIndexOfValue(runningSingleTaskActivities, className);
        if (index >= 0) {
            runningSingleTaskActivities.remove(index);
        }
    }

    /**
     * 根据要启动的Service类名获取一个未被使用的Service容器（真正注册的）
     * 目前默认会声名5个PluginProxyService在AndroidManifest.xml
     * PluginProxyService1~PluginProxyService10
     */
    public String chooseProxyService(ComponentName componentName) {
        String result = "";
        if (componentName == null) {
            return result;
        }
        String className = componentName.getClassName();
        if (TextUtils.isEmpty(className)) {
            return result;
        }
        int index = getSparseArrayIndexOfValue(runningPluginServices, className);
        if (index >= 0 && index < MAX_SERVICE_NUM) { //如果此className已运行
            return PLUGIN_SERVICE_CLASSNAME + String.valueOf(index + 1);
        }
        // 从1~10中找出一个不在runningPluginServices的keys中的序号
        for (int i = 0; i < MAX_SERVICE_NUM; i++) {
            if (runningPluginServices.valueAt(i) == null ||
                    !(runningPluginServices.valueAt(i) instanceof String)) {
                runningPluginServices.put(i, className);
                return PLUGIN_SERVICE_CLASSNAME + String.valueOf(i + 1);
            }
        }
        LogUtils.i(TAG, "[chooseProxyService] Can't find free Proxy service.");
        return result;
    }

    /**
     * Bundle Service destroy时要从runningPluginServices中remove
     *
     * @param className
     */
    public void removeProxyService(String className) {
        int index = getSparseArrayIndexOfValue(runningPluginServices, className);
        if (index >= 0) {
            runningPluginServices.remove(index);
        }
    }

    /**
     * 获取Host平台的类加载器；
     * 自定义的Bundle类加载器需要使用它加载一些系统和Host平台上的类
     *
     * @return
     */
    public ClassLoader getParentClassLoader() {
        return getClass().getClassLoader();
    }

    /**
     * 获取Bundle中的View实例
     */
    public View getBundleView(Context context, String packageName, String viewClassName) {
        Bundle bundle = getBundleByPackageName(packageName);
        if (bundle != null) {
            PluginModule module = bundle.getBundleModule();
            ClassLoader classLoader = module.getClassLoader();
            try {
                Class<?> aClass = classLoader.loadClass("android.app.Activity");
                Activity activity = (Activity) aClass.newInstance();
                PluginContextThemeWrapper newBase = new PluginContextThemeWrapper(module.getPulginApplication(), 0);
                newBase.setBundleModule(module);
                //ReflectUtils.invoke(c, activity, "attachBaseContext", new Class[]{Context.class}, new Object[]{newBase});
                ReflectUtils.invoke(ContextThemeWrapper.class, activity, "attachBaseContext",
                        new Class[]{Context.class}, new Object[]{newBase});
//                ReflectUtils.writeField(activity, "mBase", newBase);
                Class<?> cls = classLoader.loadClass(viewClassName);
                Constructor<?> constructor = cls.getDeclaredConstructor(Context.class);
                constructor.setAccessible(true);
                View view = (View) constructor.newInstance(activity);
                return view;
            } catch (Exception e) {
                LogUtils.e("PluginManager", "[getBundleView] error: " + e.toString());
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 安装、更新和删除Bundle功能 ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
     */

    /**
     * 同步方法：生成一个新的Bundle Id
     */
    private synchronized long generateBundleId() {
        long id = -1;

        Realm realm = Realm.getDefaultInstance();

        long count = realm.where(PluginInfo.class).count();

        if (count > 0) {
            id = realm.where(PluginInfo.class).max("id").intValue() + 1;
        } else {
            id = 0;
        }
        if (id >= 0) {
            PluginInfo info = new PluginInfo();
            info.setId(id);
            realm.beginTransaction();
            realm.copyToRealm(info);
            realm.commitTransaction();
        }
        realm.close();
        return id;
    }

    private PluginInfo getBundleInfoFromApk(String apkPath) {
        PluginInfo info = null;
        PackageInfo packageInfo = PluginUtils.parseApk(mHostContext, apkPath);
        if (packageInfo != null) {
            info = new PluginInfo();
            info.setPackageName(packageInfo.packageName);
            info.setVersion(packageInfo.versionCode);
//            android.os.Bundle metaData = packageInfo.applicationInfo.metaData;
//            if (metaData != null) {
//                // 解析出Bundle的配置信息
//                info.setType((String) metaData.get("net.goeasyway.bundle_type"));
//            }
            info.setPackageInfo(packageInfo);
        }
        return info;
    }

    /**
     * 安装Bundle
     */
    private void installBundle(final String apkFilePath) {

        PluginInfo info = getBundleInfoFromApk(apkFilePath);

        if (info == null) {
            return;
        }

        String packageName = info.getPackageName();
        Bundle bundle = installedBundles.get(packageName);

        if (bundle != null) {
            // TODO 已安装过的Bundle如何提示HOST端
            return;
        }

        long bundleId = generateBundleId();
        if (bundleId == -1) {
            return;
        }

        try {
            info.setId(bundleId);
            info.setPluginPath(cachePath + "/bundle" + bundleId);
            info.setApkPath(info.getPluginPath() + "/version" + info.getVersion() + "/bundle.apk");
            extractApkFileToCache(info, apkFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        bundle = new Bundle(info);
        installedBundles.put(info.getPackageName(), bundle);

        //保存安装的BundleInfo到数据库
        saveBundleInfo(info);
        /**
         * 第一次安装Bundle，创建一个DexClassLoader实例，会去做一些优化DEX文件的工作，
         * 在安装时就创建ClassLoader，这样可以达到优化下次使用Bundle时创建ClassLoader的耗时
         */
        bundle.getBundleClassLoader();
    }

    private synchronized void saveBundleInfo(PluginInfo info) {
        if (info == null || info.getId() < 0) {
            return;
        }
        Realm realm = Realm.getDefaultInstance();//Realm.getInstance(mHostContext);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(info);
        realm.commitTransaction();
        Bundle.savePackageInfo(info, info.getPackageInfo(), realm);
        realm.close();
    }


    public void asyncInstallBundle(final String apkPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                installBundle(apkPath);
            }
        }).start();
    }


    /**
     * 将Bundle APK文件释放到相应的Bundle Cache目录中
     */
    private void extractApkFileToCache(PluginInfo info, String apkFilePath) throws Exception {
        File bundlePath = new File(info.getPluginPath() + "/version" + info.getVersion());
        // Bundle目录是否存在
        if (!bundlePath.exists()) {
            bundlePath.mkdirs();
        }
        // 复制APK到指定Bundle目录中
        try {
            PluginUtils.copyApk(apkFilePath, info.getApkPath());
        } catch (Exception e) {
            throw new PluginException(PluginException.ERROR_CODE_COPY_FILE_APK, e.getMessage());
        }
        // 复制APK中的so库文件到Bundle指定的LIB目录
        File libPathFile = new File(info.getPluginPath() + "/version" + info.getVersion() + "/lib");
        if (!libPathFile.exists()) {
            libPathFile.mkdirs();
        }
        try {
            PluginUtils.copyLibs(apkFilePath, libPathFile.getAbsolutePath());
        } catch (Exception e) {
            throw new PluginException(PluginException.ERROR_CODE_COPY_FILE_SO, e.getMessage());
        }
        // 创建应用的data目录
        File dataPathFile = new File(info.getPluginPath() + "/data");
        if (!dataPathFile.exists()) {
            dataPathFile.mkdirs();
        }
    }

    private void updateBundle(Bundle bundle, String newApkPath) {
        PluginInfo info = getBundleInfoFromApk(newApkPath);
        if (info == null) {
            return;
        }

        String packageName = info.getPackageName();
        int version = info.getVersion();
        int oldVersion = bundle.getBundleInfo().getVersion();
        if (version == oldVersion) {
            LogUtils.e("updateBundle", "installed [" + packageName + "] version is same with update version " + version);
            return;
        }

        bundle.releasePluginBundle(); //释放PluginModule

        // 删除旧版本
        String path = cachePath + "/bundle" + bundle.getBundleInfo().getId() + "/version" + oldVersion;
        File bundleFile = new File(path);
        PluginUtils.deleteDirectoryTree(bundleFile);
        installedBundles.remove(packageName);

        long bundleId = bundle.getBundleInfo().getId();
        try {
            info.setId(bundleId);
            info.setPluginPath(cachePath + "/bundle" + bundleId);
            info.setApkPath(info.getPluginPath() + "/version" + info.getVersion() + "/bundle.apk");
            extractApkFileToCache(info, newApkPath);
        } catch (Exception e) {
            LogUtils.e("updateBundle", "[" + packageName + "] extractApkFileToCache error:" + e.getMessage());
            return;
        }
        Bundle newBundle = new Bundle(info);
        installedBundles.put(info.getPackageName(), newBundle);
        //保存安装的BundleInfo到数据库
        saveBundleInfo(info);
    }

    /**
     * 更新Bundle
     *
     * @param bundle
     * @param newApkPath Bundle apk路径
     */
    public void asyncUpdateBundle(final Bundle bundle, final String newApkPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateBundle(bundle, newApkPath);
            }
        }).start();
    }

    /**
     * 卸载Bundle
     *
     * @param bundle
     */
    public void asyncUnInstallBundle(final Bundle bundle) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String packageName = bundle.getPackageName();
                if (TextUtils.isEmpty(packageName)) {
                    return;
                }

                bundle.releasePluginBundle(); //释放PluginModule

                String path = cachePath + "/bundle" + bundle.getBundleInfo().getId();
                File bundleFile = new File(path);
                PluginUtils.deleteDirectoryTree(bundleFile);
                removePluginInfo(bundle.getBundleInfo());
                installedBundles.remove(packageName);
            }
        }).start();
    }

    private synchronized void removePluginInfo(PluginInfo info) {
        if (info == null) {
            return;
        }
        Realm realm = Realm.getDefaultInstance();
        RealmResults<PluginInfo> results = realm.where(PluginInfo.class).equalTo("packageName", info.getPackageName()).findAll();
        RealmResults<PluginPackageInfo> packageInfos = realm.where(PluginPackageInfo.class).equalTo("packageName", info.getPackageName()).findAll();
        realm.beginTransaction();
        if (results != null && results.size() > 0) {
            results.deleteAllFromRealm();
        }
        if (packageInfos != null && packageInfos.size() > 0) {
            packageInfos.deleteAllFromRealm();
        }
        realm.commitTransaction();
        realm.close();
    }


    public void installOrUpgradeAssetsBundles() {
        try {
            final String[] bundleNames = mHostContext.getAssets().list("bundles");

            if (bundleNames == null) {
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PluginUtils.copyAssetsBundlesToPhone(mHostContext);
                    String sdPath = PluginUtils.getDefaultBundleFilePath();
                    File files = new File(sdPath);
                    File[] fileList = files.listFiles();
                    if (fileList == null) {
                        return;
                    }
                    for (File file : fileList) {
                        String apkPath = file.getAbsolutePath();
                        PluginInfo info = getBundleInfoFromApk(apkPath);
                        Bundle bundle = installedBundles.get(info.getPackageName());
                        if (bundle != null) {
                            updateBundle(bundle, apkPath);
                        } else {
                            installBundle(apkPath);
                        }
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
