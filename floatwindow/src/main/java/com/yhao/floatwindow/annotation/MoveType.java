package com.yhao.floatwindow.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class MoveType {
    //不可以移动
    public static final int FIXED = 0;
    //不能点击不能移动
    public static final int INACTIVE = 1;
    //随手指位置移动到某处
    public static final int ACTIVE = 2;
    //释放后自动回到边缘
    public static final int SLIDE = 3;
    //移动后自动回到默认位置
    public static final int BACK = 4;

    @IntDef({FIXED, INACTIVE, ACTIVE, SLIDE, BACK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MOVE_TYPE {}
}
