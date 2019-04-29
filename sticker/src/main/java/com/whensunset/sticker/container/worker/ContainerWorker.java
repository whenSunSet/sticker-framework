package com.whensunset.sticker.container.worker;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.whensunset.sticker.MultiTouchGestureDetector;
import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.element.WsElement;

/**
 * Created by whensunset on 2019/4/26.
 * 用于给 ContainerView 增加各种功能
 */

public abstract class ContainerWorker implements Comparable {
  protected ElementContainerView mElementContainerView;
  
  public ContainerWorker() {
  }
  
  public ContainerWorker(ElementContainerView elementContainerView) {
    mElementContainerView = elementContainerView;
  }
  
  /**
   * container view layout 完毕时的回调
   */
  public abstract void viewLayoutComplete();
  
  /**
   * 添加一个元素
   *
   * @param element 被添加的元素
   */
  public abstract void addElement(WsElement element);
  
  /**
   * 删除一个元素
   *
   * @param element 被删除的元素
   */
  public abstract void deleteElement(WsElement element);
  
  /**
   * 选中一个元素
   *
   * @param element 被选中的元素
   */
  public abstract void selectElement(WsElement element);
  
  /**
   * 取消选中当前元素
   *
   */
  public abstract void unSelectElement();
  
  /**
   * 抛事件
   * @param e1
   * @param e2
   * @param velocityX
   * @param velocityY
   * @return
   */
  public abstract void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
  
  /**
   * down 事件落在了选中的 element 中，如果 downTapSelectElementPreAction 被调用了这个就不会被调用
   *
   * @param event
   */
  public abstract void downTapSelectElementAction(@NonNull MotionEvent event);
  
  /**
   * down 事件落在了选中的 element 中，优先调用的方法
   *
   * @return
   */
  public abstract void downTapSelectElementPreAction(@NonNull MotionEvent event);
  
  /**
   * down 事件落在了无 element 区域
   *
   * @param event
   */
  public abstract void downTapBlank(@NonNull MotionEvent event);
  
  /**
   * down 事件落在了某个没有选中的 element 中
   *
   * @param event
   * @param clickedElement
   */
  public abstract void downTapUnSelectElementAction(@NonNull MotionEvent event, WsElement clickedElement);
  
  /**
   * scroll 事件落在了选中的 element 中，如果 scrollTapSelectElementPreAction 被调用了这个就不会被调用
   *
   * @param event      当前的触摸事件
   * @param distanceXY size 为 2，里面分别为 x 轴的 delta 位移，和 y 轴的 delta 位移
   * @return
   */
  public abstract void scrollTapSelectElementAction(@NonNull MotionEvent event, float[] distanceXY);
  
  /**
   * scroll 事件落在了选中的 element 中，优先调用的方法
   *
   * @param event      当前的触摸事件
   * @param distanceXY size 为 2，里面分别为 x 轴的 delta 位移，和 y 轴的 delta 位移，更改了其中的数据可以影响移动
   * @return
   */
  public abstract void scrollTapSelectElementPreAction(@NonNull MotionEvent event, float[] distanceXY);
  
  /**
   * scroll 事件落在了选中的 element 之外
   *
   * @param event      当前的触摸事件
   * @param distanceXY size 为 2，里面分别为 x 轴的 delta 位移，和 y 轴的 delta 位移
   * @return
   */
  public abstract void scrollTapOutOfSelectElementAction(@NonNull MotionEvent event, float[] distanceXY);
  
  /**
   * up 事件落在了选中的 element 中，如果 upTapSelectElementPreAction 被调用了这个就不会被调用
   *
   * @param event
   */
  public abstract void upTapSelectElementAction(@NonNull MotionEvent event);
  
  /**
   * up 事件落在选中的 element 中，优先调用的方法
   *
   * @param event
   * @return
   */
  public abstract void upTapSelectElementPreAction(MotionEvent event);
  
  /**
   * up 事件落在了空白的区域
   *
   * @param event
   */
  public abstract void upTapBlank(@NonNull MotionEvent event);
  
  /**
   * up 事件落在了某个没有选中的 element 中
   *
   * @param event
   * @param clickedElement
   */
  public abstract void upTapUnSelectElementAction(@NonNull MotionEvent event, WsElement clickedElement);
  
  /**
   * 双指手势开始时落在选中元素中
   *
   * @param detector
   */
  public abstract void doubleFingerInSelectElementStart(MultiTouchGestureDetector detector);
  
  /**
   * 双指手势开始时没有落在选中的 element 中
   *
   * @param detector
   */
  public abstract void doubleFingerNotInSelectElementStart(MultiTouchGestureDetector detector);
  
  /**
   * 双指旋转缩放事件落在了选中的 element 中，如果 doubleFingerScaleAndRotateSelectElementPreAction 被调用了这个方法就不会别调用
   */
  public abstract void doubleFingerScaleAndRotateSelectElementAction(MultiTouchGestureDetector detector);
  
  /**
   * 双指旋转缩放事件落在了选中的 element 中，优先调用的方法
   */
  public abstract void doubleFingerScaleAndRotateSelectElementPreAction(MultiTouchGestureDetector detector);
  
  /**
   * 双指旋转缩放事件没有落在选中的 element 中
   */
  public abstract void doubleFingerScaleAndRotateUnSelectElementAction(MultiTouchGestureDetector detector);
  
  /**
   * 双指手势结束时落在选中的 element 中
   *
   * @param detector
   */
  public abstract void doubleFingerInSelectElementEnd(MultiTouchGestureDetector detector);
  
  /**
   * 双指手势结束时没有落在选中的 element 中
   *
   * @param detector
   */
  public abstract void doubleFingerNotInSelectElementEnd(MultiTouchGestureDetector detector);
  
  public abstract void dispatchTouchEvent(MotionEvent ev);
  
  public abstract int getPriority();
  
  public void setElementContainerView(ElementContainerView elementContainerView) {
    mElementContainerView = elementContainerView;
  }
  
  @Override
  public int compareTo(@NonNull Object o) {
    ContainerWorker containerWorker = (ContainerWorker) o;
    return getPriority() - containerWorker.getPriority();
  }
}
