package com.iwhys.demo.animator;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.iwhys.demo.animator.items.CircleWave;
import com.iwhys.demo.animator.items.SomethingRandom;
import com.iwhys.library.animator.AnimatorHolder;
import com.iwhys.library.animator.IAnimator;
import com.iwhys.library.animator.SurfaceAnimator;


/**
 * Author:      iwhys
 * Email:       whs008@gmail.com
 * Time:        6/22/16 18:51
 * Description:
 */
public class SurfaceViewDemo extends SurfaceView implements SurfaceHolder.Callback {

    private final IAnimator mAnimator;

    public SurfaceViewDemo(Context context) {
        this(context, null);
    }

    public SurfaceViewDemo(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        mAnimator = new SurfaceAnimator(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        Rect rect = new Rect(x - 10, y - 10, x + 10, y + 10);
        AnimatorHolder holder = AnimatorHolder.obtain(SomethingRandom.class).speed(100).originRect(rect);
        AnimatorHolder wave = AnimatorHolder.obtain(CircleWave.class).totalDuration(2000).speed(100).originRect(rect);
        mAnimator.add(holder);
        mAnimator.add(wave);
        return true;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mAnimator.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mAnimator.targetSizeChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mAnimator.pause();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAnimator.stop();
    }
}
