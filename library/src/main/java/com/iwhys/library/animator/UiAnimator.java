package com.iwhys.library.animator;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Author:      iwhys
 * Email:       whs008@gmail.com
 * The type Ui animator.
 */
public class UiAnimator implements IAnimator {

    private final List<AnimatorHolder> mAnimatorItemsContainer = new ArrayList<>();

    /**
     * The target
     */
    private Object mTarget;

    private int mWidth;

    private int mHeight;

    private ValueAnimator mValueAnimator;

    /**
     * Instantiates a new Ui animator.
     *
     * @param target the target
     */
    public UiAnimator(Object target){
        mTarget = target;
        assertTarget();
    }

    @Override
    public void targetSizeChanged(int width, int height){
        mWidth = width;
        mHeight = height;
        for (AnimatorHolder holder : mAnimatorItemsContainer) {
            holder.setSize(mWidth, mHeight);
        }
    }

    @Override
    public void start(){
        mValueAnimator = ValueAnimator.ofInt(0, 1);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                refreshCanvas();
            }
        });
        mValueAnimator.start();
    }

    @Override
    public void pause(){
        if (mValueAnimator != null && mValueAnimator.isRunning()){
            mValueAnimator.cancel();
        }
    }

    @Override
    public void stop(){
        pause();
        mAnimatorItemsContainer.clear();
        AnimatorHolder.clear();
        mTarget = null;
    }

    @Override
    public void add(AnimatorHolder holder){
        holder.setSize(mWidth, mHeight);
        mAnimatorItemsContainer.add(holder);
    }

    /**
     * This method should be invoked in the target's draw method
     * {@link View#onDraw(Canvas)} or
     * {@link ViewGroup#onDraw(Canvas)} or
     * {@link Drawable#draw(Canvas)}
     *
     * @param canvas the canvas
     */
    public void onDraw(Canvas canvas) {
        List<AnimatorHolder> list = getSnapshot(mAnimatorItemsContainer);
        Iterator<AnimatorHolder> iterator = list.iterator();
        while (iterator.hasNext()){
            AnimatorHolder holder = iterator.next();
            if (holder.isCanceled() || holder.isFinished()){
                iterator.remove();
            } else {
                holder.onDraw(canvas);
            }
        }
    }

    private void refreshCanvas() {
        if (mTarget instanceof Drawable) {
            ((Drawable) mTarget).invalidateSelf();
        } else {
            ((View) mTarget).invalidate();
        }
    }

    private void assertTarget() {
        if (!(mTarget instanceof View) && !(mTarget instanceof Drawable)) {
            throw new IllegalArgumentException("The target must be an instance of View/ViewGroup or Drawable");
        }
        if (mTarget instanceof ViewGroup){
            ((ViewGroup) mTarget).setWillNotDraw(false);
        }
    }

    private static <T> List<T> getSnapshot(Collection<T> src){
        List<T> result = new ArrayList<>(src.size());
        for (T item : src) {
            result.add(item);
        }
        return result;
    }

}
