package com.whensunset.sticker.container.worker;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.element.WsElement;

/**
 * Created by whensunset on 2019/4/27.
 * 单指移动选中的 Element 的 Worker
 */

public class SingleFingerMoveElementWorker extends DefaultContainerWorker {
  private static final String TAG = "SFMEWorker";
  
  public SingleFingerMoveElementWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public void downTapSelectElementAction(@NonNull MotionEvent event) {
    mElementContainerView.setMode(ElementContainerView.BaseActionMode.SELECTED_CLICK_OR_MOVE);
  }
  
  @Override
  public void scrollTapSelectElementAction(@NonNull MotionEvent event, float[] distanceXY) {
    ElementContainerView.BaseActionMode mode = mElementContainerView.getMode();
    if (mode == ElementContainerView.BaseActionMode.SELECTED_CLICK_OR_MOVE
        || mode == ElementContainerView.BaseActionMode.SELECT
        || mode == ElementContainerView.BaseActionMode.MOVE) {
      if (mode == ElementContainerView.BaseActionMode.SELECTED_CLICK_OR_MOVE || mode == ElementContainerView.BaseActionMode.SELECT) {
        singleFingerMoveStartByContainerView(distanceXY[0], distanceXY[1]);
      } else {
        singleFingerMoveProcessByContainerView(distanceXY[0], distanceXY[1]);
      }
      mElementContainerView.update();
      mElementContainerView.setMode(ElementContainerView.BaseActionMode.MOVE);
      ;
    } else {
      Log.e(TAG, "scrollTapSelectElementAction error not this action!");
    }
  }
  
  @Override
  public void upTapSelectElementAction(@NonNull MotionEvent event) {
    if (mElementContainerView.getMode() == ElementContainerView.BaseActionMode.MOVE) {
      singleFingerMoveEndByContainerView(event);
    }
  }
  
  /**
   * 开始移动选中元素
   *
   * @param distanceX
   * @param distanceY
   */
  private void singleFingerMoveStartByContainerView(float distanceX, float distanceY) {
    WsElement selectElement = mElementContainerView.getSelectElement();
    if (selectElement == null) {
      Log.e(TAG, "singleFingerMoveStartByContainerView error select element is null!");
      return;
    }
    
    selectElement.onSingleFingerMoveStart();
    mElementContainerView.callContainerWorker(containerWorker -> containerWorker.singleFingerMoveStart(distanceX, distanceY));
    mElementContainerView.callListener(elementActionListener -> {
      ((SingleFingerMoveElementActionListener) elementActionListener).onSingleFingerMoveStart(selectElement);
    });
    Log.d(TAG, "singleFingerMoveStartByContainerView move start |||||||||| distanceX:" + distanceX + ",distanceY:" + distanceY);
  }
  
  /**
   * 选中元素移动中
   *
   * @param distanceX
   * @param distanceY
   */
  private void singleFingerMoveProcessByContainerView(float distanceX, float distanceY) {
    WsElement selectElement = mElementContainerView.getSelectElement();
    if (selectElement == null) {
      Log.e(TAG, "singleFingerMoveStartByContainerView error select element is null!");
      return;
    }
    
    selectElement.onSingleFingerMoveProcess(distanceX, distanceY);
    mElementContainerView.callContainerWorker(containerWorker -> containerWorker.singleFingerMoveProcess(distanceX, distanceY));
    mElementContainerView.callListener(elementActionListener -> {
      ((SingleFingerMoveElementActionListener) elementActionListener).onSingleFingerMoveProcess(selectElement);
    });
    Log.d(TAG, "singleFingerMoveProcessByContainerView move process |||||||||| distanceX:" + distanceX + ",distanceY:" + distanceY);
  }
  
  /**
   * 选中元素移动结束
   *
   * @param event
   */
  private void singleFingerMoveEndByContainerView(MotionEvent event) {
    WsElement selectElement = mElementContainerView.getSelectElement();
    if (selectElement == null) {
      Log.e(TAG, "singleFingerMoveStartByContainerView error select element is null!");
      return;
    }
    
    selectElement.onSingleFingerMoveEnd();
    mElementContainerView.callContainerWorker(containerWorker -> containerWorker.singleFingerMoveEnd(event));
    mElementContainerView.callListener(elementActionListener -> {
      ((SingleFingerMoveElementActionListener) elementActionListener).onSingleFingerMoveEnd(selectElement);
    });
    Log.d(TAG, "singleFingerMoveEndByContainerView move end |||||||||| event:" + event);
  }
  
  
  @Override
  public int getPriority() {
    return Integer.MAX_VALUE - 1;
  }
  
  public interface SingleFingerMoveElementActionListener extends ElementContainerView.ElementActionListener {
    /**
     * 选中了元素之后，对元素单指移动开始的回调
     *
     * @param element
     */
    void onSingleFingerMoveStart(WsElement element);
    
    /**
     * 选中了元素之后，对元素单指移动过程的回调
     *
     * @param element
     */
    void onSingleFingerMoveProcess(WsElement element);
    
    /**
     * 一次 单指移动操作结束的回调
     */
    void onSingleFingerMoveEnd(WsElement element);
  }
}
