package com.chen.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.chen.easyplugin.hook.FrameworkHookHelper;


/**
 * Created by chenzhaohua on 17/4/12.
 */

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_entrance_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.amap.map3d.demo", "com.amap.map3d.demo.MainActivity");
                startActivity(intent);
            }
        });

        findViewById(R.id.tv_entrance_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("top.zibin.luban.example", "top.zibin.luban.example.MainActivity");
                startActivity(intent);
            }
        });


    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            FrameworkHookHelper.hookActivityManagerNative();
            FrameworkHookHelper.hookActivityThreadHandler();
            FrameworkHookHelper.hookPackageManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
