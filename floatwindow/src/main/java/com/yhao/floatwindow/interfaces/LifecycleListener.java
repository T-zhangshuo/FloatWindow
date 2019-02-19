package com.yhao.floatwindow.interfaces;

public interface LifecycleListener {

    void onShow();

    void onHide();

    void onBackToDesktop();

    void OrientationChange(int oldRotation, int newRotation);
}
