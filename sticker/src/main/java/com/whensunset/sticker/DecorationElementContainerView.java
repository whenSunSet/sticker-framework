package com.whensunset.sticker;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by whensunset on 2019/3/31.
 */

public class DecorationElementContainerView extends ElementContainerView {
  private static final String DEBUG_TAG = "heshixi:DECV";
  
  protected DecorationActionMode mDecorationActionMode;
  
  public DecorationElementContainerView(Context context) {
    super(context);
  }
  
  public DecorationElementContainerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  public DecorationElementContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
  
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public DecorationElementContainerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }
  
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
  }
  
  /**
   * 取消选中、删除
   */
  public void unSelectDeleteAndUpdateTopElement() {
    unSelectElement();
    deleteElement();
  }
  
  /**
   * 添加、选中、更新
   *
   * @param wsElement
   */
  public void addSelectAndUpdateElement(WsElement wsElement) {
    addElement(wsElement);
    selectElement(wsElement);
    update();
  }
  
  @Override
  protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    DecorationElement element = (DecorationElement) mSelectedElement;
    if (element == null) {
      return false;
    }
    element = (DecorationElement) findElementByPosition(e2.getX(), e2.getY());
    if (element == null) {
      return false;
    }
    
    AnimationElement.TransformParam to = new AnimationElement.TransformParam(element);
    to.mMoveY += (velocityY * 0.2 * 0.3);
    to.mMoveX += (velocityX * 0.2 * 0.3);
    element.startElementAnimation(to);
    return true;
  }
  
  @Override
  protected boolean downSelectTapOtherAction(@NonNull MotionEvent event) {
    mDecorationActionMode = DecorationActionMode.NONE;
    final float x = event.getX(), y = event.getY();
    DecorationElement selectedDecorationElement = (DecorationElement) mSelectedElement;
    if (selectedDecorationElement.isInScaleAndRotateButton(x, y)) {
      // 开始进行单指旋转缩放
      mDecorationActionMode = DecorationActionMode.SINGER_FINGER_SCALE_AND_ROTATE;
      selectedDecorationElement.onSingleFingerScaleAndRotateStart();
      callListener(elementActionListener -> {
        ((TikTokElementActionListener) elementActionListener).onSingleFingerScaleAndRotateStart(selectedDecorationElement);
      });
      Log.d(DEBUG_TAG, "downSelectTapOtherAction selected scale and rotate");
      return true;
    }
    if (selectedDecorationElement.isInRemoveButton(x, y)) {
      mDecorationActionMode = DecorationActionMode.CLICK_BUTTON_DELETE;
      Log.d(DEBUG_TAG, "downSelectTapOtherAction selected delete");
      return true;
    }
    return false;
  }
  
  @Override
  protected boolean scrollSelectTapOtherAction(@NonNull MotionEvent event, float distanceX, float distanceY) {
    if (mSelectedElement == null) {
      Log.d(DEBUG_TAG, "detectorSingleFingerRotateAndScale scale and rotate but not select");
      return false;
    }
    
    if (mDecorationActionMode == DecorationActionMode.SINGER_FINGER_SCALE_AND_ROTATE) {
      DecorationElement selectedDecorationElement = (DecorationElement) mSelectedElement;
      selectedDecorationElement.onSingleFingerScaleAndRotateProcess(event.getX(), event.getY());
      update();
      // 在单指旋转缩放过程中
      callListener(elementActionListener -> {
        ((TikTokElementActionListener) elementActionListener).onSingleFingerScaleAndRotateProcess(selectedDecorationElement);
      });
      Log.d(DEBUG_TAG,
          "scrollSelectTapOtherAction scale and rotate |||||||||| distanceX:" + distanceX
              + "distanceY:" + distanceY + "x:" + event.getX() + "y:" + event.getY());
      return true;
    }
    
    return false;
  }
  
  @Override
  protected boolean upSelectTapOtherAction(@NonNull MotionEvent event) {
    if (mSelectedElement == null) {
      Log.w(DEBUG_TAG, "upSelectTapOtherAction delete but not select ");
      return false;
    }
    
    DecorationElement selectedDecorationElement = (DecorationElement) mSelectedElement;
    if (mDecorationActionMode == DecorationActionMode.CLICK_BUTTON_DELETE) {
      unSelectDeleteAndUpdateTopElement();
      mDecorationActionMode = DecorationActionMode.NONE;
      Log.d(DEBUG_TAG, "upSelectTapOtherAction delete");
      return true;
    }
    
    if (mDecorationActionMode == DecorationActionMode.SINGER_FINGER_SCALE_AND_ROTATE) {
      selectedDecorationElement.onSingleFingerScaleAndRotateEnd();
      callListener(elementActionListener -> {
        ((TikTokElementActionListener) elementActionListener).onSingleFingerScaleAndRotateEnd(selectedDecorationElement);
      });
      mDecorationActionMode = DecorationActionMode.NONE;
      Log.d(DEBUG_TAG, "upSelectTapOtherAction scale and rotate end");
      return true;
    }
    return false;
  }
  
  public interface TikTokElementActionListener extends ElementActionListener {
    /**
     * 选中了元素之后，对元素单指缩放旋转开始的回调
     *
     * @param element
     */
    void onSingleFingerScaleAndRotateStart(DecorationElement element);
    
    /**
     * 选中了元素之后，对元素单指缩放旋转过程的回调
     *
     * @param element
     */
    void onSingleFingerScaleAndRotateProcess(DecorationElement element);
    
    /**
     * 一次单指 缩放旋转 结束
     *
     * @param element
     */
    void onSingleFingerScaleAndRotateEnd(DecorationElement element);
  }
  
  public static class DefaultTikTokElementActionListener extends DefaultElementActionListener implements TikTokElementActionListener {
    
    @Override
    public void onSingleFingerScaleAndRotateStart(DecorationElement element) {
      Log.d(DEBUG_TAG, "onSingleFingerScaleAndRotateStart");
    }
    
    @Override
    public void onSingleFingerScaleAndRotateProcess(DecorationElement element) {
      Log.d(DEBUG_TAG, "onSingleFingerScaleAndRotateProcess");
    }
    
    @Override
    public void onSingleFingerScaleAndRotateEnd(DecorationElement element) {
      Log.d(DEBUG_TAG, "onSingleFingerScaleAndRotateEnd");
    }
  }
  
  public enum DecorationActionMode {
    NONE,
    SINGER_FINGER_SCALE_AND_ROTATE,
    CLICK_BUTTON_DELETE,
  }
}
