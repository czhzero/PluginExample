package com.chen.easyplugin.core;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by chenzhaohua on 17/4/7.
 */
public class PluginPackageInfo extends RealmObject {

    @PrimaryKey
    private String packageName;
    private byte[] infoData;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public byte[] getInfoData() {
        return infoData;
    }

    public void setInfoData(byte[] infoData) {
        this.infoData = infoData;
    }
}
