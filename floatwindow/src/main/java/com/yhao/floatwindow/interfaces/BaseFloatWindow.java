package com.yhao.floatwindow.interfaces;

import android.view.View;


public abstract class BaseFloatWindow {
    public abstract void show();

    public abstract void hide();

    public abstract boolean isShowing();

    public abstract int getX();

    public abstract int getY();

    public abstract void updateX(int x);

    public abstract void updateY(int y);

    public abstract View getView();

    public abstract void dismiss();

    public abstract void destory();
}
