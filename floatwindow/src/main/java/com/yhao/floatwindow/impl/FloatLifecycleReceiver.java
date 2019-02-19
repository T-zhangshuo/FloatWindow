package com.yhao.floatwindow.impl;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.OrientationEventListener;

import com.yhao.floatwindow.interfaces.LifecycleListener;
import com.yhao.floatwindow.interfaces.ResumedListener;


public class FloatLifecycleReceiver extends BroadcastReceiver implements Application.ActivityLifecycleCallbacks {

    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static ResumedListener sResumedListener;
    private static int num = 0;
    private Class<?>[] activities;
    private boolean showFlag;
    private int startCount;
    private boolean appBackground;
    private LifecycleListener mLifecycleListener;
    private int mRotation = 0;
    private OrientationEventListener orientationEventListener;

    public FloatLifecycleReceiver(Context applicationContext, boolean showFlag, Class<?>[] activities,
                                  LifecycleListener lifecycleListener) {
        this.showFlag = showFlag;
        this.activities = activities;
        num++;
        mLifecycleListener = lifecycleListener;
        ((Application) applicationContext).registerActivityLifecycleCallbacks(this);
//        TODO 如果是其他地方打开，该startCount最好设置其值
//        startCount = 1;
        applicationContext.registerReceiver(this, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        startOrientationListener(applicationContext);

    }

    private void startOrientationListener(Context context) {
        orientationEventListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;
                int rotation = mRotation;
                if (orientation > 355 || orientation < 5) {
                    rotation = 0;
                } else if (orientation > 85 && orientation < 95) {
                    rotation = 90;
                } else if (orientation > 175 && orientation < 185) {
                    rotation = 180;
                } else if (orientation > 265 && orientation < 275) {
                    rotation = 270;
                }
                if (mRotation != rotation) {
                    mLifecycleListener.OrientationChange(mRotation, rotation);
                    mRotation = rotation;
                }
            }
        };
        orientationEventListener.enable();
    }

    public static void setResumedListener(ResumedListener resumedListener) {
        sResumedListener = resumedListener;
    }

    private boolean needShow(Activity activity) {
        if (activities == null) {
            return true;
        }
        for (Class<?> a : activities) {
            if (a.isInstance(activity)) {
                return showFlag;
            }
        }
        return !showFlag;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (sResumedListener != null) {
            num--;
            if (num == 0) {
                sResumedListener.onResumed();
                sResumedListener = null;
            }
        }
        if (needShow(activity)) {
            mLifecycleListener.onShow();
        } else {
            mLifecycleListener.onHide();
        }
        if (appBackground) {
            appBackground = false;
        }
    }

    @Override
    public void onActivityPaused(final Activity activity) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        startCount++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        startCount--;
        if (startCount == 0) {
            mLifecycleListener.onBackToDesktop();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                mLifecycleListener.onBackToDesktop();
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public void unRegisterReceiver(Context context) {
        ((Application) context).unregisterActivityLifecycleCallbacks(this);
        context.unregisterReceiver(this);
        if (orientationEventListener != null)
            orientationEventListener.disable();
    }
}
