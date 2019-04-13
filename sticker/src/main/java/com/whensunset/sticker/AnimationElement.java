package com.whensunset.sticker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

/**
 * Created by whensunset on 2019/4/6.
 * 对 element 的动画基类
 */

public abstract class AnimationElement extends WsElement {
  private static final String DEBUG_TAG = "heshixi:AElement";
  private static final long DEFAULT_ANIMATION_DURATION = 300; // 默认动画时间为 300 毫秒
  protected TransformParam mBeforeTransformParam = new AnimationElement.TransformParam();
  
  public AnimationElement(int elementType, float originWidth, float originHeight) {
    super(elementType, originWidth, originHeight);
  }
  
  public AnimationElement(int elementType) {
    super(elementType);
  }
  
  public void startElementAnimation(TransformParam to) {
    startElementAnimation(to, null, DEFAULT_ANIMATION_DURATION);
  }
  
  public void startElementAnimation(TransformParam to, Runnable endRun) {
    startElementAnimation(to, endRun, DEFAULT_ANIMATION_DURATION);
  }
  
  /**
   * 开始对 element 做动画，主要是对 showing view 做动画，如果子类有需要跟随 showing view 一起动的 view
   * 可以在子类中实现跟随动画，例如 DecorationElement 中需要对 DecorationView 做跟随动画
   *
   * @param to      element 进行动画的参数
   * @param endRun  动画完成时的操作
   * @param milTime 动画进行的时间
   */
  public void startElementAnimation(TransformParam to, Runnable endRun, long milTime) {
    mBeforeTransformParam.mRotate = mRotate;
    mBeforeTransformParam.mScale = mScale;
    mBeforeTransformParam.mAlpha = mAlpha;
    mBeforeTransformParam.mMoveX = mMoveX;
    mBeforeTransformParam.mMoveY = mMoveY;
    if (isRealChangeShowingView()) {
      ElementContainerView.Consumer<ValueAnimator> updateWidthHeight = animator -> {
        ViewGroup.LayoutParams layoutParams = mElementShowingView.getLayoutParams();
        layoutParams.width = (int) (mOriginWidth * (float) animator.getAnimatedValue());
        layoutParams.height = (int) (mOriginHeight * (float) animator.getAnimatedValue());
        mElementShowingView.setLayoutParams(layoutParams);
      };
      startViewAnimationByChangeViewParam(to, endRun, milTime, mElementShowingView, updateWidthHeight);
    } else {
      startViewAnimationByAnimationParam(to, endRun, milTime, mElementShowingView);
    }
  }
  
  public void restoreToBeforeAnimation() {
    restoreToBeforeAnimation(null, DEFAULT_ANIMATION_DURATION);
  }
  
  public void restoreToBeforeAnimation(Runnable endRun) {
    restoreToBeforeAnimation(endRun, DEFAULT_ANIMATION_DURATION);
  }
  
  public void restoreToBeforeAnimation(Runnable endRun, long milTime) {
    if (isRealChangeShowingView()) {
      ElementContainerView.Consumer<ValueAnimator> updateWidthHeight = animator -> {
        ViewGroup.LayoutParams layoutParams = mElementShowingView.getLayoutParams();
        layoutParams.width = (int) (mOriginWidth * (float) animator.getAnimatedValue());
        layoutParams.height = (int) (mOriginHeight * (float) animator.getAnimatedValue());
        mElementShowingView.setLayoutParams(layoutParams);
      };
      startViewAnimationByChangeViewParam(mBeforeTransformParam, endRun, milTime, mElementShowingView, updateWidthHeight);
    } else {
      startViewAnimationByAnimationParam(mBeforeTransformParam, endRun, milTime, mElementShowingView);
    }
  }
  
  /**
   * 开始对传入的 view 做动画，这里做动画的参数为 scale、translation 等等
   *
   * @param to            element 进行动画的参数
   * @param endRun        动画完成时的操作
   * @param milTime       动画进行的时间
   * @param animationView 需要进行动画的 view
   */
  protected void startViewAnimationByAnimationParam(
      TransformParam to,
      Runnable endRun,
      long milTime,
      View animationView) {
    if (to == null) {
      Log.e(DEBUG_TAG, "startElementAnimation error to is null");
      return;
    }
    limitTransformParam(to);
    
    AnimatorSet elementAnimator = new AnimatorSet();
    ObjectAnimator rotationAnimator = ObjectAnimator
        .ofFloat(animationView, "rotation", mRotate, to.mRotate);
    ObjectAnimator scaleXAnimator = ObjectAnimator
        .ofFloat(animationView, "scaleX", mScale, to.mScale);
    ObjectAnimator scaleYAnimator = ObjectAnimator
        .ofFloat(animationView, "scaleY", mScale, to.mScale);
    ObjectAnimator translateXAnimator =
        ObjectAnimator.ofFloat(animationView, "translationX", getRealX(mMoveX, animationView), getRealX(to.mMoveX, animationView));
    ObjectAnimator translateYAnimator = ObjectAnimator
        .ofFloat(animationView, "translationY", getRealY(mMoveY, animationView), getRealY(to.mMoveY, animationView));
    ObjectAnimator alphaYAnimator = ObjectAnimator
        .ofFloat(animationView, "alpha", mAlpha, to.mAlpha);
    elementAnimator.playTogether(rotationAnimator, scaleXAnimator, scaleYAnimator,
        translateXAnimator, translateYAnimator, alphaYAnimator);
    elementAnimator.setDuration(milTime);
    elementAnimator.setInterpolator(new CubicEaseOutInterpolator());
    elementAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationCancel(Animator animation) {
        super.onAnimationCancel(animation);
        animationEnd(endRun, to, animationView);
      }
      
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        animationEnd(endRun, to, animationView);
      }
    });
    elementAnimator.start();
    Log.i(DEBUG_TAG, "startElementAnimation to:" + to);
  }
  
  /**
   * 开始对传入的 view 做动画，这里做动画的参数为 width、height、x、y 等等
   *
   * @param to                element 进行动画的参数
   * @param endRun            动画完成时的操作
   * @param milTime           动画进行的时间
   * @param animationView     需要进行动画的 view
   * @param updateWidthHeight 更新 view width、height 的回调，需要在调用出定义
   */
  protected void startViewAnimationByChangeViewParam(
      TransformParam to,
      Runnable endRun,
      long milTime,
      View animationView,
      ElementContainerView.Consumer<ValueAnimator> updateWidthHeight) {
    
    if (to == null) {
      Log.e(DEBUG_TAG, "startViewAnimationByChangeViewParam error to is null");
      return;
    }
    
    limitTransformParam(to);
    
    AnimatorSet elementAnimator = new AnimatorSet();
    ObjectAnimator rotationAnimator = ObjectAnimator
        .ofFloat(animationView, "rotation", mRotate, to.mRotate);
    ObjectAnimator alphaYAnimator = ObjectAnimator
        .ofFloat(animationView, "alpha", mAlpha, to.mAlpha);
    
    ValueAnimator scaleAnimator = ValueAnimator.ofFloat(mScale, to.mScale);
    scaleAnimator.addUpdateListener(updateWidthHeight::accept);
    
    ValueAnimator translationXAnimator = ValueAnimator.ofFloat(mMoveX, to.mMoveX);
    translationXAnimator.addUpdateListener(animation -> {
      AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) animationView.getLayoutParams();
      layoutParams.x = (int) getRealX((Float) animation.getAnimatedValue(), animationView);
      animationView.setLayoutParams(layoutParams);
    });
    
    ValueAnimator translationYAnimator = ValueAnimator.ofFloat(mMoveY, to.mMoveY);
    translationYAnimator.addUpdateListener(animation -> {
      AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) animationView.getLayoutParams();
      layoutParams.y = (int) getRealY((Float) animation.getAnimatedValue(), animationView);
      animationView.setLayoutParams(layoutParams);
    });
    elementAnimator.playTogether(rotationAnimator, alphaYAnimator, scaleAnimator, translationXAnimator, translationYAnimator);
    elementAnimator.setDuration(milTime);
    elementAnimator.setInterpolator(new CubicEaseOutInterpolator());
    elementAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationCancel(Animator animation) {
        super.onAnimationCancel(animation);
        animationEnd(endRun, to, animationView);
      }
      
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        animationEnd(endRun, to, animationView);
      }
    });
    elementAnimator.start();
    Log.i(DEBUG_TAG, "startViewAnimationByChangeViewParam to:" + to);
  }
  
  public class CubicEaseOutInterpolator implements TimeInterpolator {
    @Override
    public float getInterpolation(float input) {
      input -= 1;
      return input * input * input + 1;
    }
  }
  
  private void animationEnd(Runnable endRun, TransformParam to, View animationView) {
    if (endRun != null) {
      endRun.run();
    }
    if (animationView == mElementShowingView) {
      mRotate = to.mRotate;
      mScale = to.mScale;
      mAlpha = to.mAlpha;
      mMoveX = to.mMoveX;
      mMoveY = to.mMoveY;
    }
  }
  
  /**
   * 限制动画的参数
   *
   * @param to
   */
  private void limitTransformParam(TransformParam to) {
    if (to == null) {
      return;
    }
    
    to.mMoveX = (to.mMoveX < (-1 * getLeftRightLimitLength()) ? (-1 * getLeftRightLimitLength()) : to.mMoveX);
    to.mMoveX = (to.mMoveX > getLeftRightLimitLength() ? getLeftRightLimitLength() : to.mMoveX);
    to.mMoveY = (to.mMoveY < (-1 * getBottomTopLimitLength()) ? (-1 * getBottomTopLimitLength()) : to.mMoveY);
    to.mMoveY = (to.mMoveY > getBottomTopLimitLength() ? getBottomTopLimitLength() : to.mMoveY);
    to.mScale = (to.mScale < MIN_SCALE_FACTOR ? MIN_SCALE_FACTOR : to.mScale);
    to.mScale = (to.mScale > MAX_SCALE_FACTOR ? MAX_SCALE_FACTOR : to.mScale);
  }
  
  public static class TransformParam {
    public float mRotate = 0f; // 图像顺时针旋转的角度
    
    public float mScale = 1.0f; // 图像缩放的大小
    
    public float mAlpha = 1.0f; // 图像的透明度
    
    public float mMoveX = 0f; // 初始化后相对 mElementContainerView 中心 的移动距离
    
    public float mMoveY = 0f; // 初始化后相对 mElementContainerView 中心 的移动距离
    
    public TransformParam() {
    }
    
    public TransformParam(WsElement element) {
      if (element == null) {
        return;
      }
      
      mRotate = element.mRotate;
      mScale = element.mScale;
      mAlpha = element.mAlpha;
      mMoveX = element.mMoveX;
      mMoveY = element.mMoveY;
    }
    
    @Override
    public String toString() {
      return "TransformParam{" +
          "mRotate=" + mRotate +
          ", mScale=" + mScale +
          ", mAlpha=" + mAlpha +
          ", mMoveX=" + mMoveX +
          ", mMoveY=" + mMoveY +
          '}';
    }
  }
}
