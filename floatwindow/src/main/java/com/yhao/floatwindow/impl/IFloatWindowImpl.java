package com.yhao.floatwindow.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.annotation.MoveType;
import com.yhao.floatwindow.interfaces.BaseFloat;
import com.yhao.floatwindow.interfaces.BaseFloatWindow;
import com.yhao.floatwindow.interfaces.LifecycleListener;
import com.yhao.floatwindow.utils.ScreenUtils;


public class IFloatWindowImpl extends BaseFloatWindow {
    private int screenWidth, screenHeight;
    private FloatWindow.Builder mBuilder;
    private BaseFloat mFloatView;
    private FloatLifecycleReceiver mFloatLifecycle;
    private boolean isShow;
    private boolean once = true;
    private ValueAnimator mAnimator;
    private TimeInterpolator mDecelerateInterpolator;
    private float downX;
    private float downY;
    private float upX;
    private float upY;
    private boolean mClick = false;
    private int mSlop;
    private float offsetRateX = 0.05f, offsetRateY = 0.05f;

    @SuppressWarnings("unused")
    private IFloatWindowImpl() {
    }

    public IFloatWindowImpl(FloatWindow.Builder b) {
        mBuilder = b;
        screenWidth = ScreenUtils.getScreenWidth(b.mApplicationContext);
        screenHeight = ScreenUtils.getScreenHeight(b.mApplicationContext);
        if (mBuilder.mMoveType == MoveType.FIXED) {
            if (Build.VERSION.SDK_INT >= 25) {
                mFloatView = new FloatPhone(b.mApplicationContext, mBuilder.mPermissionListener);
            } else {
                mFloatView = new FloatToast(b.mApplicationContext);
            }
        } else {
            mFloatView = new FloatPhone(b.mApplicationContext, mBuilder.mPermissionListener);
            initTouchEvent();
        }
        mFloatView.setSize(mBuilder.mWidth, mBuilder.mHeight);
        mFloatView.setGravity(mBuilder.gravity, mBuilder.xOffset, mBuilder.yOffset);
        mFloatView.setView(mBuilder.mView);
        mFloatLifecycle = new FloatLifecycleReceiver(mBuilder.mApplicationContext, mBuilder.mShow,
                mBuilder.mActivities, new LifecycleListener() {
            @Override
            public void onShow() {
                if (!mBuilder.mAuto) return;
                show();
            }

            @Override
            public void onHide() {
                if (!mBuilder.mAuto) return;
                hide();
            }

            @Override
            public void onBackToDesktop() {
                if (!mBuilder.mAuto) return;
                if (mBuilder.mDesktopShow) {
                    show();
                } else {
                    hide();
                }
                if (mBuilder.mViewStateListener != null) {
                    mBuilder.mViewStateListener.onBackToDesktop();
                }
            }

            @Override
            public void OrientationChange(int oldRotation, int newRotation) {
                if (!mBuilder.mAuto) {
                    hide();
                    return;
                }
                if (isShow) {
                    float rateX = getX() * 1f / screenWidth;
                    float rateY = getY() * 1f / screenHeight;
                    if (newRotation == 0 || newRotation == 180) {
                        int maxValue = Math.max(screenWidth, screenHeight);
                        screenWidth = Math.min(screenWidth, screenHeight);
                        screenHeight = maxValue;
                    } else {
                        int maxValue = Math.max(screenWidth, screenHeight);
                        screenHeight = Math.min(screenWidth, screenHeight);
                        screenWidth = maxValue;
                    }
                    //按当前的x，和 y，比率，来进行计算位置
                    updateX((int) (rateX * screenWidth));
                    updateY((int) (rateY * screenHeight));

                    //开始进行贴边动画
                    animSlide(getView());
                }
            }
        });
    }


    @Override
    public void show() {
        if (once) {
            mFloatView.init();
            once = false;
            isShow = true;
        } else {
            if (isShow) {
                return;
            }
            getView().setVisibility(View.VISIBLE);
            isShow = true;
        }
        if (mBuilder.mViewStateListener != null) {
            mBuilder.mViewStateListener.onShow();
        }
    }

    @Override
    public void hide() {
        if (once || !isShow) {
            return;
        }
        getView().setVisibility(View.INVISIBLE);
        isShow = false;
        if (mBuilder.mViewStateListener != null) {
            mBuilder.mViewStateListener.onHide();
        }
    }

    @Override
    public boolean isShowing() {
        return isShow;
    }

    @Override
    public void dismiss() {
        mFloatView.dismiss();
        isShow = false;
        if (mBuilder.mViewStateListener != null) {
            mBuilder.mViewStateListener.onDismiss();
        }
    }

    @Override
    public void destory() {
        if (mFloatLifecycle != null) {
            mFloatLifecycle.unRegisterReceiver(mBuilder.mApplicationContext);
        }
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    @Override
    public void updateX(int x) {
        checkMoveType();
        mBuilder.xOffset = x;
        mFloatView.updateX(x);
    }

    @Override
    public void updateY(int y) {
        checkMoveType();
        mBuilder.yOffset = y;
        mFloatView.updateY(y);
    }

    @Override
    public int getX() {
        return mFloatView.getX();
    }

    @Override
    public int getY() {
        return mFloatView.getY();
    }

    @Override
    public View getView() {
        mSlop = ViewConfiguration.get(mBuilder.mApplicationContext).getScaledTouchSlop();
        return mBuilder.mView;
    }

    private void checkMoveType() {
        if (mBuilder.mMoveType == MoveType.FIXED) {
            throw new IllegalArgumentException("FloatWindow of this tag is not allowed to move!");
        }
    }

    private void initTouchEvent() {
        switch (mBuilder.mMoveType) {
            case MoveType.INACTIVE:
                break;
            default:
                getView().setOnTouchListener(new View.OnTouchListener() {
                    float lastX, lastY, changeX, changeY;
                    int newX, newY;

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                downX = event.getRawX();
                                downY = event.getRawY();
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                cancelAnimator();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (!isOutOfRange(event.getRawX(), event.getRawY())) {
                                    changeX = event.getRawX() - lastX;
                                    changeY = event.getRawY() - lastY;
                                    newX = (int) (mFloatView.getX() + changeX);
                                    newY = (int) (mFloatView.getY() + changeY);
                                    mFloatView.updateXY(newX, newY);
                                    if (mBuilder.mViewStateListener != null) {
                                        mBuilder.mViewStateListener.onPositionUpdate(newX, newY);
                                    }
                                    lastX = event.getRawX();
                                    lastY = event.getRawY();
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                upX = event.getRawX();
                                upY = event.getRawY();
                                mClick = (Math.abs(upX - downX) > mSlop) || (Math.abs(upY - downY) > mSlop);
                                switch (mBuilder.mMoveType) {
                                    case MoveType.SLIDE:
                                        animSlide(v);
                                        break;
                                    case MoveType.BACK:
                                        PropertyValuesHolder pvhX =
                                                PropertyValuesHolder.ofInt("x", mFloatView.getX(), mBuilder.xOffset);
                                        PropertyValuesHolder pvhY =
                                                PropertyValuesHolder.ofInt("y", mFloatView.getY(), mBuilder.yOffset);
                                        mAnimator = ObjectAnimator.ofPropertyValuesHolder(pvhX, pvhY);
                                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                int x = (Integer) animation.getAnimatedValue("x");
                                                int y = (Integer) animation.getAnimatedValue("y");
                                                mFloatView.updateXY(x, y);
                                                if (mBuilder.mViewStateListener != null) {
                                                    mBuilder.mViewStateListener.onPositionUpdate(x, y);
                                                }
                                            }
                                        });
                                        startAnimator();
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            default:
                                break;
                        }
                        return mClick;
                    }
                });
        }
    }

    private void cancelAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    private void animSlide(View view) {
        int startX = mFloatView.getX();
        int endX = (startX * 2 + view.getWidth() > screenWidth)
                ? screenWidth - view.getWidth()
                - mBuilder.mSlideRightMargin
                : mBuilder.mSlideLeftMargin;
        mAnimator = ObjectAnimator.ofInt(startX, endX);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int x = (Integer) animation.getAnimatedValue();
                mFloatView.updateX(x);
                if (mBuilder.mViewStateListener != null) {
                    mBuilder.mViewStateListener.onPositionUpdate(x, (int) upY);
                }
            }
        });
        startAnimator();
    }

    /**
     * @param x event.getRawX()
     * @param y event.getRawY()
     * @return
     */
    private boolean isOutOfRange(float x, float y) {
        boolean b = true;
        float widthRate, heightRate;
        widthRate = (screenWidth - x) / screenWidth;
        heightRate = (screenHeight - y) / screenHeight;
        if (widthRate > offsetRateX && heightRate > offsetRateY && widthRate < (1 - offsetRateX) && heightRate < (1 - offsetRateY)) {
            b = false;
        } else {
            b = true;
        }
        return b;
    }

    private void startAnimator() {
        if (mBuilder.mInterpolator == null) {
            if (mDecelerateInterpolator == null) {
                mDecelerateInterpolator = new DecelerateInterpolator();
            }
            mBuilder.mInterpolator = mDecelerateInterpolator;
        }
        mAnimator.setInterpolator(mBuilder.mInterpolator);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator.removeAllUpdateListeners();
                mAnimator.removeAllListeners();
                mAnimator = null;
                if (mBuilder.mViewStateListener != null) {
                    mBuilder.mViewStateListener.onMoveAnimEnd();
                }
            }
        });
        mAnimator.setDuration(mBuilder.mDuration).start();
        if (mBuilder.mViewStateListener != null) {
            mBuilder.mViewStateListener.onMoveAnimStart();
        }
    }


}
