package com.whensunset.sticker.container.worker;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

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
   * down 事件落在了空白的区域
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
  public abstract boolean scrollTapSelectElementPreAction(@NonNull MotionEvent event, float[] distanceXY);
  
  /**
   * scroll 事件落在了选中的 element 之外
   *
   * @param event      当前的触摸事件
   * @param distanceXY size 为 2，里面分别为 x 轴的 delta 位移，和 y 轴的 delta 位移
   * @return
   */
  public abstract void scrollTapUnSelectElementAction(@NonNull MotionEvent event, float[] distanceXY);
  
  /**
   * 开始移动选中元素
   *
   * @param distanceX
   * @param distanceY
   */
  public abstract void singleFingerMoveStart(float distanceX, float distanceY);
  
  /**
   * 选中元素移动中
   *
   * @param distanceX
   * @param distanceY
   */
  public abstract void singleFingerMoveProcess(float distanceX, float distanceY);
  
  /**
   * 选中元素移动结束
   */
  public abstract void singleFingerMoveEnd(MotionEvent event);
  
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
  public abstract boolean upTapSelectElementPreAction(MotionEvent event);
  
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
