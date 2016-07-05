package com.iwhys.demo.animator;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.iwhys.demo.animator.items.CircleWave;
import com.iwhys.demo.animator.items.SomethingRandom;
import com.iwhys.library.animator.AnimatorHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ListView listView = new ListView(this);
        listView.setAdapter(new ArrayAdapter<Class<? extends AnimatorHolder.AnimatorItem>>(this, android.R.layout.simple_list_item_1, getData()){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Class<?> clazz = getItem(position);
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
                textView.setPadding(padding, padding, padding, padding);
                textView.setText(clazz.getSimpleName());
                textView.setTextColor(getTextColorStateList());
                return textView;
            }
        });
        listView.setDivider(new ColorDrawable(Color.LTGRAY));
        listView.setDividerHeight(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
        listView.setSelector(getSelectorDrawable());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Class<?> clazz = (Class<?>) listView.getAdapter().getItem(i);
                startActivity(DetailActivity.getIntent(MainActivity.this, clazz.getName()));
            }
        });
        setContentView(listView);
    }

    private List<Class<? extends AnimatorHolder.AnimatorItem>> getData(){
        List<Class<? extends AnimatorHolder.AnimatorItem>> list = new ArrayList<>();
        list.add(CircleWave.class);
        list.add(SomethingRandom.class);
        return list;
    }

    private Drawable getSelectorDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.BLUE));
        drawable.addState(new int[0], new ColorDrawable(Color.TRANSPARENT));
        return drawable;
    }

    private ColorStateList getTextColorStateList() {
        int[] colors = new int[]{Color.WHITE, Color.DKGRAY};
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_pressed};
        states[1] = new int[0];
        return new ColorStateList(states, colors);
    }
}
