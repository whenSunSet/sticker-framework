package com.whensunset.sticker.container.worker;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.whensunset.sticker.MultiTouchGestureDetector;
import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.element.WsElement;

/**
 * Created by whensunset on 2019/4/27.
 * 默认的 worker 空实现
 */

public abstract class DefaultContainerWorker extends ContainerWorker {
  
  public DefaultContainerWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public void viewLayoutComplete() {
  
  }
  
  @Override
  public void addElement(WsElement element) {
  
  }
  
  @Override
  public void deleteElement(WsElement element) {
  
  }
  
  @Override
  public void selectElement(WsElement element) {
  
  }
  
  @Override
  public void unSelectElement() {
  
  }
  
  @Override
  public void downTapSelectElementAction(@NonNull MotionEvent event) {
  
  }
  
  @Override
  public void downTapSelectElementPreAction(@NonNull MotionEvent event) {
  
  }
  
  @Override
  public void downTapBlank(@NonNull MotionEvent event) {
  
  }
  
  @Override
  public void downTapUnSelectElementAction(@NonNull MotionEvent event, WsElement clickedElement) {
  
  }
  
  @Override
  public void scrollTapSelectElementAction(@NonNull MotionEvent event, float[] distanceXY) {
  
  }
  
  @Override
  public void scrollTapSelectElementPreAction(@NonNull MotionEvent event, float[] distanceXY) {
  }
  
  @Override
  public void scrollTapOutOfSelectElementAction(@NonNull MotionEvent event, float[] distanceXY) {
  
  }
  
  @Override
  public void upTapSelectElementAction(@NonNull MotionEvent event) {
  
  }
  
  @Override
  public void upTapBlank(@NonNull MotionEvent event) {
  
  }
  
  @Override
  public void upTapUnSelectElementAction(@NonNull MotionEvent event, WsElement clickedElement) {
  
  }
  
  @Override
  public void upTapSelectElementPreAction(MotionEvent event) {
  }
  
  @Override
  public void doubleFingerInSelectElementStart(MultiTouchGestureDetector detector) {
  
  }
  
  @Override
  public void doubleFingerNotInSelectElementStart(MultiTouchGestureDetector detector) {
  
  }
  
  @Override
  public void doubleFingerScaleAndRotateSelectElementAction(MultiTouchGestureDetector detector) {
  
  }
  
  @Override
  public void doubleFingerScaleAndRotateSelectElementPreAction(MultiTouchGestureDetector detector) {
  
  }
  
  @Override
  public void doubleFingerScaleAndRotateUnSelectElementAction(MultiTouchGestureDetector detector) {
  
  }
  
  @Override
  public void doubleFingerInSelectElementEnd(MultiTouchGestureDetector detector) {
  
  }
  
  @Override
  public void doubleFingerNotInSelectElementEnd(MultiTouchGestureDetector detector) {
  
  }
  
  @Override
  public void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
  
  }
  
  @Override
  public void dispatchTouchEvent(MotionEvent ev) {
  
  }
}
