package com.iwhys.demo.animator.items;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.animation.AccelerateInterpolator;

import com.iwhys.library.animator.AnimatorHolder;


/**
 * Author:      iwhys
 * Email:       whs008@gmail.com
 * Time:        6/21/16 18:36
 * Description:
 */
public class CircleWave extends AnimatorHolder.AnimatorItem {

    private final static int mOffset = 100;
    private final static int mStrokeWidth = 8;
    private final static int mEdgeColor = Color.parseColor("#ffff0000");
    private final static int mCenterColor = Color.parseColor("#aaff0000");

    @Override
    protected void onAttached() {
        setInterpolator(new AccelerateInterpolator());
    }

    @Override
    protected void onDraw(Canvas canvas, Paint paint) {
        setRect();
        RadialGradient radialGradient = new RadialGradient(mCurrentRect.centerX(), mCurrentRect.centerY(), mCurrentRect.width() / 2, mCenterColor, mEdgeColor, Shader.TileMode.REPEAT);
        paint.setStyle(Paint.Style.STROKE);
        paint.setShader(radialGradient);
        paint.setAlpha(getAlpha());
        paint.setStrokeWidth(getStrokeWidth());
        canvas.drawCircle(mCurrentRect.centerX(), mCurrentRect.centerY(), mCurrentRect.width() / 2, paint);
    }

    private void setRect() {
        int offset = (int) (mOffset * getProgress());
        mCurrentRect.set(mOriginRect);
        mCurrentRect.inset(-offset, -offset);
    }

    private int getStrokeWidth() {
        return (int) (mStrokeWidth * (1 - getProgress()) + 1);
    }

    private int getAlpha() {
        return (int) (255 * (1 - getProgress()));
    }
}
