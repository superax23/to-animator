package com.iwhys.library.animator;

/**
 * Author:      iwhys
 * Email:       whs008@gmail.com
 * Time:        6/29/16 18:27
 * Description:
 */
public interface IAnimator {

    /**
     * Target size changed.
     *
     * @param width  the width
     * @param height the height
     */
    void targetSizeChanged(int width, int height);

    /**
     * Destroy.
     */
    void destroy();

    /**
     * Start
     *
     * @param holder the holder
     */
    void start(AnimatorHolder holder);

    /**
     * Stop
     */
    void stop();
}
