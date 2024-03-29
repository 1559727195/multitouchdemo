package com.mundane.multitouchdemo.widget;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.OverScroller;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * 多点触控的弹性ScrollView
 */
public class ElasticScrollView extends ScrollView {
    private boolean canScroll;
    private GestureDetector mGestureDetector;
    private Point mOriginPos = new Point();
    private OverScroller mOverScroller;


    /*
     *    *invalidate->computeScroll->scrollTo->invalidate->draw->Scroller->invalidate
     *    *getScrollX():当前视图相对于屏幕原点在x轴上的偏移量
     *    *getScrollY():当前视图相对于屏幕原点在y轴上的偏移量
     *
     */
    public ElasticScrollView(Context context) {
        super(context);
        init(context);
    }

    public ElasticScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ElasticScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mOverScroller = new OverScroller(context, interpolator);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        canScroll = true;
        setVerticalScrollBarEnabled(false);
    }

    private View mTargetView;
    private float mLastY;
//    private Rect normal = new Rect();
    private Rect normal = new Rect();//Rect(0, 0 - 720, 384)
    private Rect normal_1 = new Rect();//Rect(0, 200 - 720, 584)

    @Override
    protected void onFinishInflate() {//xml加载完后执行
        super.onFinishInflate();
        if (getChildCount() > 0) {
            mTargetView = getChildAt(0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mTargetView != null) {
            mOriginPos.x = mTargetView.getLeft();
            mOriginPos.y = mTargetView.getTop();
        }
    }

    /*
     *    *invalidate->computeScroll->scrollTo->invalidate->draw->Scroller->invalidate
     *    *getScrollX():当前视图相对于屏幕原点在x轴上的偏移量
     *    *getScrollY():当前视图相对于屏幕原点在y轴上的偏移量
     *
     */


    class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e("robin debug", "onScroll");
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
            }
            return canScroll;
        }
    }

    private float startY, moveYY;
    private int activePointerId;
    /**
     * A null/invalid pointer ID.
     */
    private final int INVALID_POINTER = -1;


    /*	dispatchTouch
        onInterceptTouch
        dispatchTouch
        onInterceptTouch
        dispatchTouch
        dispatchTouch
        dispatchTouch
        dispatchTouch
        dispatchTouch
        dispatchTouch
        dispatchTouch
*/
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = ev.getPointerId(0);
                startY = mLastY = ev.getY(0);
                break;
        }
        Log.e("robin debug", "dispatchTouch");
        return super.dispatchTouchEvent(ev);


	/*	如果return super.dispatchTouchEvent(ev)，事件分发分为两种情况：

  如果当前View是ViewGroup,则事件会分发给onInterceptTouchEvent方法进行处理;

  如果当前View是普通View,则事件直接交给onTouchEvent方法进行处理*/

    }


    /*
     *    *invalidate->computeScroll->scrollTo->invalidate->draw->Scroller->invalidate
     *    *getScrollX():当前视图相对于屏幕原点在x轴上的偏移量
     *    *getScrollY():当前视图相对于屏幕原点在y轴上的偏移量
     *
     *    *真实改变 View 位置的方法有这么几种：
     *offsetLeftAndRight、offsetTopAndButtom
     *view.setLeft(left)、view.setRight(right)（属性动画同理）
     *LayoutParams
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("robin debug", "onTouchEvent");
        if (mTargetView == null) {
            return false;
        }

        final int action = event.getActionMasked();
        final int actionIndex = event.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 将新落下来那根手指作为活动手指
                activePointerId = event.getPointerId(actionIndex);
                mLastY = event.getY(actionIndex);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (activePointerId == event.getPointerId(actionIndex)) { // 如果松开的是活动手指
                    final int newPointerIndex = actionIndex == 0 ? 1 : 0;
                    activePointerId = event.getPointerId(newPointerIndex);
                    mLastY = event.getY(newPointerIndex);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointerId = INVALID_POINTER;
                if (mOnPullListener != null && mTargetView != null) {
                    // 往下拉的距离超过了100dp
                    if (mTargetView.getTop() - mOriginPos.y > dp2px(100)) {
                        mOnPullListener.onDownPull();
                    } else if (mOriginPos.y - mTargetView.getTop() > dp2px(100)) { // 往上拉的距离超过了100dp  mOriginPos.y - mTargetView.getTop() > dp2px(100
                        mOnPullListener.onUpPull();
                    }
                }
                if (!normal.isEmpty()) {//Rect(0, 0 - 720, 384)->normal
//                    mOverScroller.startScroll(mTargetView.getLeft(), mTargetView.getTop(), 0, mOriginPos.y - mTargetView.getTop(), 200);
//                    invalidate();
                    mOverScroller.startScroll(mTargetView.getLeft(),mTargetView.getTop(),0,mOriginPos.y - mTargetView.getTop(),200);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER) {
//					int activePointerIndex = event.getPointerId(0) == INVALID_POINTER ? 1 : 0;
                    Log.d("ACTION_MOVE", "无效手指");
                    activePointerId = event.getPointerId(actionIndex);
                }
                final int pointerIndex = event.findPointerIndex(activePointerId);
                float currentY = event.getY(pointerIndex);
                // 假如是下拉, currentY > perY, offset > 0
                int offset = (int) (currentY - mLastY);
                mLastY = currentY;
                int deltaY = Math.abs(mTargetView.getTop() - mOriginPos.y);
                Log.e("robin debug", "deltaY = " + deltaY);
                if (isNeedMove()) {
                    if (normal.isEmpty()) {
                        normal.set(mTargetView.getLeft(), mTargetView.getTop(), mTargetView.getRight(), mTargetView.getBottom());
                    }
                    normal_1.set(mTargetView.getLeft(),mTargetView.getTop(),mTargetView.getRight(),mTargetView.getBottom());

                    int offset_1 = calcateNewOffset(deltaY, offset);
                    Log.e("robin debug", "offset_1 = " + offset_1);
                    ViewCompat.offsetTopAndBottom(mTargetView, offset_1);//真实移动距离。
                }
            default:
                break;
        }
        return true;
    }

    /**
     * 判断ScrollView能否继续下拉, false表示已经到了最顶端, 无法继续下拉
     *
     * @param view
     * @return
     */
    public boolean canChildScrollUp(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(view, -1) || view.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, -1);
        }
    }

    /**
     * 判断View是否可以继续上拉, false表示已经达到了最低端, 无法再继续上拉
     *
     * @return canChildScrollDown
     */
    public boolean canChildScrollDown(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0 && absListView.getAdapter() != null
                        && (absListView.getLastVisiblePosition() < absListView.getAdapter().getCount() - 1 || absListView.getChildAt(absListView.getChildCount() - 1)
                        .getBottom() < absListView.getPaddingBottom());
            } else {
                return ViewCompat.canScrollVertically(view, 1) || view.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, 1);
        }
    }

    /**
     * 每隔多少距离就开始增大阻力系数, 数值越小阻力就增大的越快
     */
    private final int LENGTH = 150;
    /**
     * 阻力系数, 越大越难拉
     */
    private int mFraction = 2;

    private int calcateNewOffset(int deltaY, int offset) {
        int newOffset = offset / (mFraction + deltaY / LENGTH);
        if (newOffset == 0) {
            newOffset = offset >= 0 ? 1 : -1;
        }
        return newOffset;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        Log.e("robin debug","computeScroll");
        if (mOverScroller.computeScrollOffset()) {
            ViewCompat.offsetTopAndBottom(mTargetView,mOverScroller.getCurrY() - mTargetView.getTop());//ViewCompat.offsetTopAndBottom()为真实上下移动方法；
            invalidate();
        }
    }

    public boolean isNeedMove() {
        int offset = mTargetView.getMeasuredHeight() - getHeight();
        int scrollY = getScrollY();
        if (scrollY == 0 || scrollY == offset) {
            return true;
        }
        return false;
    }

    public interface OnPullListener {
        void onDownPull();

        void onUpPull();
    }

    private OnPullListener mOnPullListener;

    public void setOnPullListener(OnPullListener listener) {
        mOnPullListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            canScroll = true;
        }
        Log.e("robin debug", "onInterceptTouch");
        return mGestureDetector.onTouchEvent(ev);//接管目标view的onTouchEvent方法
    }

    private float dp2px(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
	
