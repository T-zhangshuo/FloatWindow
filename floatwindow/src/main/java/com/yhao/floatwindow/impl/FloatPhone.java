package com.yhao.floatwindow.impl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import com.yhao.floatwindow.FloatActivity;
import com.yhao.floatwindow.interfaces.BaseFloat;
import com.yhao.floatwindow.permission.PermissionListener;
import com.yhao.floatwindow.utils.LogUtils;
import com.yhao.floatwindow.utils.Miui;


public class FloatPhone extends BaseFloat {
    private final Context mContext;

    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private int mX, mY;
    private boolean isRemove = false;
    private PermissionListener mPermissionListener;

    public FloatPhone(Context applicationContext, PermissionListener permissionListener) {
        mContext = applicationContext;
        mPermissionListener = permissionListener;
        mWindowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.windowAnimations = 0;
    }

    @Override
    public void setSize(int width, int height) {
        mLayoutParams.width = width;
        mLayoutParams.height = height;
    }

    @Override
    public void setView(View view) {
        mView = view;
    }

    @Override
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mLayoutParams.gravity = gravity;
        mLayoutParams.x = mX = xOffset;
        mLayoutParams.y = mY = yOffset;
    }

    @Override
    public void init() {
        if (Build.VERSION.SDK_INT >= 25) {
            req();
        } else if (Miui.rom()) {
            if (Build.VERSION.SDK_INT >= 23) {
                req();
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                Miui.requestPermission(mContext, new PermissionListener() {
                    @Override
                    public void onSuccess() {
                        mWindowManager.addView(mView, mLayoutParams);
                        if (mPermissionListener != null) {
                            mPermissionListener.onSuccess();
                        }
                    }

                    @Override
                    public void onFail() {
                        if (mPermissionListener != null) {
                            mPermissionListener.onFail();
                        }
                    }
                });
            }
        } else {
            try {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                mWindowManager.addView(mView, mLayoutParams);
            } catch (Exception e) {
                mWindowManager.removeView(mView);
                LogUtils.e("TYPE_TOAST 失败");
                req();
            }
        }
    }

    private void req() {
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        if (Build.VERSION.SDK_INT >= 26) {
            mLayoutParams.type = 2038;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        FloatActivity.request(mContext, new PermissionListener() {
            @Override
            public void onSuccess() {
                mWindowManager.addView(mView, mLayoutParams);
                if (mPermissionListener != null) {
                    mPermissionListener.onSuccess();
                }
            }

            @Override
            public void onFail() {
                if (mPermissionListener != null) {
                    mPermissionListener.onFail();
                }
            }
        });
    }

    @Override
    public void dismiss() {
        isRemove = true;
        mWindowManager.removeView(mView);
    }

    @Override
    public void updateXY(int x, int y) {
        if (isRemove) {
            return;
        }
        mLayoutParams.x = mX = x;
        mLayoutParams.y = mY = y;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    public void updateX(int x) {
        if (isRemove) {
            return;
        }
        mLayoutParams.x = mX = x;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    public void updateY(int y) {
        if (isRemove) {
            return;
        }
        mLayoutParams.y = mY = y;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    public int getX() {
        return mX;
    }

    @Override
    public int getY() {
        return mY;
    }

}