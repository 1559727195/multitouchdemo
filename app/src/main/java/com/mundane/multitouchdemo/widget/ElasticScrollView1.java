package com.mundane.multitouchdemo.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * 只有单点触控的弹性ScrollView
 */
public class ElasticScrollView1 extends ScrollView {
    Context mContext;
    private boolean canScroll;
    private GestureDetector mGestureDetector;

    public ElasticScrollView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ElasticScrollView1(Context context) {
        super(context);
        init(context);
    }

    private void init(Context c) {
        mContext = c;
        mGestureDetector = new GestureDetector(new YScrollDetector());
        canScroll = true;
        setVerticalScrollBarEnabled(false);
    }

    private View inner;
    private float y;
    private Rect normal = new Rect();
    private boolean isCount = false;

    @Override
    protected void onFinishInflate() {//onFinishInflate的作用，就是在xml加载组件完成后调用的。
        // 这个方法一般在自制ViewGroup的时候调用。
        super.onFinishInflate();
        if (getChildCount() > 0) {
            inner = getChildAt(0);
        }
    }

    class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (canScroll)
                if (Math.abs(distanceY) >= Math.abs(distanceX))
                    canScroll = true;
                else
                    canScroll = false;

            if (canScroll) {
                if (e1.getAction() == MotionEvent.ACTION_DOWN) {
                    moveYY = 0;
                    startY = e1.getY();
                }
                Log.e("robin debug", "onScroll-start");
            }
            Log.e("robin debug", "onScrolling");

            return canScroll;
        }
    }

    double startY, moveYY;


    /*
    *拖动时执行的事件
    onIntercept
    onIntercept
    onScroll-start
    onScrolling
    onTouchEvent-ACTION_MOVE
    onTouchEvent-ACTION_MOVE
    onTouchEvent-ACTION_MOVE
    onTouchEvent-ACTION_MOVE
    onTouchEvent-ACTION_UP*/
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (inner != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    startY = ev.getY();
                    Log.e("robin debug", "onTouchEvent-ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    switch (action) {
                        case MotionEvent.ACTION_UP:
                            Log.e("robin debug", "onTouchEvent-ACTION_UP");
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            Log.e("robin debug", "onTouchEvent-ACTION_CANCEL");
                            break;
                    }
                    if (isNeedAnimation()) {
                        animation();
                        isCount = false;
                    }

                    if (startY < moveYY - dp2px(100)) {
                        if (onPullListener != null) {
                            onPullListener.onDownPull();
                        }
                    } else if (startY - moveYY > dp2px(100)) {
                        if (onPullListener != null) {
                            onPullListener.onUpPull();
                        }
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.e("robin debug", "onTouchEvent-ACTION_MOVE");
                    final float preY = y;
                    float nowY = ev.getY();
                    int deltaY = (int) (preY - nowY);
                    if (!isCount) {
                        deltaY = 0;
                    }

                    y = nowY;
                    if (isNeedMove()) {
                        if (normal.isEmpty()) {//记住第一次的原来的位置
                           normal.set(inner.getLeft(),inner.getTop(),inner.getRight(),inner.getBottom());
                        }
                        inner.layout(inner.getLeft(), inner.getTop() - deltaY / 2,
                                inner.getRight(), inner.getBottom() - deltaY / 2);
                        Log.e("robin debug", "isNeedMove");
                    }
                    isCount = true;
                    moveYY = ev.getY();
                default:
                    break;
            }
        }

        return true;
    }

    public void animation() {
        TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(),
                normal.top);
        ta.setDuration(200);
        ta.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                // TODO Auto-generated method stub

            }
        });
        inner.startAnimation(ta);
        inner.layout(normal.left,normal.top,normal.right,normal.bottom);//Rect(0, 0 - 720, 384)

        normal.setEmpty();
    }

    public boolean isNeedAnimation() {
        return !normal.isEmpty();
    }

    public boolean isNeedMove() {//inner.getMeasuredHeight():384,getHeight():1118,scrollY:0,offset:-734
        int offset = inner.getMeasuredHeight() - getHeight();
        int scrollY = getScrollY();


        Log.e("robin debug1", "inner.getMeasuredHeight():" + inner.getMeasuredHeight()
                + ",getHeight():" + getHeight() + ",scrollY:" + scrollY + ",offset:" + offset);
        if (scrollY == 0 || scrollY == offset) {
            return true;
        }
        return false;
    }

    public interface OnPullListener {
        public void onDownPull();

        public void onUpPull();
    }

    OnPullListener onPullListener = null;

    public void setOnPullListener(OnPullListener listener) {
        onPullListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            canScroll = true;
        }
        Log.e("robin debug", "onIntercept");
        boolean istrue = mGestureDetector.onTouchEvent(ev);
        return istrue;//为false
//        如果return true，则表示拦截该事件，并将事件交给当前View的onTouchEvent方法；
//
//    如果return false，则表示不拦截该事件，并将该事件交由子View的dispatchTouchEvent方法进行事件分发，重复上述过程；
//
//    如果return super.onInterceptTouchEvent(ev)， 事件拦截分两种情况: 
//
//如果该View(ViewGroup)存在子View且点击到了该子View, 则不拦截, 继续分发给子View 处理, 此时相当于return false。
//
//如果该View(ViewGroup)没有子View或者有子View但是没有点击中子View(此时ViewGroup相当于普通View), 则交由该View的onTouchEvent响应，此时相当于return true。 

    }

    private float dp2px(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
	
