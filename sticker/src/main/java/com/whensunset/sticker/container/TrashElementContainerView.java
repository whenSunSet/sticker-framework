package com.whensunset.sticker.container;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.whensunset.sticker.R;
import com.whensunset.sticker.element.AnimationElement;
import com.whensunset.sticker.element.DecorationElement;

/**
 * Created by whensunset on 2019/4/14.
 */

public class TrashElementContainerView extends RuleLineElementContainerView {
  private static final String TAG = "heshixi:MTTECV";
  private static final float TRASH_RECT_WIDTH_PERCENT = 0.13f;// 垃圾桶区域长宽占 view 的宽的百分比
  private static final float TRASH_RECT_MARGIN_TOP = 0.03f;// 垃圾桶区域距离 view 顶部距离占 view 长的百分比
  private static final long TRASH_VIEW_ANIMATION_DURATION = 100;// 垃圾桶 view 显示和消失的动画的时间
  private static final long ELEMENT_ANIMATION_DURATION = 300;// 元素做动画的时间
  protected static final long VIBRATOR_DURATION_IN_TRASH = 40; // 进入垃圾桶时的震动的时长
  
  public TrashElementContainerView(Context context) {
    super(context);
  }
  
  public TrashElementContainerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  public TrashElementContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
  
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public TrashElementContainerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }
  
  private RectF mTrashRect = new RectF();
  private View mTrashView;
  private AnimatorSet mTrashViewAnimator;
  private boolean mIsInTrashRect = false;
  
  @Override
  protected void viewLayoutComplete() {
    super.viewLayoutComplete();
    float trashWidth = TRASH_RECT_WIDTH_PERCENT * getWidth();
    mTrashRect.set(
        (getWidth() - trashWidth) / 2,
        TRASH_RECT_MARGIN_TOP * getHeight(),
        (getWidth() + trashWidth) / 2,
        TRASH_RECT_MARGIN_TOP * getHeight() + trashWidth);
    mNoRuleRectList.add(new RectF(
        mTrashRect.left - mTrashRect.width() / 2,
        mTrashRect.top - mTrashRect.height() / 2,
        mTrashRect.right + mTrashRect.width() / 2,
        mTrashRect.bottom + mTrashRect.height() / 2));
    mTrashView = initTrashView();
    addView(mTrashView);
    mTrashView.setVisibility(GONE);
  }
  
  /**
   * 初始化垃圾桶 view，子类可以实现自己的样式
   *
   * @return
   */
  @NonNull
  protected View initTrashView() {
    ImageView trashView = new ImageView(getContext());
    LayoutParams layoutParams = new LayoutParams((int) mTrashRect.width(), (int) mTrashRect.height(), (int) mTrashRect.left, (int) mTrashRect.top);
    trashView.setLayoutParams(layoutParams);
    trashView.setImageResource(R.drawable.default_decoration_trash);
    int padding = (int) (mTrashRect.width() * 0.25);
    trashView.setPadding(padding, padding, padding, padding);
    return trashView;
  }
  
  @Override
  protected boolean scrollTapSelectElementPreAction(@NonNull MotionEvent event, float[] distanceXY) {
    if (mSelectedElement == null) {
      Log.w(TAG, "scrollTapSelectElementPreAction mSelectedElement is null");
      return super.scrollTapSelectElementPreAction(event, distanceXY);
    }
    if (!mSelectedElement.isSingerFingerMove()) {
      Log.w(TAG, "scrollTapSelectElementPreAction mSelectedElement is not move");
      return super.scrollTapSelectElementPreAction(event, distanceXY);
    }
    
    boolean isInTrashRect = mTrashRect.contains(event.getX(), event.getY());
    if (isInTrashRect && !mIsInTrashRect) {
      elementEnterTrash(event);
    }
    if (!isInTrashRect && mIsInTrashRect) {
      elementLeaveTrash(event);
    }
    mIsInTrashRect = isInTrashRect;
    return super.scrollTapSelectElementPreAction(event, distanceXY);
  }
  
  @Override
  protected boolean upTapSelectElementPreAction(@NonNull MotionEvent event) {
    if (mIsInTrashRect) {
      DecorationElement decorationElement = (DecorationElement) mSelectedElement;
      AnimationElement.TransformParam to = new AnimationElement.TransformParam(decorationElement);
      to.mScale = 0f;
      to.mMoveX = 0;
      to.mMoveY = -1 * (getHeight() / 2 - mTrashRect.top - mTrashRect.height() / 2);
      to.mIsNeedLimitScale = false;
      to.mIsNeedLimitXY = false;
      decorationElement.startElementAnimation(to, () -> deleteElement(mSelectedElement), ELEMENT_ANIMATION_DURATION, false);
      mIsInTrashRect = false;
      trashViewHide();
      return true;
    }
    return super.upTapSelectElementPreAction(event);
  }
  
  @Override
  protected void doubleFingerScaleAndRotateStart(float deltaRotate, float deltaScale) {
    mIsInTrashRect = false;
    super.doubleFingerScaleAndRotateStart(deltaRotate, deltaScale);
  }
  
  @Override
  protected void singleFingerMoveStart(float distanceX, float distanceY) {
    super.singleFingerMoveStart(distanceX, distanceY);
    trashViewShow();
  }
  
  @Override
  protected void singleFingerMoveEnd(MotionEvent event) {
    super.singleFingerMoveEnd(event);
    trashViewHide();
  }
  
  /**
   * 元素进入垃圾桶
   */
  protected void elementEnterTrash(@NonNull MotionEvent event) {
    DecorationElement decorationElement = (DecorationElement) mSelectedElement;
    AnimationElement.TransformParam to = new AnimationElement.TransformParam(decorationElement);
    to.mAlpha = 0.3f;
    to.mEnableScale = false;
    to.mEnableRotate = false;
    to.mEnableMoveX = false;
    to.mEnableMoveY = false;
    decorationElement.startElementAnimation(to, null, ELEMENT_ANIMATION_DURATION, false);
    mVibrator.vibrate(VIBRATOR_DURATION_IN_TRASH);
    callListener(elementActionListener ->
        ((TrashElementActionListener) elementActionListener).onEnterInTrashRect());
  }
  
  /**
   * 元素离开垃圾桶
   */
  protected void elementLeaveTrash(@NonNull MotionEvent event) {
    DecorationElement decorationElement = (DecorationElement) mSelectedElement;
    decorationElement.restoreToBeforeAnimation(null, ELEMENT_ANIMATION_DURATION, false);
    mVibrator.vibrate(VIBRATOR_DURATION_IN_TRASH);
    callListener(elementActionListener ->
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
  
  public interface TrashElementActionListener extends RuleLineElementActionListener {
    void onEnterInTrashRect();
    
    void onLeaveTrashRect();
  }
  
  public class DefaultTrashElementActionListener extends DefaultRuleLineElementActionListener implements TrashElementActionListener {
    @Override
    public void onEnterInTrashRect() {
      Log.i(TAG, "onEnterInTrashRect");
      
    }
    
    @Override
    public void onLeaveTrashRect() {
      Log.i(TAG, "onLeaveTrashRect");
    }
  }
}
