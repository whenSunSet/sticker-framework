package com.whensunset.sticker.container.worker;

import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.container.worker.DefaultContainerWorker;
import com.whensunset.sticker.element.WsElement;

import static com.whensunset.sticker.CommonUtil.copyMotionEvent;

/**
 * Created by whensunset on 2019/4/28.
 * 对选中的 Element 再次点击后进行事件分发的 worker
 */

public class SelectedClickElementWorker extends DefaultContainerWorker {
  private static final String TAG = "WhenSunset:SCEW";
  
  private MotionEvent[] mUpDownMotionEvent = new MotionEvent[2]; // 储存当前 up down 事件，以便在需要的时候进行事件分发
  
  public SelectedClickElementWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public void dispatchTouchEvent(MotionEvent event) {
    WsElement selectElement = mElementContainerView.getSelectElement();
    if (selectElement == null) {
      Log.e(TAG, "upTapAction error select element is null!");
      return;
    }
    if (selectElement.isShowingViewResponseSelectedClick()) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        mUpDownMotionEvent[0] = copyMotionEvent(event);
      } else if (event.getAction() == MotionEvent.ACTION_UP) {
        mUpDownMotionEvent[1] = copyMotionEvent(event);
      }
    }
  }
  
  @Override
  public void downTapSelectElementAction(@NonNull MotionEvent event) {
    mElementContainerView.setMode(ElementContainerView.BaseActionMode.SELECTED_CLICK_OR_MOVE);
  }
  
  @Override
  public void upTapSelectElementAction(@NonNull MotionEvent event) {
    if (mElementContainerView.getMode() == ElementContainerView.BaseActionMode.SELECTED_CLICK_OR_MOVE) {
      WsElement selectElement = mElementContainerView.getSelectElement();
      if (selectElement == null) {
        Log.e(TAG, "upTapAction error select element is null!");
        return;
      }
      if (selectElement.isShowingViewResponseSelectedClick()) {
        mUpDownMotionEvent[0].setLocation(
            mUpDownMotionEvent[0].getX() - selectElement.getElementShowingView().getLeft(),
            mUpDownMotionEvent[0].getY() - selectElement.getElementShowingView().getTop());
        rotateMotionEvent(mUpDownMotionEvent[0], selectElement);
        
        mUpDownMotionEvent[1].setLocation(
            mUpDownMotionEvent[1].getX() - selectElement.getElementShowingView().getLeft(),
            mUpDownMotionEvent[1].getY() - selectElement.getElementShowingView().getTop());
        rotateMotionEvent(mUpDownMotionEvent[1], selectElement);
        selectElement.getElementShowingView().dispatchTouchEvent(mUpDownMotionEvent[0]);
        selectElement.getElementShowingView().dispatchTouchEvent(mUpDownMotionEvent[1]);
      } else {
        selectElement.selectedClick(event);
      }
      mElementContainerView.callListener(elementActionListener -> ((SelectedClickElementActionListener) elementActionListener).onSelectedClick(selectElement));
    }
  }
  
  /**
   * 将 @event 旋转 @element 中的角度
   *
   * @param event
   * @param element
   */
  private void rotateMotionEvent(MotionEvent event, WsElement element) {
    if (element.getRotate() != 0) {
      Matrix mInvertMatrix = new Matrix();
      mInvertMatrix.postRotate(
          -element.getRotate(),
          element.getElementShowingView().getWidth() / 2,
          element.getElementShowingView().getHeight() / 2);
      float[] point = new float[]{event.getX(), event.getY()};
      mInvertMatrix.mapPoints(point);
      event.setLocation(point[0], point[1]);
    }
  }
  
  public interface SelectedClickElementActionListener extends ElementContainerView.ElementActionListener {
    void onSelectedClick(WsElement wsElement);
  }
  
  @Override
  public int getPriority() {
    return 0;
  }
}
