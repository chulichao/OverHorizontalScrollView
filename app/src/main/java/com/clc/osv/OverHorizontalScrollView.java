package com.clc.osv;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;

public class OverHorizontalScrollView extends HorizontalScrollView {
    // 阻尼系数
    private static final int DAMPING_NUM = 2;
    // 回弹动画时间
    private static final int ANIM_DURATION = 200;
    // 子View
    private View mInnerView;
    // 上次MotionEvent的y坐标
    private float mLastX;
    // 一个矩形对象，用于记录子View的位置
    private Rect mRect;

    public OverHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        init();
        super.onFinishInflate();
    }

    private void init() {
        // 去除原本ScrollView的边界反馈
        setOverScrollMode(OVER_SCROLL_NEVER);
        mRect = new Rect();
        if (getChildAt(0) != null) {
            mInnerView = getChildAt(0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                // 松手恢复
                if (!mRect.isEmpty()) {
                    rebound();
                    mRect.setEmpty();
                }
                mLastX = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mLastX = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                float currentX = event.getX();
                int distanceX = (int) (mLastX - currentX);
                if (mLastX != 0 && (isToLeft() && distanceX < 0) || (isToRight() && distanceX > 0)) {
                    if (mRect.isEmpty()) {
                        // 保存正常的子view位置
                        mRect.set(mInnerView.getLeft(), mInnerView.getTop(), mInnerView.getRight(), mInnerView.getBottom());
                    }
                    // 设置滑动阻尼效果
                    mInnerView.layout(mInnerView.getLeft() - distanceX / DAMPING_NUM, mInnerView.getTop(),
                            mInnerView.getRight() - distanceX / DAMPING_NUM, mInnerView.getBottom());
                }
                mLastX = currentX;
                break;
            default:
                // Do nothing.
        }
        return super.onTouchEvent(event);
    }

    /**
     * 回弹动画
     */
    private void rebound() {
        TranslateAnimation animation = new TranslateAnimation(mInnerView.getLeft(), mRect.left, 0, 0);
        animation.setDuration(ANIM_DURATION);
        mInnerView.startAnimation(animation);
        // 补间动画并不会真正修改子view的位置，这里需要设置位置，使得子view回到正常的位置
        mInnerView.layout(mRect.left, mRect.top, mRect.right, mRect.bottom);
    }

    private boolean isToRight() {
        int offset = mInnerView.getMeasuredWidth() - getWidth();
        return getScrollX() == offset;
    }

    private boolean isToLeft() {
        return getScrollX() == 0;
    }
}
