package com.chen.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by chenzhaohua on 17/5/26.
 */

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_test_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jump(TestActivity1.class);
            }
        });
        findViewById(R.id.btn_test_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jump(TestActivity2.class);
            }
        });
    }


    private void jump(Class targetActivityClass) {
        Intent intent = new Intent();
        intent.setClass(this, targetActivityClass);
        startActivity(intent);
    }


}
