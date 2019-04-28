package com.whensunset.sticker.container.worker;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.element.WsElement;

/**
 * Created by whensunset on 2019/4/28.
 * 点击空白区域取消当前 Element 选中的 Worker
 */

public class TapBlackUnSelectElementWorker extends DefaultContainerWorker {
  private static final String TAG = "TBUSEWorker";
  
  public TapBlackUnSelectElementWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public void downTapBlank(@NonNull MotionEvent event) {
  }
  
  @Override
  public void upTapBlank(@NonNull MotionEvent event) {
    WsElement selectElement = mElementContainerView.getSelectElement();
    if (selectElement == null) {
      Log.e(TAG, "upTapBlank error select element is null!");
      return;
    }
    mElementContainerView.unSelectElement();
    mElementContainerView.callListener(elementActionListener ->
        ((TapBlackUnSelectElementActionListener) elementActionListener).onClickBlankScreen(selectElement));
  }
  
  public interface TapBlackUnSelectElementActionListener extends ElementContainerView.ElementActionListener {
    void onClickBlankScreen(WsElement wsElement);
  }
  
  @Override
  public int getPriority() {
    return 0;
  }
}
