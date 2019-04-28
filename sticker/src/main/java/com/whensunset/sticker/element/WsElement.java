package com.whensunset.sticker.element;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.whensunset.sticker.container.ElementContainerView;


/**
 * Created by whensunset on 2019/3/6.
 * 各种元素的展示基类，用于容纳数据和被展示的 view
 */

public abstract class WsElement implements Cloneable {
  private static final String TAG = "heshixi:WsElement";
  
  protected static final float MIN_SCALE_FACTOR = 0.3F; // 最小缩放倍数
  protected static final float MAX_SCALE_FACTOR = 4F; // 最大缩放倍数
  
  // element 必须在屏幕中显示的最小宽度
  private static final int Element_LIMIT_AREA_WIDTH = 180;
  
  public int mZIndex = -1; // 图像的层级
  
  protected float mMoveX; // 初始化后相对 mElementContainerView 中心 的移动距离
  
  protected float mMoveY; // 初始化后相对 mElementContainerView 中心 的移动距离
  
  protected float mOriginWidth; // 初始化时内容的宽度
  
  protected float mOriginHeight; // 初始化时内容的高度
  
  protected Rect mEditRect; // 可绘制的区域
  
  protected float mRotate; // 图像顺时针旋转的角度
  
  protected float mScale = 1.0f; // 图像缩放的大小
  
  protected float mAlpha = 1.0f; // 图像的透明度
  
  protected boolean mIsSelected; // 是否处于选中状态
  
  protected boolean mIsSingeFingerMove; // 是否处于单指移动的状态
  
  protected boolean mIsDoubleFingerScaleAndRotate; // 是否处于双指旋转缩放的状态
  
  // element 中 mElementShowingView 的父 View，用于包容所有的 element 需要显示的 view
  protected ElementContainerView mElementContainerView;
  
  protected View mElementShowingView; // 用于展示内容的 view
  
  protected int mRedundantAreaLeftRight = 0; // 内容区域左右向外延伸的一段距离，用于扩展元素的可点击区域
  
  protected int mRedundantAreaTopBottom = 0; // 内容区域上下向外延伸的一段距离，用于扩展元素的可点击区域
  
  // 是否让 showing view 响应选中该 元素 之后的点击事件
  protected boolean mIsResponseSelectedClick = false;
  
  // 是否在刷新 showing view 的时候，真正修改 height、width 之类的参数。一般来说只是使用 scale 和 rotate 来刷新 view
  protected boolean mIsRealUpdateShowingViewParams = false;
  
  protected WsElement(float originWidth, float originHeight) {
    this();
    mOriginWidth = originWidth;
    mOriginHeight = originHeight;
  }
  
  protected WsElement() {
  }
  
  /**
   * 当前 element 被添加到 DecorationContainerView 中
   *
   * @param elementContainerView
   */
  public void add(ElementContainerView elementContainerView) {
    mElementContainerView = elementContainerView;
    
    if (mElementShowingView == null) {
      mElementShowingView = initView();
      AbsoluteLayout.LayoutParams showingViewLayoutParams = new AbsoluteLayout.LayoutParams((int) mOriginWidth, (int) mOriginWidth, 0, 0);
      mElementShowingView.setLayoutParams(showingViewLayoutParams);
      mElementContainerView.addView(mElementShowingView);
    } else {
      update();
    }
  }
  
  /**
   * 初始化需要展示的 view，返回需要展示的 view
   */
  protected abstract View initView();
  
  /**
   * 当前 element 被从 DecorationContainerView 中删除
   */
  public void remove() {
    mElementContainerView.removeView(mElementShowingView);
    mElementContainerView = null;
  }
  
  /**
   * 当前 element 开始单指移动
   */
  public void onSingleFingerMoveStart() {
    mIsSingeFingerMove = true;
  }
  
  /**
   * 当前 element 单指移动中
   */
  public void onSingleFingerMoveProcess(float motionEventX, float motionEventY) {
    mMoveX += motionEventX;
    mMoveY += motionEventY;
  }
  
  /**
   * 当前 element 单指移动结束
   */
  public void onSingleFingerMoveEnd() {
    mIsSingeFingerMove = false;
  }
  
  /**
   * 当前 element 开始双指旋转缩放
   */
  public void onDoubleFingerScaleAndRotateStart(float deltaRotate, float deltaScale) {
    doubleFingerScaleAndRotate(deltaRotate, deltaScale);
    mIsDoubleFingerScaleAndRotate = true;
  }
  
  /**
   * 当前 element 双指旋转缩放中
   */
  public void onDoubleFingerScaleAndRotateProcess(float deltaRotate, float deltaScale) {
    doubleFingerScaleAndRotate(deltaRotate, deltaScale);
  }
  
  /**
   * 当前 element 双指旋转缩放结束
   */
  public void onDoubleFingerScaleAndRotateEnd() {
    mIsDoubleFingerScaleAndRotate = false;
  }
  
  private void doubleFingerScaleAndRotate(float deltaRotate, float deltaScale) {
    mScale *= deltaScale;
    mScale = (mScale < MIN_SCALE_FACTOR ? MIN_SCALE_FACTOR : mScale);
    mScale = (mScale > MAX_SCALE_FACTOR ? MAX_SCALE_FACTOR : mScale);
    mRotate += deltaRotate;
    mRotate = (mRotate % 360);
  }
  
  /**
   * 当前 element 选中了
   */
  public void select() {
    mZIndex = 0;
    mIsSelected = true;
    mElementShowingView.bringToFront();
  }
  
  /**
   * 当前 element 取消选中了
   */
  public void unSelect() {
    mIsSelected = false;
  }
  
  /**
   * 选中 element 之后，如果 showing view 不响应事件的话，那么就会调用这个方法
   *
   * @param e
   */
  public void selectedClick(MotionEvent e) {
  
  }
  
  /**
   * 刷新展示的 view
   */
  public void update() {
    if (isRealChangeShowingView()) {
      AbsoluteLayout.LayoutParams showingViewLayoutParams = (AbsoluteLayout.LayoutParams) mElementShowingView.getLayoutParams();
      showingViewLayoutParams.width = (int) (mOriginWidth * mScale);
      showingViewLayoutParams.height = (int) (mOriginHeight * mScale);
      if (!limitElementAreaLeftRight()) {
        mMoveX = (mMoveX < 0 ? -1 * getLeftRightLimitLength() : getLeftRightLimitLength());
      }
      showingViewLayoutParams.x = (int) getRealX(mMoveX, mElementShowingView);
      
      if (!limitElementAreaTopBottom()) {
        mMoveY = (mMoveY < 0 ? -1 * getBottomTopLimitLength() : getBottomTopLimitLength());
      }
      showingViewLayoutParams.y = (int) getRealY(mMoveY, mElementShowingView);
      mElementShowingView.setLayoutParams(showingViewLayoutParams);
    } else {
      mElementShowingView.setScaleX(mScale);
      mElementShowingView.setScaleY(mScale);
      if (!limitElementAreaLeftRight()) {
        mMoveX = (mMoveX < 0 ? -1 * getLeftRightLimitLength() : getLeftRightLimitLength());
      }
      mElementShowingView.setTranslationX(getRealX(mMoveX, mElementShowingView));
      
      if (!limitElementAreaTopBottom()) {
        mMoveY = (mMoveY < 0 ? -1 * getBottomTopLimitLength() : getBottomTopLimitLength());
      }
      mElementShowingView.setTranslationY(getRealY(mMoveY, mElementShowingView));
    }
    mElementShowingView.setRotation(mRotate);
  }
  
  /**
   * 获取 view 在 mEditRect 中的真实位置
   *
   * @return
   */
  protected float getRealX(float moveX, View view) {
    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
    return mEditRect.centerX() + moveX - layoutParams.width / 2;
  }
  
  /**
   * 同 getRealX
   *
   * @return
   */
  protected float getRealY(float moveY, View view) {
    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
    return mEditRect.centerY() + moveY - layoutParams.height / 2;
  }
  
  /**
   * 限制 element 左右移动的区域
   *
   * @return false 表示已达到限制区域，不可再继续移动
   */
  protected boolean limitElementAreaLeftRight() {
    float halfLimitWidthLength = getLeftRightLimitLength();
    Log.d(TAG, "limitElementAreaLeftRight halfWidth:" + halfLimitWidthLength + ",moveX:" + mMoveX);
    return (-1 * halfLimitWidthLength <= mMoveX && mMoveX <= halfLimitWidthLength);
  }
  
  /**
   * 同 limitElementAreaLeftRight 限制上下的范围
   *
   * @return
   */
  protected boolean limitElementAreaTopBottom() {
    float halfLimitHeightLength = getBottomTopLimitLength();
    Log.d(TAG, "limitElementAreaLeftRight halfHeight:" + halfLimitHeightLength + ",moveY:" + mMoveY);
    return (-1 * halfLimitHeightLength <= mMoveY && mMoveY <= halfLimitHeightLength);
  }
  
  protected float getLeftRightLimitLength() {
    return mEditRect.width() / 2 + getWholeRect().width() / 2 - Element_LIMIT_AREA_WIDTH;
  }
  
  protected float getBottomTopLimitLength() {
    return mEditRect.height() / 2 + getWholeRect().height() / 2 - Element_LIMIT_AREA_WIDTH;
  }
  
  protected static float getCanonicalRotation(float rotation) {
    if (Math.abs(rotation % 90) < 3) {
      return (float) (Math.round(rotation / 90) * 90);
    }
    if (Math.abs(rotation % 45) < 3) {
      return (float) (Math.round(rotation / 45) * 45);
    }
    return rotation;
  }
  
  /**
   * 判断坐标是否处于整个 元素 中，包括延伸区域
   *
   * @param motionEventX
   * @param motionEventY
   * @return
   */
  public boolean isInWholeDecoration(float motionEventX, float motionEventY) {
    return isPointInTheRect(motionEventX, motionEventY, getWholeRect());
  }
  
  protected boolean isPointInTheRect(float motionEventX, float motionEventY, Rect rect) {
    PointF afterRotatePoint = new PointF(motionEventX, motionEventY);
    if (mRotate != 0) {
      Matrix mInvertMatrix = new Matrix();
      mInvertMatrix.postRotate(-mRotate, getContentRect().centerX(),
          getContentRect().centerY());
      float[] point = new float[]{motionEventX, motionEventY};
      mInvertMatrix.mapPoints(point);
      afterRotatePoint = new PointF(point[0], point[1]);
    }
    Log.d(TAG,
        "isPointInTheRect rect:" + rect + ",model:" + this);
    return rect.contains((int) afterRotatePoint.x, (int) afterRotatePoint.y);
  }
  
  public static boolean isSameElement(WsElement wsElementOne,
                                      WsElement wsElementTwo) {
    if (wsElementOne == null || wsElementTwo == null) {
      return false;
    } else {
      return wsElementOne.equals(wsElementTwo);
    }
  }
  
  /**
   * 获取 元素 内容区域，不包括延伸区域
   *
   * @return
   */
  public Rect getContentRect() {
    float viewCenterX = mEditRect.centerX();
    float viewCenterY = mEditRect.centerY();
    float contentWidth = mOriginWidth * mScale;
    float contentHeight = mOriginHeight * mScale;
    
    return new Rect(
        (int) (viewCenterX + mMoveX
            - (contentWidth / 2)),
        (int) (viewCenterY + mMoveY
            - (contentHeight / 2)),
        (int) (viewCenterX + mMoveX
            + (contentWidth / 2)),
        (int) (viewCenterY + mMoveY
            + (contentHeight / 2)));
  }
  
  /**
   * 获取 元素 整个区域，包括延伸区域
   *
   * @return
   */
  protected Rect getWholeRect() {
    Rect dstDrawRect = getContentRect();
    return new Rect(
        dstDrawRect.left - mRedundantAreaLeftRight,
        dstDrawRect.top - mRedundantAreaLeftRight,
        dstDrawRect.right + mRedundantAreaTopBottom,
        dstDrawRect.bottom + mRedundantAreaTopBottom);
  }
  
  public void setEditRect(Rect editRect) {
    mEditRect = editRect;
  }
  
  /**
   * 是否让 showing view 响应选中该 元素 之后的点击事件
   * 只有在刷新的时候真正修改了 showing view 的 params 才能响应点击事件
   * 要不然事件分发会出错
   *
   * @return
   */
  public boolean isShowingViewResponseSelectedClick() {
    return mIsResponseSelectedClick && mIsRealUpdateShowingViewParams;
  }
  
  /**
   * 是否在刷新 showing view 的时候，真正修改 height、width 之类的参数。一般来说只是使用 scale 和 rotate 来刷新 view
   *
   * @return
   */
  public boolean isRealChangeShowingView() {
    return mIsRealUpdateShowingViewParams;
  }
  
  public boolean isSingerFingerMove() {
    return mIsSingeFingerMove;
  }
  
  public boolean isDoubleFingerScaleAndRotate() {
    return mIsDoubleFingerScaleAndRotate;
  }
  
  public View getElementShowingView() {
    return mElementShowingView;
  }
  
  public float getRotate() {
    return mRotate;
  }
}
