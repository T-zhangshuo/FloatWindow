package com.yhao.floatwindow.interfaces;

public interface ViewStateListener {
    public void onPositionUpdate(int x, int y);

    public void onShow();

    public void onHide();

    public void onDismiss();

    public void onMoveAnimStart();

    public void onMoveAnimEnd();

    public void onBackToDesktop();
}
