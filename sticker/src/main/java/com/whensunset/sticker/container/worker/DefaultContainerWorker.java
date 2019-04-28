package com.whensunset.sticker.container.worker;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

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
  public boolean scrollTapSelectElementPreAction(@NonNull MotionEvent event, float[] distanceXY) {
    return false;
  }
  
  @Override
  public void scrollTapUnSelectElementAction(@NonNull MotionEvent event, float[] distanceXY) {
  
  }
  
  @Override
  public void singleFingerMoveStart(float distanceX, float distanceY) {
  
  }
  
  @Override
  public void singleFingerMoveProcess(float distanceX, float distanceY) {
  
  }
  
  @Override
  public void singleFingerMoveEnd(MotionEvent event) {
  
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
  public boolean upTapSelectElementPreAction(MotionEvent event) {
    return false;
  }
  
  @Override
  public void dispatchTouchEvent(MotionEvent ev) {
  
  }
}
