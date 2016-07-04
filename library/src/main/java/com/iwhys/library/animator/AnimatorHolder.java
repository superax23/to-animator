package com.iwhys.library.animator;

import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pools;
import android.view.animation.LinearInterpolator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Author:      iwhys
 * Email:       whs008@gmail.com
 * Time:        6/28/16 12:36
 * Description:
 */
public class  AnimatorHolder {

    /**
     * the max size of every object pool
     */
    private static final int MAX_POOL_SIZE = 10;

    /**
     * the object pools
     */
    private final static ArrayMap<Class, Pools.SynchronizedPool<AnimatorHolder>> sPoolMap = new ArrayMap<>();

    /**
     * The container of the running items
     */
    private final ArrayList<AnimatorItem> mRunningList = new ArrayList<>();

    /**
     * The Container of the recycled items
     */
    private final Set<AnimatorItem> mRecyclerSet = Collections.newSetFromMap(new WeakHashMap<AnimatorItem, Boolean>());

    /**
     * The item' type
     */
    private final Class<? extends AnimatorItem> mItemClass;

    /**
     * The paint
     */
    private final Paint mPaint = new Paint();

    /**
     * The width
     */
    private int mWidth;

    /**
     * The height
     */
    private int mHeight;

    /**
     * Can item be cached
     */
    private boolean mCacheItem = true;

    /**
     * If the speed value is zero and this value is true,
     * when the only item finished, it will continue run until the total time is over.
     */
    private boolean mFillAfter;

    /**
     * The total duration of the animator
     */
    private long mTotalDuration = -1;

    /**
     * The default interval for add a new item
     * Note: If {@link #needNewItem(long, long, long, long)} be override, the speed is invalidate
     */
    private long mSpeed = 300;

    /**
     * The animator listener
     */
    private AnimatorListener mListener;

    /**
     * The origin rect
     */
    private final Rect mOriginRect = new Rect();

    /**
     * The time when the animator started.
     */
    private long mStartTime;

    /**
     * The delayed duration before the animator start.
     */
    private long mStartDelayed;

    /**
     * The time when the last new item has been added
     */
    private long mLastNewItemTime;

    /**
     * Finish flag
     */
    private boolean mFinished = false;

    /**
     * Cancel flag
     */
    private boolean mCanceled = false;


    /**
     * The interface Animator listener.
     */
    public abstract class AnimatorListener {
        /**
         * On start.
         */
        void onStart(){}

        /**
         * On finished.
         */
        void onFinished(){}

        /**
         * On canceled.
         */
        void onCanceled(){}
    }


    /**
     * The constructor
     * @param itemClass item class
     */
    private AnimatorHolder(Class<? extends AnimatorItem> itemClass){
        mItemClass = itemClass;
        initPaint();
    }

    /**
     * Retriever an instance from the pool
     *
     * @param itemClass item class
     * @return animator holder
     */
    public static AnimatorHolder obtain(Class<? extends AnimatorItem> itemClass){
        Pools.SynchronizedPool<AnimatorHolder> pool = sPoolMap.get(itemClass);
        if (pool == null){
            pool = new Pools.SynchronizedPool<>(MAX_POOL_SIZE);
            sPoolMap.put(itemClass, pool);
        }
        AnimatorHolder holder = pool.acquire();
        if (holder != null && holder.mItemClass.equals(itemClass)){
            holder.reset();
            return holder;
        } else {
            return new AnimatorHolder(itemClass);
        }
    }

    /**
     * clear the pools
     */
    public static void clear(){
        sPoolMap.clear();
    }

    /**
     * Listener animator holder.
     *
     * @param listener the listener
     * @return the animator holder
     */
    public AnimatorHolder listener(AnimatorListener listener){
        mListener = listener;
        return this;
    }

    /**
     * If the value is true, the only item will run until the total time is over.
     * @param fillAfter boolean
     * @return the animator holder
     */
    public AnimatorHolder fillAfter(boolean fillAfter){
        mFillAfter = fillAfter;
        return this;
    }

    /**
     * Cache item animator holder.
     *
     * @param cacheItem the cache item
     * @return the animator holder
     */
    public AnimatorHolder cacheItem(boolean cacheItem){
        mCacheItem = cacheItem;
        return this;
    }

    /**
     * Speed animator holder.
     * Note: if you just need one animator item, set the speed value zero, please
     *
     * @param speed the speed
     * @return the animator holder
     */
    public AnimatorHolder speed(long speed){
        mSpeed = speed;
        return this;
    }

    /**
     * The delay duration before the animator start
     *
     * @param startDelayed delay
     * @return the animator holder
     */
    public AnimatorHolder startDelayed(long startDelayed){
        mStartDelayed = startDelayed;
        return this;
    }

    /**
     * Total duration animator holder.
     *
     * @param totalDuration the total duration
     * @return the animator holder
     */
    public AnimatorHolder totalDuration(long totalDuration){
        mTotalDuration = totalDuration;
        return this;
    }

    /**
     * Origin rect animator holder.
     *
     * @param originRect the origin rect
     * @return the animator holder
     */
    public AnimatorHolder originRect(Rect originRect){
        if (originRect != null) {
            mOriginRect.set(originRect);
        }
        return this;
    }

    /**
     * Internal invoke by the library, perform add new item, and traversal perform item's drawing method
     * don't invoke this method at any time
     *
     * @param canvas canvas
     */
    public final void onDraw(Canvas canvas){

        /**
         * Set animator start
         */
        setAnimatorStart();

        /**
         * Handle animator delay
         */
        if (animatorDelay()) return;

        /**
         * Handle animator canceled
         */
        if (animatorCanceled()) return;

        /**
         * Add a new item
         */
        if (needNewItem(mStartTime, mLastNewItemTime, mTotalDuration, mStartDelayed)){
            addNewItem();
        }

        /**
         * Handle animator finished
         */
        if (animatorFinished()) return;

        /**
         * Traversal perform item's drawing method
         */
        performDraw(canvas);
    }

    /**
     * Cancel the animator, only for the kind of itemClass
     */
    public void cancel(){
        mCanceled = true;
    }

    /**
     * Has the animator been canceled
     *
     * @return result boolean
     */
    public boolean isCanceled() {
        return mCanceled;
    }

    /**
     * Has the animator been finished
     *
     * @return result boolean
     */
    public boolean isFinished(){
        return mFinished;
    }

    /**
     * Set size
     *
     * @param w the w
     * @param h the h
     */
    public void setSize(int w, int h){
        mWidth = w;
        mHeight = h;
    }

    /**
     * Traversal perform item's drawing method,and remove the item finished
     * @param canvas the canvas
     */
    private void performDraw(Canvas canvas){
        Iterator<AnimatorItem> iterator = mRunningList.iterator();
        while (iterator.hasNext()) {
            AnimatorItem item = iterator.next();
            if (item.isFinished() && !(mSpeed ==0 && mFillAfter)) {
                iterator.remove();
                if (mCacheItem){
                    mRecyclerSet.add(item);
                }
            } else {
                item.onDraw(canvas, mPaint);
            }
        }
    }

    /**
     * Handle animator start
     */
    private void setAnimatorStart(){
        if (mStartTime == 0){
            mStartTime = System.currentTimeMillis();
            if (mListener != null){
                mListener.onStart();
            }
        }
    }

    /**
     * Handle animator delay
     * @return result
     */
    private boolean animatorDelay(){
        return System.currentTimeMillis() - mStartTime < mStartDelayed;
    }

    /**
     * Handle animator has been canceled
     * @return result
     */
    private boolean animatorCanceled(){
        if (mCanceled){
            if (mListener != null){
                mListener.onCanceled();
            }
            recycle();
            return true;
        }
        return false;
    }

    /**
     * Handle animator finished
     * @return result
     */
    private boolean animatorFinished(){
        if (!mFinished && mRunningList.isEmpty()){
            mFinished = true;
            if (mListener != null){
                mListener.onFinished();
            }
            recycle();
            return true;
        }
        return false;
    }

    /**
     * Reset the key properties to reuse
     */
    private void reset(){
        mStartTime = 0;
        mLastNewItemTime = 0;
        mFinished = false;
        mCanceled = false;
    }

    /**
     * Put current object into the pool
     */
    private void recycle(){
        Pools.SynchronizedPool<AnimatorHolder> pool = sPoolMap.get(mItemClass);
        pool.release(this);
    }

    /**
     * Add a new item
     */
    private void addNewItem(){
        AnimatorItem item = obtainItem();
        if (item == null) {
            return;
        }
        long now = System.currentTimeMillis();
        item.mStartTime = now;
        item.mOriginRect.set(mOriginRect);
        item.mWidth = mWidth;
        item.mHeight = mHeight;
        item.mPosition = mRunningList.size();
        item.onAttached();
        mRunningList.add(item);
        mLastNewItemTime = now;
        onNewItemAttached(item);
    }

    /**
     * Retriever an animator item
     * @return entity of animator item
     */
    private AnimatorItem obtainItem(){
        AnimatorItem item = null;
        if (mCacheItem){
            item = itemFromRecycler();
        }
        if (item == null){
            item = createNewItem();
        }
        return item;
    }

    /**
     * Retriever an item from the recycler
     *
     * @return entity
     */
    private AnimatorItem itemFromRecycler() {
        if (!mRecyclerSet.isEmpty()) {
            Iterator<AnimatorItem> itemIterator = mRecyclerSet.iterator();
            if (itemIterator.hasNext()){
                AnimatorItem item = itemIterator.next();
                if (item.isFinished()) {
                    itemIterator.remove();
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Initialize the paint
     */
    private void initPaint(){
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    /**
     * Create a new item
     * the default implement can be override
     *
     * @return animator item
     */
    protected AnimatorItem createNewItem(){
        try {
            Constructor constructor = mItemClass.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return (AnimatorItem) constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method is invoked when the new item has been created
     * override to set some properties of the item
     *
     * @param item the new item
     */
    protected void onNewItemAttached(AnimatorItem item){}

    /**
     * Need to add a new item
     * can be override by the subclass
     *
     * @param startTime       the start time
     * @param lastNewItemTime the last new item time
     * @param totalDuration   the total duration
     * @param startDelayed   the delay duration before start
     * @return boolean
     */
    protected boolean needNewItem(long startTime, long lastNewItemTime, long totalDuration, long startDelayed){
        /**
         * for the case only need one item
         */
        if (mSpeed == 0){
            return lastNewItemTime == 0;
        }

        long now = System.currentTimeMillis();
        boolean speedResult = now - lastNewItemTime >= mSpeed;
        boolean otherResult = true;
        if (totalDuration >= 0){
            if (!mRunningList.isEmpty()){
                AnimatorItem lastItem = mRunningList.get(mRunningList.size() - 1);
                otherResult = startTime + totalDuration + startDelayed - lastItem.mStartTime > lastItem.mDuration;
            } else {
                /**
                 * For the case that the animator is finished, and the running list is empty
                 */
                otherResult = now - startTime - startDelayed < totalDuration;
            }
        }
        return speedResult && otherResult;
    }


    /**
     * the base class of single animator item
     */
    public static abstract class AnimatorItem {
        /**
         * The M origin rect.
         */
        protected final Rect mOriginRect = new Rect();
        /**
         * The M current rect.
         */
        protected final RectF mCurrentRect = new RectF();
        /**
         * The M position.
         */
        protected int mPosition;
        /**
         * The M width.
         */
        protected int mWidth;
        /**
         * The M height.
         */
        protected int mHeight;

        /**
         * The animator's start time
         */
        private long mStartTime;

        /**
         * The animator's duration
         */
        private long mDuration = 1000;

        /**
         * The animator's time interpolator
         */
        private TimeInterpolator mInterpolator = new LinearInterpolator();

        /**
         * Instantiates a new Animator item.
         */
        public AnimatorItem(){

        }

        /**
         * Sets duration.
         *
         * @param duration the duration
         */
        public void setDuration(long duration) {
            mDuration = duration;
        }

        /**
         * Sets interpolator.
         *
         * @param interpolator the interpolator
         */
        public void setInterpolator(TimeInterpolator interpolator) {
            if (interpolator != null) {
                mInterpolator = interpolator;
            }
        }

        /**
         * get the item's current rect
         *
         * @return {@link RectF}
         */
        public RectF getCurrentRect() {
            return mCurrentRect;
        }

        /**
         * is the animator finished
         *
         * @return result boolean
         */
        public boolean isFinished() {
            return getInputValue() >= 1;
        }

        /**
         * get the progress value
         *
         * @return the progress
         */
        protected float getProgress() {
            float input = getInputValue();
            return mInterpolator.getInterpolation(input);
        }

        /**
         * get the input value
         *
         * @return valid value[0, 1]
         */
        protected float getInputValue() {
            return (System.currentTimeMillis() - mStartTime) * 1.f / mDuration;
        }

        /**
         * the item from attach to the running container
         */
        protected void onAttached() {
        }

        /**
         * perform the real drawing task
         *
         * @param canvas canvas
         * @param paint  paint
         */
        protected abstract void onDraw(Canvas canvas, Paint paint);

        /**
         * Random float.
         *
         * @param max the max
         * @return the float
         */
        public static float random(float max){
            return random(max, 0);
        }

        /**
         * Random float.
         *
         * @param max the max
         * @param min the min
         * @return the float
         */
        public static float random(float max, float min){
            return (float) (Math.random() * (max - min) + min);
        }

    }
}
