package com.yhao.floatwindow;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yhao.floatwindow.annotation.MoveType;
import com.yhao.floatwindow.impl.IFloatWindowImpl;
import com.yhao.floatwindow.interfaces.BaseFloatWindow;
import com.yhao.floatwindow.interfaces.ViewStateListener;
import com.yhao.floatwindow.permission.PermissionListener;
import com.yhao.floatwindow.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;



public class FloatWindow {

    private static final String DEFAULT_TAG = "default_float_window_tag";
    private static Map<String, BaseFloatWindow> mFloatWindowMap;
    @SuppressWarnings("unused")
    private static Builder mBuilder = null;

    private FloatWindow() {

    }

    public static BaseFloatWindow get() {
        return get(DEFAULT_TAG);
    }

    public static BaseFloatWindow get(String tag) {
        return mFloatWindowMap == null ? null : mFloatWindowMap.get(tag);
    }

    public static Builder with(Context context) {
        return mBuilder = new Builder(context);
    }

    /**
     * 销毁默认tag的
     */
    public static void destroy() {
        destroy(DEFAULT_TAG);
    }

    /**
     * 销毁指定tag的
     *
     * @param tag
     */
    public static void destroy(String tag) {
        if (mFloatWindowMap == null || !mFloatWindowMap.containsKey(tag)) {
            return;
        }
        mFloatWindowMap.get(tag).dismiss();
        mFloatWindowMap.get(tag).destory();
        mFloatWindowMap.remove(tag);
    }

    /**
     * 销毁全部
     */
    public static void destroyAll() {
        if (mFloatWindowMap != null) {
            for (BaseFloatWindow iFloatWindow : mFloatWindowMap.values()) {
                try {
                    iFloatWindow.dismiss();
                    iFloatWindow.destory();
                    mFloatWindowMap.remove(iFloatWindow);
                } catch (Throwable e) {
                    LogUtils.e(Log.getStackTraceString(e));
                }
            }
            mFloatWindowMap = null;
        }
    }

    /**
     * 支持链式调用的Builder类
     */
    public static class Builder {
        public Context mApplicationContext;
        public View mView;
        public int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        public int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        public int gravity = Gravity.TOP | Gravity.START;
        public int xOffset;
        public int yOffset;
        public boolean mShow = true;
        public Class<?>[] mActivities;
        public int mMoveType = MoveType.SLIDE;
        public int mSlideLeftMargin;
        public int mSlideRightMargin;
        public long mDuration = 300;
        public TimeInterpolator mInterpolator;
        public boolean mDesktopShow;
        public boolean mAuto=true;
        public PermissionListener mPermissionListener;
        public ViewStateListener mViewStateListener;
        public int mLayoutId;
        private String mTag = DEFAULT_TAG;

        @SuppressWarnings("unused")
        private Builder() {
        }

        Builder(Context applicationContext) {
            mApplicationContext = applicationContext;
        }

        public Builder setView(View view) {
            mView = view;
            return this;
        }

        public Builder setView(int layoutId) {
            mLayoutId = layoutId;
            return this;
        }

        public Builder setWidth(int width) {
            mWidth = width;
            return this;
        }

        public Builder setHeight(int height) {
            mHeight = height;
            return this;
        }


        public Builder setX(int x) {
            xOffset = x;
            return this;
        }

        public Builder setY(int y) {
            yOffset = y;
            return this;
        }


        /**
         * 设置 Activity 过滤器，用于指定在哪些界面显示悬浮窗，默认全部界面都显示
         *
         * @param show       过滤类型,子类类型也会生效
         * @param activities 过滤界面
         */
        public Builder setFilter(boolean show, Class<?>... activities) {
            mShow = show;
            mActivities = activities;
            return this;
        }

        public Builder setMoveType(@MoveType.MOVE_TYPE int moveType) {
            return setMoveType(moveType, 0, 0);
        }

        /**
         * 设置带边距的贴边动画，只有 moveType 为 MoveType.SLIDE，设置边距才有意义，这个方法不标准，后面调整
         *
         * @param moveType         贴边动画 MoveType.SLIDE
         * @param slideLeftMargin  贴边动画左边距，默认为 0
         * @param slideRightMargin 贴边动画右边距，默认为 0
         */
        public Builder setMoveType(@MoveType.MOVE_TYPE int moveType, int slideLeftMargin, int slideRightMargin) {
            mMoveType = moveType;
            mSlideLeftMargin = slideLeftMargin;
            mSlideRightMargin = slideRightMargin;
            return this;
        }

        public Builder setMoveStyle(long duration, TimeInterpolator interpolator) {
            mDuration = duration;
            mInterpolator = interpolator;
            return this;
        }

        public Builder setTag(String tag) {
            mTag = tag;
            return this;
        }

        public Builder setDesktopShow(boolean show) {
            mDesktopShow = show;
            return this;
        }

        public Builder setAuto(boolean auto) {
            mAuto = auto;
            return this;
        }

        public Builder setPermissionListener(PermissionListener listener) {
            mPermissionListener = listener;
            return this;
        }

        public Builder setViewStateListener(ViewStateListener listener) {
            mViewStateListener = listener;
            return this;
        }

        public void build() {
            if (mFloatWindowMap == null) {
                mFloatWindowMap = new HashMap<String, BaseFloatWindow>();
            }
            if (mFloatWindowMap.containsKey(mTag)) {
                return;
            }
            if (mView == null && mLayoutId == 0) {
                throw new IllegalArgumentException("View has not been set!");
            }
            if (mView == null) {
                LayoutInflater inflate = (LayoutInflater) mApplicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = inflate.inflate(mLayoutId, null);
            }
            BaseFloatWindow floatWindowImpl = new IFloatWindowImpl(this);
            mFloatWindowMap.put(mTag, floatWindowImpl);
            LogUtils.i("build windowView [" + mTag + "] success.");
        }
    }
}
