package com.example.market.viewUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.example.market.utils.AndroidUtilities;

public abstract class BasicPopup<V extends View> implements DialogInterface.OnDismissListener, DialogInterface.OnKeyListener {
  public Activity activity;
  
  private FrameLayout contentLayout;
  
  private Dialog dialog;
  
  private boolean isPrepared;
  
  private int mWindowHeight;
  
  protected int screenHeightPixels;
  
  protected int screenWidthPixels;
  
  public BasicPopup(Activity paramActivity) {
    this.activity = paramActivity;
    DisplayMetrics displayMetrics = paramActivity.getResources().getDisplayMetrics();
    this.screenWidthPixels = displayMetrics.widthPixels;
    this.screenHeightPixels = displayMetrics.heightPixels;
    this.mWindowHeight = (int) AndroidUtilities.dip2px(paramActivity, 280.0F);
    initDialog();
  }
  
  private void initDialog() {
    this.contentLayout = new FrameLayout((Context)this.activity);
    this.contentLayout.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
    this.contentLayout.setFocusable(true);
    this.contentLayout.setFocusableInTouchMode(true);
    this.dialog = new Dialog((Context)this.activity);
    this.dialog.setCanceledOnTouchOutside(true);
    this.dialog.setCancelable(true);
    this.dialog.setOnKeyListener((DialogInterface.OnKeyListener)this);
    this.dialog.setOnDismissListener((DialogInterface.OnDismissListener)this);
    Window window = this.dialog.getWindow();
    if (window != null) {
      window.setGravity(80);
      window.setBackgroundDrawable((Drawable)new ColorDrawable(0));
      window.requestFeature(1);
      window.setContentView((View)this.contentLayout);
    } 
    setSize(this.screenWidthPixels, this.mWindowHeight);
  }
  
  public void dismiss() {
    dismissImmediately();
  }
  
  protected final void dismissImmediately() {
    dialog.dismiss();
  }
  
  public View getContentView() {
    return this.contentLayout.getChildAt(0);
  }
  
  public Context getContext() {
    return this.dialog.getContext();
  }
  
  public ViewGroup getRootView() {
    return (ViewGroup)this.contentLayout;
  }
  
  public int getScreenHeightPixels() {
    return this.screenHeightPixels;
  }
  
  public int getScreenWidthPixels() {
    return this.screenWidthPixels;
  }
  
  public Window getWindow() {
    return this.dialog.getWindow();
  }
  
  public boolean isShowing() {
    return this.dialog.isShowing();
  }
  
  protected abstract V makeContentView();
  
  public boolean onBackPress() {
    dismiss();
    return false;
  }
  
  public void onDismiss(DialogInterface paramDialogInterface) {
    dismiss();
  }
  
  public final boolean onKey(DialogInterface paramDialogInterface, int paramInt, KeyEvent paramKeyEvent) {
    if (paramKeyEvent.getAction() == 0 && paramInt == 4)
      onBackPress(); 
    return false;
  }
  
  public void setAnimationStyle(int paramInt) {
    Window window = this.dialog.getWindow();
    if (window != null)
      window.setWindowAnimations(paramInt); 
  }
  
  public void setCancelable(boolean paramBoolean) {
    this.dialog.setCancelable(paramBoolean);
  }
  
  public void setCanceledOnTouchOutside(boolean paramBoolean) {
    this.dialog.setCanceledOnTouchOutside(paramBoolean);
  }
  
  public void setContentView(View paramView) {
    this.contentLayout.removeAllViews();
    this.contentLayout.addView(paramView);
  }
  
  protected void setContentViewAfter(V paramV) {}
  
  protected void setContentViewBefore() {}
  
  public void setFillScreen(boolean paramBoolean) {
    if (paramBoolean)
      setSize(this.screenWidthPixels, (int)(this.screenHeightPixels * 0.85F)); 
  }
  
  public void setFitsSystemWindows(boolean paramBoolean) {
    this.contentLayout.setFitsSystemWindows(paramBoolean);
  }
  
  public void setGravity(int paramInt) {
    Window window = this.dialog.getWindow();
    if (window != null)
      window.setGravity(paramInt); 

  }
  
  public void setHalfScreen(boolean paramBoolean) {
    if (paramBoolean)
      setSize(this.screenWidthPixels, this.screenHeightPixels / 2); 
  }
  public void setSize(int a,int v){}
  public void setHeight(int paramInt) {
    setSize(0, paramInt);
  }
  
  public void setOnDismissListener(final DialogInterface.OnDismissListener onDismissListener) {
    this.dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
          @Override
          public void onDismiss(DialogInterface param1DialogInterface) {
            com.example.market.viewUtils.BasicPopup.this.onDismiss(param1DialogInterface);
            onDismissListener.onDismiss(param1DialogInterface);
          }
        });
  }
  
  public void setOnKeyListener(final DialogInterface.OnKeyListener onKeyListener) {
    this.dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface param1DialogInterface, int param1Int, KeyEvent param1KeyEvent) {
            com.example.market.viewUtils.BasicPopup.this.onKey(param1DialogInterface, param1Int, param1KeyEvent);
            return onKeyListener.onKey(param1DialogInterface, param1Int, param1KeyEvent);
          }
        });
  }
  
  public void setPrepared(boolean paramBoolean) {
    this.isPrepared = paramBoolean;
  }

  public final void show() {
    if (this.isPrepared && !this.activity.isFinishing()) {
      this.dialog.show();
      showAfter();
      return;
    } 
    setContentViewBefore();
    V v = makeContentView();
    setContentView((View)v);
    setContentViewAfter(v);
    this.isPrepared = true;
    this.dialog.show();
    showAfter();
  }
  
  protected void showAfter() {}

}
