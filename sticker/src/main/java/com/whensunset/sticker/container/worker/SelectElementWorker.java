package com.whensunset.sticker.container.worker;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.element.WsElement;

/**
 * Created by whensunset on 2019/4/27.
 * down 事件选中 Element 的 worker
 */

public class SelectElementWorker extends DefaultContainerWorker {
  
  public SelectElementWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public void downTapUnSelectElementAction(@NonNull MotionEvent event, WsElement clickedElement) {
    mElementContainerView.setMode(ElementContainerView.BaseActionMode.SELECT);
    mElementContainerView.unSelectElement();
    mElementContainerView.selectElement(clickedElement);
    mElementContainerView.update();
  }
  
  @Override
  public int getPriority() {
    return Integer.MAX_VALUE;
  }
}
