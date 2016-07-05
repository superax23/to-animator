package com.iwhys.demo.animator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.iwhys.demo.animator.items.CircleWave;
import com.iwhys.demo.animator.items.SomethingRandom;
import com.iwhys.library.animator.AnimatorHolder;
import com.iwhys.library.animator.IAnimator;
import com.iwhys.library.animator.UiAnimator;


/**
 * Author:      iwhys
 * Email:       whs008@gmail.com
 * Time:        6/29/16 09:27
 * Description:
 */
public class ViewDemo extends View {

    private final IAnimator mAnimator;

    public ViewDemo(Context context) {
        this(context, null);
    }

    public ViewDemo(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAnimator = new UiAnimator(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        Rect rect = new Rect(x - 10, y - 10, x + 10, y + 10);
        if (event.getAction() == MotionEvent.ACTION_UP){
            AnimatorHolder randomDrawable = AnimatorHolder.obtain(SomethingRandom.class).speed(100).originRect(rect);
            AnimatorHolder wave = AnimatorHolder.obtain(CircleWave.class).totalDuration(3000).speed(200).startDelayed(500).originRect(rect);
            mAnimator.start(randomDrawable);
            mAnimator.start(wave);
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mAnimator.targetSizeChanged(w, h);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus){
            mAnimator.stop();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        ((UiAnimator) mAnimator).onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        mAnimator.destroy();
        super.onDetachedFromWindow();
    }
}
