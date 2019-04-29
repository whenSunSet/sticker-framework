package com.whensunset.sticker.container.worker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.whensunset.sticker.R;
import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.element.AnimationElement;
import com.whensunset.sticker.element.DecorationElement;
import com.whensunset.sticker.element.WsElement;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.security.AccessController.getContext;

/**
 * Created by whensunset on 2019/4/29.
 * 垃圾桶的 worker
 */

public class TrashWorker extends DefaultContainerWorker {
  private static final String TAG = "heshixi:TrashWorker";
  private static final float TRASH_RECT_WIDTH_PERCENT = 0.13f;// 垃圾桶区域长宽占 view 的宽的百分比
  private static final float TRASH_RECT_MARGIN_TOP = 0.03f;// 垃圾桶区域距离 view 顶部距离占 view 长的百分比
  private static final long TRASH_VIEW_ANIMATION_DURATION = 100;// 垃圾桶 view 显示和消失的动画的时间
  private static final long ELEMENT_ANIMATION_DURATION = 300;// 元素做动画的时间
  protected static final long VIBRATOR_DURATION_IN_TRASH = 40; // 进入垃圾桶时的震动的时长
  
  private RectF mTrashRect = new RectF();
  private View mTrashView;
  private AnimatorSet mTrashViewAnimator;
  private boolean mIsInTrashRect = false;
  
  public TrashWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  @Override
  public void viewLayoutComplete() {
    super.viewLayoutComplete();
    float containerViewWidth = mElementContainerView.getWidth();
    float containerViewHeight = mElementContainerView.getHeight();
    float trashWidth = TRASH_RECT_WIDTH_PERCENT * containerViewWidth;
    mTrashRect.set(
        (containerViewWidth - trashWidth) / 2,
        TRASH_RECT_MARGIN_TOP * containerViewHeight,
        (containerViewWidth + trashWidth) / 2,
        TRASH_RECT_MARGIN_TOP * containerViewHeight + trashWidth);
    mNoRuleRectList.add(new RectF(
        mTrashRect.left - mTrashRect.width() / 2,
        mTrashRect.top - mTrashRect.height() / 2,
        mTrashRect.right + mTrashRect.width() / 2,
        mTrashRect.bottom + mTrashRect.height() / 2));
    mTrashView = initTrashView();
    mElementContainerView.addView(mTrashView);
    mTrashView.setVisibility(GONE);
  }
  
  /**
   * 初始化垃圾桶 view，子类可以实现自己的样式
   *
   * @return
   */
  @NonNull
  protected View initTrashView() {
    ImageView trashView = new ImageView(mElementContainerView.getContext());
    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams((int) mTrashRect.width(), (int) mTrashRect.height(), (int) mTrashRect.left, (int) mTrashRect.top);
    trashView.setLayoutParams(layoutParams);
    trashView.setImageResource(R.drawable.default_decoration_trash);
    int padding = (int) (mTrashRect.width() * 0.25);
    trashView.setPadding(padding, padding, padding, padding);
    return trashView;
  }
  
  @Override
  public void scrollTapSelectElementPreAction(@NonNull MotionEvent event, float[] distanceXY) {
    WsElement selectElement = mElementContainerView.getSelectElement();
    if (selectElement == null) {
      Log.w(TAG, "scrollTapSelectElementPreAction selectElement is null");
      return;
    }
    if (!selectElement.isSingerFingerMove()) {
      Log.w(TAG, "scrollTapSelectElementPreAction selectElement is not move");
      return;
    }
    
    boolean isInTrashRect = mTrashRect.contains(event.getX(), event.getY());
    if (isInTrashRect && !mIsInTrashRect) {
      elementEnterTrash(event);
    }
    if (!isInTrashRect && mIsInTrashRect) {
      elementLeaveTrash(event);
    }
    mIsInTrashRect = isInTrashRect;
  }
  
  @Override
  public void upTapSelectElementPreAction(@NonNull MotionEvent event) {
    WsElement selectedElement = mElementContainerView.getSelectElement();
    if (mIsInTrashRect) {
      DecorationElement decorationElement = (DecorationElement) selectedElement;
      AnimationElement.TransformParam to = new AnimationElement.TransformParam(decorationElement);
      to.mScale = 0f;
      to.mMoveX = 0;
      to.mMoveY = -1 * (mElementContainerView.getHeight() / 2 - mTrashRect.top - mTrashRect.height() / 2);
      to.mIsNeedLimitScale = false;
      to.mIsNeedLimitXY = false;
      decorationElement.startElementAnimation(to, () -> deleteElement(selectedElement), ELEMENT_ANIMATION_DURATION, false);
      mIsInTrashRect = false;
      trashViewHide();
    }
  }
  
  @Override
  public void doubleFingerScaleAndRotateStart(float deltaRotate, float deltaScale) {
    mIsInTrashRect = false;
  }
  
  @Override
  public void singleFingerMoveStart(float distanceX, float distanceY) {
    trashViewShow();
  }

  @Override
  public void singleFingerMoveEnd(MotionEvent event) {
    trashViewHide();
  }
  
  /**
   * 元素进入垃圾桶
   */
  protected void elementEnterTrash(@NonNull MotionEvent event) {
    DecorationElement decorationElement = (DecorationElement) mElementContainerView.getSelectElement();
    AnimationElement.TransformParam to = new AnimationElement.TransformParam(decorationElement);
    to.mAlpha = 0.3f;
    to.mEnableScale = false;
    to.mEnableRotate = false;
    to.mEnableMoveX = false;
    to.mEnableMoveY = false;
    decorationElement.startElementAnimation(to, null, ELEMENT_ANIMATION_DURATION, false);
    mElementContainerView.getVibrator().vibrate(VIBRATOR_DURATION_IN_TRASH);
    mElementContainerView.callListener(elementActionListener ->
        ((TrashElementActionListener) elementActionListener).onEnterInTrashRect());
  }
  
  /**
   * 元素离开垃圾桶
   */
  protected void elementLeaveTrash(@NonNull MotionEvent event) {
    DecorationElement decorationElement = (DecorationElement) mElementContainerView.getSelectElement();
    decorationElement.restoreToBeforeAnimation(null, ELEMENT_ANIMATION_DURATION, false);
    mElementContainerView.getVibrator().vibrate(VIBRATOR_DURATION_IN_TRASH);
    mElementContainerView.callListener(elementActionListener ->
        ((TrashElementActionListener) elementActionListener).onLeaveTrashRect());
  }
  
  protected void trashViewShow() {
    mTrashView.setVisibility(VISIBLE);
    mTrashView.bringToFront();
    trashViewAnimation(null, true);
  }
  
  protected void trashViewHide() {
    trashViewAnimation(() -> mTrashView.setVisibility(GONE), false);
  }
  
  protected void trashViewAnimation(Runnable endRun, boolean isShow) {
    if (mTrashViewAnimator != null) {
      mTrashViewAnimator.cancel();
    }
    AnimatorSet trashViewAnimator = new AnimatorSet();
    mTrashViewAnimator = trashViewAnimator;
    ObjectAnimator scaleXAnimator = ObjectAnimator
        .ofFloat(mTrashView, "scaleX", isShow ? 0 : 1, isShow ? 1 : 0);
    ObjectAnimator scaleYAnimator = ObjectAnimator
        .ofFloat(mTrashView, "scaleY", isShow ? 0 : 1, isShow ? 1 : 0);
    trashViewAnimator.playTogether(scaleXAnimator, scaleYAnimator);
    trashViewAnimator.setDuration(TRASH_VIEW_ANIMATION_DURATION);
    trashViewAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationCancel(Animator animation) {
        super.onAnimationCancel(animation);
        if (endRun != null) {
          endRun.run();
        }
        mTrashViewAnimator = null;
      }
      
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        if (endRun != null) {
          endRun.run();
        }
        mTrashViewAnimator = null;
      }
    });
    trashViewAnimator.setInterpolator(new AnimationElement.CubicEaseOutInterpolator());
    trashViewAnimator.start();
  }
  
  public interface TrashElementActionListener extends ElementContainerView.ElementActionListener {
    void onEnterInTrashRect();
    
    void onLeaveTrashRect();
  }
  
  @Override
  public int getPriority() {
    return 0;
  }
}
