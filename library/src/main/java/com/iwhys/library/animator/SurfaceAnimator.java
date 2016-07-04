package com.iwhys.library.animator;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.HandlerThread;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.LinearInterpolator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Author:      iwhys
 * Email:       whs008@gmail.com
 * Time:        6/28/16 11:16
 * Description:
 */
public class SurfaceAnimator implements IAnimator {

    private final DrawTask mDrawTask;
    private int mWidth;
    private int mHeight;
    private SurfaceView mSurfaceView;

    /**
     * Instantiates a new Surface animator.
     *
     * @param surfaceView the surface view
     */
    public SurfaceAnimator(SurfaceView surfaceView){
        mSurfaceView = surfaceView;
        mSurfaceView.setZOrderOnTop(true);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSLUCENT);
        mDrawTask = new DrawTask(holder);
        mDrawTask.start();
    }

    @Override
    public void targetSizeChanged(int width, int height){
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void start(){
        mDrawTask.startAnimator();
    }

    @Override
    public void pause(){
        mDrawTask.stopAnimator();
    }

    @Override
    public void stop(){
        pause();
        mDrawTask.mAnimatorItemsContainer.clear();
        AnimatorHolder.clear();
        mSurfaceView = null;
        try {
            mDrawTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(AnimatorHolder holder){
        holder.setSize(mWidth, mHeight);
        mDrawTask.mAnimatorItemsContainer.add(holder);
    }


    /**
     * The drawing thread
     */
    private static class DrawTask extends HandlerThread {
        /**
         * The M animator items container.
         */
        final Set<AnimatorHolder> mAnimatorItemsContainer = new HashSet<>();
        /**
         * The M surface holder.
         */
        final SurfaceHolder mSurfaceHolder;
        /**
         * The M value animator.
         */
        ValueAnimator mValueAnimator;

        /**
         * Instantiates a new Draw task.
         *
         * @param surfaceHolder the surface holder
         */
        DrawTask(SurfaceHolder surfaceHolder){
            super("Animator");
            mSurfaceHolder = surfaceHolder;
        }

        /**
         * Start animator.
         */
        void startAnimator(){
            mValueAnimator = ValueAnimator.ofInt(0, 1);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    performDraw();
                }
            });
            mValueAnimator.start();
        }

        /**
         * Stop animator.
         */
        void stopAnimator(){
            if (mValueAnimator != null && mValueAnimator.isRunning()){
                mValueAnimator.cancel();
            }
        }

        /**
         * Perform draw.
         */
        void performDraw(){
            synchronized (mSurfaceHolder){
                Canvas canvas = mSurfaceHolder.lockCanvas(null);
                if (canvas != null){
                    onDraw(canvas);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        /**
         * Perform the real drawing action
         *
         * @param canvas canvas
         */
        void onDraw(Canvas canvas) {
            clearCanvas(canvas);
            Iterator<AnimatorHolder> iterator = mAnimatorItemsContainer.iterator();
            while (iterator.hasNext()){
                AnimatorHolder holder = iterator.next();
                if (holder.isCanceled() || holder.isFinished()){
                    iterator.remove();
                } else {
                    holder.onDraw(canvas);
                }
            }
        }

        /**
         * Clear the canvas,it's very important, because this canvas can't be auto clear
         *
         * @param canvas canvas
         */
        void clearCanvas(Canvas canvas){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
    }
}
