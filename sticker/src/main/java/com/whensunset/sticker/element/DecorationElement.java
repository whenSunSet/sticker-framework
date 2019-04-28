package com.whensunset.sticker.element;

import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.widget.DecorationView;

/**
 * Created by whensunset on 2019/3/31.
 * element 的装饰框基类
 */

public abstract class DecorationElement extends AnimationElement {
  private static final String TAG = "heshixi:DElement";
  
  public static final int ELEMENT_SCALE_ROTATE_ICON_WIDTH = 84; // 旋转按钮的宽度
  
  public static final int ELEMENT_REMOVE_ICON_WIDTH = 84; // 删除按钮的宽度
  
  private static final int REDUNDANT_AREA_LEFT_RIGHT = 40; // 延伸区域的宽度
  
  private static final int REDUNDANT_AREA_TOP_BOTTOM = 40; // 延伸区域的高度
  
  private DecorationView mDecorationView;
  
  private ViewGroup.MarginLayoutParams mShowingViewParams = new ViewGroup.MarginLayoutParams(0, 0);
  
  protected boolean mIsSingleFingerScaleAndRotate; // 是否处于单指旋转缩放的状态
  
  public DecorationElement() {
    this(0, 0);
  }
  
  public DecorationElement(float originWidth, float originHeight) {
    super(originWidth, originHeight);
    mRedundantAreaTopBottom = REDUNDANT_AREA_TOP_BOTTOM;
    mRedundantAreaLeftRight = REDUNDANT_AREA_LEFT_RIGHT;
  }
  
  @Override
  public void add(ElementContainerView elementContainerView) {
    super.add(elementContainerView);
    AbsoluteLayout.LayoutParams showingViewAbsLayoutParams = (AbsoluteLayout.LayoutParams) mElementShowingView.getLayoutParams();
    mShowingViewParams = new ViewGroup.MarginLayoutParams(showingViewAbsLayoutParams.width, showingViewAbsLayoutParams.height);
    mShowingViewParams.leftMargin = REDUNDANT_AREA_LEFT_RIGHT + ELEMENT_REMOVE_ICON_WIDTH / 2;
    mShowingViewParams.topMargin = REDUNDANT_AREA_TOP_BOTTOM + ELEMENT_REMOVE_ICON_WIDTH / 2;
    mShowingViewParams.rightMargin = REDUNDANT_AREA_LEFT_RIGHT + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2;
    mShowingViewParams.bottomMargin = REDUNDANT_AREA_TOP_BOTTOM + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2;
    
    mDecorationView = initDecorationView();
    mElementContainerView.addView(mDecorationView);
  }
  
  /**
   * 初始化边框装饰 view，子类可以实现自己的样式
   */
  @NonNull
  protected DecorationView initDecorationView() {
    DecorationView decorationView = new DecorationView(mElementContainerView.getContext());
    decorationView.setDecorationElement(this);
    AbsoluteLayout.LayoutParams decorationViewLayoutParams =
        new AbsoluteLayout.LayoutParams(0, 0, 0, 0);
    decorationView.setLayoutParams(decorationViewLayoutParams);
    return decorationView;
  }
  
  @Override
  public void update() {
    super.update();
    AbsoluteLayout.LayoutParams decorationViewLayoutParams = (AbsoluteLayout.LayoutParams) mDecorationView.getLayoutParams();
    decorationViewLayoutParams.width = (int) ((mShowingViewParams.width * mScale) + mShowingViewParams.leftMargin + mShowingViewParams.rightMargin);
    decorationViewLayoutParams.height = (int) ((mShowingViewParams.height * mScale) + mShowingViewParams.topMargin + mShowingViewParams.bottomMargin);
    decorationViewLayoutParams.x = (int) getRealX(mMoveX, mDecorationView);
    decorationViewLayoutParams.y = (int) getRealY(mMoveY, mDecorationView);
    mDecorationView.setLayoutParams(decorationViewLayoutParams);
    mDecorationView.setRotation(mRotate);
    mDecorationView.setAlpha(mAlpha);
    mDecorationView.bringToFront();
  }
  
  @Override
  public void startElementAnimation(TransformParam to, Runnable endRun, long milTime) {
    startElementAnimation(to, endRun, milTime, true);
  }
  
  public void startElementAnimation(TransformParam to, Runnable endRun, long milTime, boolean isEndShowDecoration) {
    super.startElementAnimation(to, endRun, milTime);
    mDecorationView.setVisibility(View.GONE);
    startDecorationViewAnimation(to, milTime, isEndShowDecoration);
  }
  
  @Override
  public void restoreToBeforeAnimation(Runnable endRun, long milTime) {
    restoreToBeforeAnimation(endRun, milTime, true);
  }
  
  public void restoreToBeforeAnimation(Runnable endRun, long milTime, boolean isEndShowDecoration) {
    super.restoreToBeforeAnimation(endRun, milTime);
    mDecorationView.setVisibility(View.GONE);
    startDecorationViewAnimation(mBeforeTransformParam, milTime, isEndShowDecoration);
  }
  
  private void startDecorationViewAnimation(TransformParam to, long milTime, boolean isEndShowDecoration) {
    ElementContainerView.Consumer<ValueAnimator> updateWidthHeight = animator -> {
      ViewGroup.LayoutParams layoutParams = mDecorationView.getLayoutParams();
      layoutParams.width = (int) ((mShowingViewParams.width * (float) animator.getAnimatedValue()) + mShowingViewParams.leftMargin + mShowingViewParams.rightMargin);
      layoutParams.height = (int) ((mShowingViewParams.height * (float) animator.getAnimatedValue()) + mShowingViewParams.topMargin + mShowingViewParams.bottomMargin);
      mDecorationView.setLayoutParams(layoutParams);
    };
    startViewAnimationByChangeViewParam(to, () -> {
      if (isEndShowDecoration) {
        mDecorationView.setVisibility(View.VISIBLE);
      } else {
        mDecorationView.setVisibility(View.GONE);
      }
    }, milTime, mDecorationView, updateWidthHeight);
  }
  
  @Override
  public void select() {
    super.select();
    mDecorationView.setVisibility(View.VISIBLE);
  }
  
  @Override
  public void unSelect() {
    super.unSelect();
    mDecorationView.setVisibility(View.GONE);
  }
  
  @Override
  public void remove() {
    mElementContainerView.removeView(mDecorationView);
    super.remove();
  }
  
  @Override
  public void onSingleFingerMoveStart() {
    super.onSingleFingerMoveStart();
    mDecorationView.setVisibility(View.GONE);
  }
  
  @Override
  public void onSingleFingerMoveEnd() {
    super.onSingleFingerMoveEnd();
    mDecorationView.setVisibility(View.VISIBLE);
  }
  
  @Override
  public void onDoubleFingerScaleAndRotateStart(float deltaRotate, float deltaScale) {
    super.onDoubleFingerScaleAndRotateStart(deltaRotate, deltaScale);
    mDecorationView.setVisibility(View.GONE);
  }
  
  @Override
  public void onDoubleFingerScaleAndRotateEnd() {
    super.onDoubleFingerScaleAndRotateEnd();
    mDecorationView.setVisibility(View.VISIBLE);
  }
  
  /**
   * 当前 Element 开始单指旋转缩放
   */
  public void onSingleFingerScaleAndRotateStart() {
    mDecorationView.setVisibility(View.GONE);
    mIsSingleFingerScaleAndRotate = true;
  }
  
  /**
   * 当前 Element 单指旋转缩放中
   */
  public void onSingleFingerScaleAndRotateProcess(float motionEventX, float motionEventY) {
    scaleAndRotateForSingleFinger(motionEventX, motionEventY);
  }
  
  /**
   * 当前 Element 单指旋转缩放结束
   */
  public void onSingleFingerScaleAndRotateEnd() {
    mDecorationView.setVisibility(View.VISIBLE);
    mIsSingleFingerScaleAndRotate = true;
  }
  
  /**
   * 判断坐标是否处于 旋转缩放按钮 区域中
   *
   * @param motionEventX
   * @param motionEventY
   * @return
   */
  public boolean isInScaleAndRotateButton(float motionEventX, float motionEventY) {
    return isPointInTheRect(motionEventX, motionEventY,
        getScaleAndRotateButtonRect());
  }
  
  /**
   * 判断坐标是否处于 删除按钮 区域中
   *
   * @param motionEventX
   * @param motionEventY
   * @return
   */
  public boolean isInRemoveButton(float motionEventX, float motionEventY) {
    return isPointInTheRect(motionEventX, motionEventY, getRemoveButtonRect());
  }
  
  /**
   * 计算单指缩放的旋转角度
   *
   * @param motionEventX
   * @param motionEventY
   */
  private void scaleAndRotateForSingleFinger(float motionEventX, float motionEventY) {
    Rect originWholeRect = getOriginRedundantRect();
    float halfWidth = originWholeRect.width() / 2.0f;
    float halfHeight = originWholeRect.height() / 2.0f;
    float newRadius =
        PointF.length(motionEventX - originWholeRect.centerX(), motionEventY - originWholeRect.centerY());
    float oldRadius = PointF.length(halfWidth, halfHeight);
    
    mScale = newRadius / oldRadius;
    mScale = (mScale < MIN_SCALE_FACTOR ? MIN_SCALE_FACTOR : mScale);
    mScale = (mScale > MAX_SCALE_FACTOR ? MAX_SCALE_FACTOR : mScale);
    
    mRotate = (float) Math
        .toDegrees(Math.atan2(halfWidth, halfHeight)
            - Math.atan2(motionEventX - originWholeRect.centerX(), motionEventY - originWholeRect.centerY()));
    mRotate = getCanonicalRotation(mRotate);
    Log.d(TAG,
        "scaleAndRotateForSingleFinger mScale:" + mScale + ",mRotate:" + mRotate + ",x:"
            + motionEventX + ",y:"
            + motionEventY + ",rect:" + originWholeRect + ",newRadius:" + newRadius + "oldRadius:"
            + oldRadius);
  }
  
  /**
   * 包括旋转、删除按钮的最小矩形区域
   *
   * @return
   */
  @Override
  protected Rect getWholeRect() {
    Rect redundantAreaRect = getRedundantAreaRect();
    return new Rect(
        redundantAreaRect.left - ELEMENT_REMOVE_ICON_WIDTH / 2,
        redundantAreaRect.top - ELEMENT_REMOVE_ICON_WIDTH / 2,
        redundantAreaRect.right + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
        redundantAreaRect.bottom + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2);
  }
  
  /**
   * 获取 元素 原始(没有 scale 过)的区域，包括延伸区域
   *
   * @return
   */
  protected Rect getOriginRedundantRect() {
    float viewCenterX = mEditRect.centerX();
    float viewCenterY = mEditRect.centerY();
    float contentWidth = mOriginWidth;
    float contentHeight = mOriginHeight;
    
    Rect originContentRect = new Rect(
        (int) (viewCenterX + mMoveX
            - (contentWidth / 2)),
        (int) (viewCenterY + mMoveY
            - (contentHeight / 2)),
        (int) (viewCenterX + mMoveX
            + (contentWidth / 2)),
        (int) (viewCenterY + mMoveY
            + (contentHeight / 2)));
    
    return new Rect(
        originContentRect.left - mRedundantAreaLeftRight,
        originContentRect.top - mRedundantAreaLeftRight,
        originContentRect.right + mRedundantAreaTopBottom,
        originContentRect.bottom + mRedundantAreaTopBottom);
  }
  
  /**
   * 获取 元素 包括延伸区域的区域
   *
   * @return
   */
  protected Rect getRedundantAreaRect() {
    Rect dstDrawRect = getContentRect();
    return new Rect(
        dstDrawRect.left - mRedundantAreaLeftRight,
        dstDrawRect.top - mRedundantAreaLeftRight,
        dstDrawRect.right + mRedundantAreaTopBottom,
        dstDrawRect.bottom + mRedundantAreaTopBottom);
  }
  
  /**
   * 获取 元素 删除按钮在 @EditRect 坐标下的 Rect
   *
   * @return
   */
  protected Rect getRemoveButtonRect() {
    Rect redundantAreaRect = getRedundantAreaRect();
    return new Rect(
        redundantAreaRect.left - ELEMENT_REMOVE_ICON_WIDTH / 2,
        redundantAreaRect.top - ELEMENT_REMOVE_ICON_WIDTH / 2,
        redundantAreaRect.left + ELEMENT_REMOVE_ICON_WIDTH / 2,
        redundantAreaRect.top + ELEMENT_REMOVE_ICON_WIDTH / 2);
  }
  
  /**
   * 获取 元素 旋转缩放按钮在 @EditRect 坐标下的 Rect
   *
   * @return
   */
  protected Rect getScaleAndRotateButtonRect() {
    Rect redundantAreaRect = getRedundantAreaRect();
    return new Rect(
        redundantAreaRect.right - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
        redundantAreaRect.bottom - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
        redundantAreaRect.right + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
        redundantAreaRect.bottom + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2);
  }
  
  public boolean isSingleFingerScaleAndRotate() {
    return mIsSingleFingerScaleAndRotate;
  }
}
