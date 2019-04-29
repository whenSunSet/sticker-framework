package com.whensunset.sticker.container.worker;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.whensunset.sticker.MultiTouchGestureDetector;
import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.container.worker.DefaultContainerWorker;
import com.whensunset.sticker.element.WsElement;

/**
 * Created by whensunset on 2019/4/28.
 * 自动取消选中当前 element 的 worker
 */

public class AutoUnSelectElementWorker extends DefaultContainerWorker {
  private long mAutoUnSelectDuration = 2000; // 自动取消选中的时间，默认 2000 毫秒，
  private Runnable mUnSelectRunnable = this::unSelectElement;
  
  public AutoUnSelectElementWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public void downTapSelectElementAction(@NonNull MotionEvent event) {
    cancelAutoUnSelectDecoration();
  }
  
  @Override
  public void downTapSelectElementPreAction(@NonNull MotionEvent event) {
    cancelAutoUnSelectDecoration();
  }
  
  @Override
  public void downTapBlank(@NonNull MotionEvent event) {
    cancelAutoUnSelectDecoration();
  }
  
  @Override
  public void downTapUnSelectElementAction(@NonNull MotionEvent event, WsElement clickedElement) {
    cancelAutoUnSelectDecoration();
  }
  
  @Override
  public void doubleFingerInSelectElementStart(MultiTouchGestureDetector detector) {
    cancelAutoUnSelectDecoration();
  }
  
  @Override
  public void doubleFingerNotInSelectElementStart(MultiTouchGestureDetector detector) {
    cancelAutoUnSelectDecoration();
  }
  
  @Override
  public void upTapSelectElementAction(@NonNull MotionEvent event) {
    autoUnSelectDecoration();
  }
  
  @Override
  public void upTapBlank(@NonNull MotionEvent event) {
    autoUnSelectDecoration();
  }
  
  @Override
  public void upTapUnSelectElementAction(@NonNull MotionEvent event, WsElement clickedElement) {
    autoUnSelectDecoration();
  }
  
  @Override
  public void upTapSelectElementPreAction(MotionEvent event) {
    autoUnSelectDecoration();
  }
  
  @Override
  public void doubleFingerInSelectElementEnd(MultiTouchGestureDetector detector) {
    autoUnSelectDecoration();
  }
  
  @Override
  public void doubleFingerNotInSelectElementEnd(MultiTouchGestureDetector detector) {
    autoUnSelectDecoration();
  }
  
  @Override
  public void addElement(WsElement element) {
    autoUnSelectDecoration();
  }
  
  /**
   * 一定的时间之后自动取消当前元素的选中
   */
  public void autoUnSelectDecoration() {
    cancelAutoUnSelectDecoration();
    mElementContainerView.postDelayed(mUnSelectRunnable, mAutoUnSelectDuration);
  }
  
  /**
   * 取消自动取消选中
   */
  public void cancelAutoUnSelectDecoration() {
    mElementContainerView.removeCallbacks(mUnSelectRunnable);
  }
  
  
  @Override
  public int getPriority() {
    return 0;
  }
}
