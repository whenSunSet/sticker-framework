package com.whensunset.sticker.container.worker;

import android.util.Log;

import com.whensunset.sticker.MultiTouchGestureDetector;
import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.element.WsElement;

/**
 * Created by whensunset on 2019/4/27.
 * 双指旋转缩放选中的 element 的 worker
 */

public class DoubleFingerScaleAndRotateElementWorker extends DefaultContainerWorker {
  // 每个双指旋转事件能进行的最大角度，注意这里限制的是 delta 值，超过了这个值，就丢弃这个事件
  private static final String TAG = "WhenSunset:DFSAREW";
  private static final int DOUBLE_FINGER_ROTATE_THRESHOLD = 45;
  public DoubleFingerScaleAndRotateElementWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public void doubleFingerInSelectElementStart(MultiTouchGestureDetector detector) {
    doubleFingerScaleAndRotateStart(getDeltaRotateFromDetector(detector), detector.getScale());
  }
  
  @Override
  public void doubleFingerScaleAndRotateSelectElementAction(MultiTouchGestureDetector detector) {
    doubleFingerScaleAndRotateProcess(getDeltaRotateFromDetector(detector), detector.getScale());
  }
  
  @Override
  public void doubleFingerInSelectElementEnd(MultiTouchGestureDetector detector) {
    doubleFingerScaleAndRotateEnd();
  }
  
  @Override
  public void doubleFingerNotInSelectElementEnd(MultiTouchGestureDetector detector) {
    doubleFingerScaleAndRotateEnd();
  }
  
  private float getDeltaRotateFromDetector(MultiTouchGestureDetector detector) {
    float deltaRotate = detector.getRotation();
    if (deltaRotate <= 0) {
      deltaRotate = (Math.abs(deltaRotate) > DOUBLE_FINGER_ROTATE_THRESHOLD
          ? 0
          : deltaRotate);
    } else {
      deltaRotate = (deltaRotate > DOUBLE_FINGER_ROTATE_THRESHOLD
          ? 0
          : deltaRotate);
    }
    return deltaRotate;
  }
  
  /**
   * 开始双指旋转缩放
   * @param deltaRotate
   * @param deltaScale
   */
  protected void doubleFingerScaleAndRotateStart(float deltaRotate, float deltaScale) {
    WsElement selectElement = mElementContainerView.getSelectElement();
    if (selectElement == null) {
      Log.e(TAG, "doubleFingerScaleAndRotateStart error select element is null!");
      return;
    }
    
    selectElement.onDoubleFingerScaleAndRotateStart(deltaRotate, deltaScale);
    mElementContainerView.update();
    mElementContainerView.callListener(elementActionListener ->
        ((DoubleFingerScaleAndRotateElementActionListener)elementActionListener).onDoubleFingerScaleAndRotateStart(selectElement));
    Log.d(TAG, "doubleFingerScaleAndRotateStart start |||||||||| deltaRotate:" + deltaRotate + ",deltaScale:" + deltaScale);
  }
  
  /**
   * 双指旋转缩放进行中
   * @param deltaRotate
   * @param deltaScale
   */
  protected void doubleFingerScaleAndRotateProcess(float deltaRotate, float deltaScale) {
    WsElement selectElement = mElementContainerView.getSelectElement();
    if (selectElement == null) {
      Log.e(TAG, "doubleFingerScaleAndRotateStart error select element is null!");
      return;
    }
    selectElement.onDoubleFingerScaleAndRotateProcess(deltaRotate, deltaScale);
    mElementContainerView.update();
    mElementContainerView.callListener(elementActionListener ->
        ((DoubleFingerScaleAndRotateElementActionListener)elementActionListener).onDoubleFingerScaleAndRotateProcess(selectElement));
    Log.d(TAG, "doubleFingerScaleAndRotateProcess process |||||||||| deltaRotate:" + deltaRotate + ",deltaScale:" + deltaScale);
  }
  
  /**
   * 双指旋转缩放结束
   */
  protected void doubleFingerScaleAndRotateEnd() {
    WsElement selectElement = mElementContainerView.getSelectElement();
    if (selectElement == null) {
      Log.e(TAG, "doubleFingerScaleAndRotateStart error select element is null!");
      return;
    }
    selectElement.onDoubleFingerScaleAndRotateEnd();
    mElementContainerView.callListener(elementActionListener ->
        ((DoubleFingerScaleAndRotateElementActionListener)elementActionListener).onDoubleFingerScaleRotateEnd(selectElement));
    Log.d(TAG, "doubleFingerScaleAndRotateEnd end");
  }
  
  @Override
  public int getPriority() {
    return Integer.MAX_VALUE - 2;
  }
  
  public interface DoubleFingerScaleAndRotateElementActionListener extends ElementContainerView.ElementActionListener {
    /**
     * 选中了元素之后，对元素双指旋转缩放开始的回调
     *
     * @param element
     */
    void onDoubleFingerScaleAndRotateStart(WsElement element);
    
    /**
     * 选中了元素之后，对元素双指旋转缩放过程的回调
     *
     * @param element
     */
    void onDoubleFingerScaleAndRotateProcess(WsElement element);
    
    /**
     * 一次 双指旋转、缩放 操作结束的回调
     *
     * @param element
     */
    void onDoubleFingerScaleRotateEnd(WsElement element);
  }
}
