package com.chen.easyplugin.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Parcel;


import com.chen.easyplugin.pm.PluginManager;
import com.chen.easyplugin.pm.PluginUtils;
import com.chen.easyplugin.utils.ParcelableUtils;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by chenzhaohua on 17/4/7.
 */
public class Bundle {

    private PluginModule bundleModule;
    private PluginInfo bundleInfo;
    private PluginClassLoader pluginClassLoader;

    public Bundle(PluginInfo info) {
        bundleInfo = info;
    }

    public PluginInfo getBundleInfo() {
        return bundleInfo;
    }

    public PluginModule getBundleModule() {
        if (bundleModule == null) {
            String apkPath = bundleInfo.getApkPath();
            String libraryPath = bundleInfo.getPluginPath() + "/version" + bundleInfo.getVersion() + "/lib";
            String optimized = bundleInfo.getPluginPath() + "/version" + bundleInfo.getVersion();
            File pluginDataFile = new File(bundleInfo.getPluginPath() + "/data");
            ClassLoader parent = PluginManager.getInstance().getParentClassLoader();
            if ( pluginClassLoader == null) {
                pluginClassLoader = new PluginClassLoader(apkPath, optimized, libraryPath, parent);
            }
            PackageInfo packageInfo = bundleInfo.getPackageInfo();
            Context hostContext = PluginManager.getInstance().getHostContext();
            if (packageInfo == null) {
                packageInfo = getPackageInfo(bundleInfo);
                bundleInfo.setPackageInfo(packageInfo);
            }
            bundleModule = new PluginModule(hostContext, apkPath, pluginDataFile, pluginClassLoader, packageInfo);
        }
        return bundleModule;
    }

    public PluginClassLoader getBundleClassLoader() {
        if (pluginClassLoader == null) {
            String apkPath = bundleInfo.getApkPath();
            String libraryPath = bundleInfo.getPluginPath() + "/version" + bundleInfo.getVersion() + "/lib";
            String optimized = bundleInfo.getPluginPath() + "/version" + bundleInfo.getVersion();
            ClassLoader parent = PluginManager.getInstance().getParentClassLoader();
            pluginClassLoader = new PluginClassLoader(apkPath, optimized, libraryPath, parent);
        }
        return pluginClassLoader;
    }

    public void releasePluginBundle() {
        if (bundleModule != null) {
            bundleModule = null;
        }
    }

    public String getPackageName() {
        return bundleInfo.getPackageName();
    }

    public String getType() {
        return bundleInfo.getType();
    }



    /**
     * 获取PackageInfo信息
     * 如果在数据库中未保存有PackageInfo (Parcelable对像)，则从APK文件解析，并转换为byte[]保存入数据库。
     */
    private PackageInfo getPackageInfo(PluginInfo pluginInfo) {

        PackageInfo packageInfo = null;
        Context hostContext = PluginManager.getInstance().getHostContext();
        Realm realm = Realm.getDefaultInstance(); //Realm.getInstance(hostContext);
        RealmQuery<PluginPackageInfo> query = realm.where(PluginPackageInfo.class);
        PluginPackageInfo info = query.equalTo("packageName", pluginInfo.getPackageName()).findFirst();

        //从数据库获取的数据转换成PackageInfo
        if (info != null) {
            Parcel parcel = ParcelableUtils.unmarshall(info.getInfoData());
            try {
                packageInfo = PackageInfo.CREATOR.createFromParcel(parcel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 从APK文件解析出PackageInfo，并存入数据库中
        if (packageInfo == null) {
            packageInfo =  PluginUtils.parseApk(hostContext, pluginInfo.getApkPath());
            savePackageInfo(pluginInfo, packageInfo, realm);
        }

        realm.close();
        return packageInfo;
    }


    /**
     * 存储PackageInfo到数据库
     * @param pluginInfo
     * @param packageInfo
     * @param realm
     */
    public static void savePackageInfo(PluginInfo pluginInfo, PackageInfo packageInfo, Realm realm) {
        PluginPackageInfo info = new PluginPackageInfo();
        info.setPackageName(pluginInfo.getPackageName());
        info.setInfoData(ParcelableUtils.marshall(packageInfo));
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(info);
        realm.commitTransaction();
    }


}
