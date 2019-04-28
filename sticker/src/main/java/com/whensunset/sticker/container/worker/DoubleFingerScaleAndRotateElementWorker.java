package com.whensunset.sticker.container.worker;

import com.whensunset.sticker.container.ElementContainerView;

/**
 * Created by whensunset on 2019/4/27.
 */

public class DoubleFingerScaleAndRotateElementWorker extends DefaultContainerWorker {
  public DoubleFingerScaleAndRotateElementWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public int getPriority() {
    return Integer.MAX_VALUE - 2;
  }
}
