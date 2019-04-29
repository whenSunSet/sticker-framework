package com.whensunset.sticker.container.worker;

import android.view.MotionEvent;

import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.element.AnimationElement;
import com.whensunset.sticker.element.DecorationElement;

/**
 * Created by whensunset on 2019/4/29.
 * 可以对 element 进行一个抛的动作的 worker
 */

public class FlingSlidingElementWorker extends DefaultContainerWorker {
  public FlingSlidingElementWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    if (!(mElementContainerView.getSelectElement() instanceof DecorationElement)) {
      return;
    }
    
    DecorationElement element = (DecorationElement) mElementContainerView.getSelectElement();
    if (element == null) {
      return;
    }
    
    element = (DecorationElement) mElementContainerView.findElementByPosition(e2.getX(), e2.getY());
    if (element == null) {
      return;
    }
    
    AnimationElement.TransformParam to = new AnimationElement.TransformParam(element);
    to.mMoveY += (velocityY * 0.2 * 0.3);
    to.mMoveX += (velocityX * 0.2 * 0.3);
    element.startElementAnimation(to);
  }
  
  @Override
  public int getPriority() {
    return 0;
  }
}
