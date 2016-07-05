package com.iwhys.demo.animator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.iwhys.demo.animator.items.CircleWave;
import com.iwhys.demo.animator.items.SomethingRandom;
import com.iwhys.library.animator.AnimatorHolder;

/**
 * Author:      iwhys
 * Email:       whs008@gmail.com
 * Time:        7/5/16 09:16
 * Description:
 */
public class DetailActivity extends AppCompatActivity {

    private final static String CLASS_PATH = "class_path";

    public static Intent getIntent(Context context, String classPath){
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(CLASS_PATH, classPath);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String classPath = getIntent().getStringExtra(CLASS_PATH);
        if (!TextUtils.isEmpty(classPath)){
            try {
                Class clazz = Class.forName(classPath);
                AnimatorHolder holder = null;
                if (clazz.equals(CircleWave.class)){
                    holder = AnimatorHolder.obtain(CircleWave.class).speed(200);
                } else if (clazz.equals(SomethingRandom.class)){
                    holder = AnimatorHolder.obtain(SomethingRandom.class).speed(100);
                }
                if (holder != null){
                    setTitle(clazz.getSimpleName());
                    View view = new ViewDemo(this, holder);
                    setContentView(view);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                finish();
            }
        } else {
            finish();
        }
    }


}
