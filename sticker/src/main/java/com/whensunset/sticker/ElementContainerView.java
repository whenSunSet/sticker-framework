package com.whensunset.sticker;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.AbsoluteLayout;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.whensunset.sticker.CommonUtil.copyMotionEvent;

/**
 * Created by whensunset on 2019/3/18.
 * 容纳元素的基类，用于接收各种手势操作和维持数据结构
 */

public class ElementContainerView extends AbsoluteLayout {
  private static final String TAG = "WhenSunset:ECV";
  
  private BaseActionMode mMode = BaseActionMode.SELECTED_CLICK_OR_MOVE; // 当前手势所处的模式
  private Rect mEditorRect = new Rect(); // 元素 可绘制的区域，也就是当前 View 的区域
  private GestureDetector mDetector; // 处理单指手势
  private MultiTouchGestureDetector mMultiTouchGestureDetector; // 处理双指手势
  private boolean mIsInDoubleFinger; // 是否处于双指手势状态
  protected boolean mIsNeedAutoUnSelect = true; // 是否需要自动取消选中
  protected long mAutoUnSelectDuration = 2000; // 自动取消选中的时间，默认 2000 毫秒，
  protected Runnable mUnSelectRunnable = this::unSelectElement;
  protected WsElement mSelectedElement; // 当前选中的 元素
  protected LinkedList<WsElement> mElementList = new LinkedList<>();
  protected Set<ElementActionListener> mElementActionListenerSet = new HashSet<>(); // 监听列表
  protected MotionEvent[] mUpDownMotionEvent = new MotionEvent[2]; // 储存当前 up down 事件，以便在需要的时候进行事件分发
  protected Vibrator mVibrator;
  
  
  {
    init();
  }
  
  public ElementContainerView(Context context) {
    super(context);
  }
  
  public ElementContainerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  public ElementContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    
  }
  
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public ElementContainerView(Context context, AttributeSet attrs, int defStyleAttr,
                              int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }
  
  protected void init() {
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
      @Override
      public void onGlobalLayout() {
        viewLayoutComplete();
        if (getWidth() != 0 && getHeight() != 0) {
          getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        Log.d(TAG, "init |||||||||| mEditorRect:" + mEditorRect);
      }
    });
    
    addDetector();
    mVibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
  }
  
  /**
   * view layout 完成 width 和 height 都有了，这个时候可以做一些初始化的事情
   */
  protected void viewLayoutComplete() {
    mEditorRect.set(0, 0, getWidth(), getHeight());
  }
  
  // --------------------------------------- 手势操作开始 ---------------------------------------
  private void addDetector() {
    mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
      @Override
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return ElementContainerView.this.onFling(e1, e2, velocityX, velocityY);
      }
      
      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return singleFingerMove(e2, new float[]{distanceX, distanceY});
      }
    });
    
    mMultiTouchGestureDetector = new MultiTouchGestureDetector(getContext(), new MultiTouchGestureDetector.SimpleOnMultiTouchGestureListener() {
      boolean mIsMultiTouchBegin = false;
      @Override
      public void onScaleOrRotate(MultiTouchGestureDetector detector) {
        if (mIsInDoubleFinger) {
          doubleFingerScaleAndRotateProcess(detector.getRotation(), detector.getScale());
        } else {
          doubleFingerScaleAndRotateStart(detector.getRotation(), detector.getScale());
          mIsInDoubleFinger = true;
        }
      }
      
      @Override
      public void onMove(MultiTouchGestureDetector detector) {
         if (mIsMultiTouchBegin) {
          mIsMultiTouchBegin = false;
        } else {
          mSelectedElement.onSingleFingerMoveProcess(detector.getMoveX(), detector.getMoveY());
        }
      }
      
       @Override
      public boolean onBegin(MultiTouchGestureDetector detector) {
        mIsMultiTouchBegin = true;
        return super.onBegin(detector);
      }
      
      @Override
      public void onEnd(MultiTouchGestureDetector detector) {
        super.onEnd(detector);
        mIsMultiTouchBegin = false;
      }
    });
  }
  
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {

    //空白区域，没有选中元素，放行
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      final float x = ev.getX(), y = ev.getY();
      WsElement clickedElement = findElementByPosition(x, y);
      if (clickedElement == null && mSelectedElement == null) {
        return false;
      }
    }

    if (mSelectedElement != null && mSelectedElement.isShowingViewResponseSelectedClick()) {
      if (ev.getAction() == MotionEvent.ACTION_DOWN) {
        mUpDownMotionEvent[0] = copyMotionEvent(ev);
      } else if (ev.getAction() == MotionEvent.ACTION_UP) {
        mUpDownMotionEvent[1] = copyMotionEvent(ev);
      }
    }
    return super.dispatchTouchEvent(ev);
  }
  
  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    return true;
  }
  
  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    Log.d(TAG, "initDoubleFingerDetector |||||||||| mIsInDoubleFinger:"
        + mIsInDoubleFinger + ",x0:" + event.getX() + ",y0:" + event.getY());
    
    if (isDoubleFingerInSelectElement(event)) {
      mMultiTouchGestureDetector.onTouchEvent(event);
    } else {
      if (mIsInDoubleFinger) {
        doubleFingerScaleAndRotateEnd();
        mIsInDoubleFinger = false;
        return true;
      }
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          cancelAutoUnSelectDecoration();
          singleFingerDown(event);
          //单指操作不在子控件，进行事件分发,不作全局拦截
          if (mMode == BaseActionMode.SINGLE_TAP_BLANK_SCREEN) {
            autoUnSelectDecoration();
            singleFingerUp(event);
            return false;
          }
          break;
        case MotionEvent.ACTION_MOVE:
          mDetector.onTouchEvent(event);
          break;
        case MotionEvent.ACTION_UP:
          autoUnSelectDecoration();
          singleFingerUp(event);
          break;
      }
    }
    return true;
  }
  
  private boolean isDoubleFingerInSelectElement(MotionEvent event) {
    if (mSelectedElement != null && event.getPointerCount() > 1) {
      final double x0 = event.getX(0);
      final double y0 = event.getY(0);
      final double x1 = event.getX(1);
      final double y1 = event.getY(1);
      if (mSelectedElement.isInWholeDecoration((float) x0, (float) y0)
          || mSelectedElement.isInWholeDecoration((float) x1, (float) y1)) {
        Log.d(TAG, "isDoubleFingerInSelectElement |||||||||| x0:" + x0 + ",x1:" + x1 + ",y0:" + y0 + ",y1:" + y1);
        return true;
      }
    }
    return false;
  }
  
  private void singleFingerDown(MotionEvent e) {
    final float x = e.getX(), y = e.getY();
    mMode = BaseActionMode.SELECTED_CLICK_OR_MOVE;
    WsElement clickedElement = findElementByPosition(x, y);
    
    Log.d(TAG, "singleFingerDown |||||||||| x:" + x + ",y:" + y + ",clickedElement:" + clickedElement + ",mSelectedElement:" + mSelectedElement);
    if (mSelectedElement != null) {
      if (WsElement.isSameElement(clickedElement,
          mSelectedElement)) {
        boolean result = downSelectTapOtherAction(e);
        if (result) {
          Log.d(TAG, "singleFingerDown other action");
          return;
        }
        if (mSelectedElement.isInWholeDecoration(x, y)) {
          mMode = BaseActionMode.SELECTED_CLICK_OR_MOVE;
          Log.d(TAG, "singleFingerDown SELECTED_CLICK_OR_MOVE");
          return;
        }
        Log.e(TAG, "singleFingerDown error not action");
      } else {
        if (clickedElement == null) {
          mMode = BaseActionMode.SINGLE_TAP_BLANK_SCREEN;
          Log.d(TAG, "singleFingerDown SINGLE_TAP_BLANK_SCREEN");
        } else {
          mMode = BaseActionMode.SELECT;
          unSelectElement();
          selectElement(clickedElement);
          update();
          Log.d(TAG, "singleFingerDown unSelect old element, select new element");
        }
      }
    } else {
      if (clickedElement != null) {
        mMode = BaseActionMode.SELECT;
        selectElement(clickedElement);
        update();
        Log.d(TAG, "singleFingerDown select new element");
      } else {
        mMode = BaseActionMode.SINGLE_TAP_BLANK_SCREEN;
        Log.d(TAG, "singleFingerDown SINGLE_TAP_BLANK_SCREEN");
      }
    }
  }
  
  private boolean singleFingerMove(MotionEvent e2, float[] distanceXY) {
    Log.d(TAG,
        "singleFingerMove move |||||||||| distanceX:" + distanceXY[0] + "distanceY:"
            + distanceXY[1]);
    if (scrollSelectTapOtherAction(e2, distanceXY)) {
      return true;
    } else {
      if (mMode == BaseActionMode.SELECTED_CLICK_OR_MOVE
          || mMode == BaseActionMode.SELECT
          || mMode == BaseActionMode.MOVE) {
        if (mMode == BaseActionMode.SELECTED_CLICK_OR_MOVE || mMode == BaseActionMode.SELECT) {
          singleFingerMoveStart(distanceXY[0], distanceXY[1]);
        } else {
          singleFingerMoveProcess(distanceXY[0], distanceXY[1]);
        }
        update();
        mMode = BaseActionMode.MOVE;
        return true;
      }
    }
    return false;
  }
  
  private void singleFingerUp(MotionEvent event) {
    Log.d(TAG, "singleFingerUp ||||||||||  x:" + event.getX() + ",y:" + event.getY());
    if (!upSelectTapOtherAction(event)) {
      switch (mMode) {
        case SELECTED_CLICK_OR_MOVE:
          selectedClick(event);
          update();
          return;
        case SINGLE_TAP_BLANK_SCREEN:
          onClickBlank();
          return;
        case MOVE:
          singleFingerMoveEnd();
          return;
        default:
          Log.d(TAG, "singleFingerUp other action");
      }
    }
  }
  
  // --------------------------------------- 手势操作结束 ---------------------------------------
  
  
  // --------------------------------------- 子类手势复写开始 ---------------------------------------
  protected void singleFingerMoveStart(float distanceX, float distanceY) {
    mSelectedElement.onSingleFingerMoveStart();
    callListener(elementActionListener -> elementActionListener
        .onSingleFingerMoveStart(mSelectedElement));
    Log.d(TAG, "singleFingerMoveStart move start |||||||||| distanceX:" + distanceX + ",distanceY:" + distanceY);
  }
  
  protected void singleFingerMoveProcess(float distanceX, float distanceY) {
    mSelectedElement.onSingleFingerMoveProcess(-distanceX, -distanceY);
    callListener(elementActionListener -> elementActionListener
        .onSingleFingerMoveProcess(mSelectedElement));
    Log.d(TAG, "singleFingerMoveStart move progress |||||||||| distanceX:" + distanceX + ",distanceY:" + distanceY);
  }
  
  protected void singleFingerMoveEnd() {
    callListener(elementActionListener -> elementActionListener
        .onSingleFingerMoveEnd(mSelectedElement));
    mSelectedElement.onSingleFingerMoveEnd();
    Log.d(TAG, "singleFingerMoveEnd move end");
  }
  
  protected void doubleFingerScaleAndRotateStart(float deltaRotate, float deltaScale) {
    mMode = BaseActionMode.DOUBLE_FINGER_SCALE_AND_ROTATE;
    mSelectedElement.onDoubleFingerScaleAndRotateStart(deltaRotate, deltaScale);
    update();
    callListener(elementActionListener -> elementActionListener
        .onDoubleFingerScaleAndRotateStart(mSelectedElement));
    mIsInDoubleFinger = true;
    Log.d(TAG, "doubleFingerScaleAndRotateStart start |||||||||| deltaRotate:" + deltaRotate + ",deltaScale:" + deltaScale);
  }
  
  protected void doubleFingerScaleAndRotateProcess(float deltaRotate, float deltaScale) {
    mSelectedElement.onDoubleFingerScaleAndRotateProcess(deltaRotate, deltaScale);
    update();
    callListener(elementActionListener -> elementActionListener
        .onDoubleFingerScaleAndRotateProcess(mSelectedElement));
    Log.d(TAG, "doubleFingerScaleAndRotateStart process |||||||||| deltaRotate:" + deltaRotate + ",deltaScale:" + deltaScale);
  }
  
  protected void doubleFingerScaleAndRotateEnd() {
    mSelectedElement.onDoubleFingerScaleAndRotateEnd();
    callListener(elementActionListener -> elementActionListener
        .onDoubleFingerScaleRotateEnd(mSelectedElement));
    mIsInDoubleFinger = false;
    autoUnSelectDecoration();
    Log.d(TAG, "doubleFingerScaleAndRotateEnd end");
  }
  
  protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return true;
  }
  
  protected void onClickBlank() {
    callListener(elementActionListener -> elementActionListener
        .onSingleTapBlankScreen(mSelectedElement));
    Log.d(TAG, "onClickBlank");
  }
  
  /**
   * 按下了已经选中的元素，如果子类中有操作的话可以给它，优先级最高
   *
   * @return
   */
  protected boolean downSelectTapOtherAction(@NonNull MotionEvent event) {
    Log.d(TAG, "downSelectTapOtherAction |||||||||| event:" + event);
    return false;
  }
  
  /**
   * 滑动已经选中的元素，如果子类中有操作的话可以给它，优先级最高
   *
   * @param event    当前的触摸事件
   * @param distance size 为 2，里面分别为 x 轴的 delta 位移，和 y 轴的 delta 位移
   * @return
   */
  protected boolean scrollSelectTapOtherAction(@NonNull MotionEvent event, float[] distance) {
    Log.d(TAG, "scrollSelectTapOtherAction |||||||||| event:" + event + ",distanceX:" + distance[0] + ",distanceY:" + distance[1]);
    return false;
  }
  
  /**
   * 抬起已经选中的元素，如果子类中有操作的话可以给它，优先级最高
   *
   * @return
   */
  protected boolean upSelectTapOtherAction(@NonNull MotionEvent event) {
    Log.d(TAG, "upSelectTapOtherAction |||||||||| event:" + event);
    return false;
  }
  // --------------------------------------- 子类手势复写结束 ---------------------------------------
  
  /**
   * 添加一个监听器
   *
   * @param elementActionListener
   */
  public void addElementActionListener(ElementActionListener elementActionListener) {
    if (elementActionListener == null) {
      return;
    }
    mElementActionListenerSet.add(elementActionListener);
  }
  
  /**
   * 移除一个监听器
   *
   * @param elementActionListener
   */
  public void removeElementActionListener(ElementActionListener elementActionListener) {
    mElementActionListenerSet.remove(elementActionListener);
  }
  
  /**
   * 根据 position 移除一个监听器
   *
   * @param position
   */
  public void removeElementActionListener(int position) {
    if (0 <= position && position < mElementActionListenerSet.size()) {
      mElementActionListenerSet.remove(position);
    }
  }
  
  @Nullable
  public WsElement getSelectElement() {
    return mSelectedElement;
  }
  
  public List<WsElement> getElementList() {
    return mElementList;
  }
  
  // --------------------------------------- Element 操作开始 ---------------------------------------
  
  /**
   * 添加一个元素，如果元素已经存在，那么就会添加失败
   *
   * @param element 被添加的元素
   * @return 是否元素添加成功
   */
  public boolean addElement(WsElement element) {
    Log.i(TAG, "addElement |||||||||| element:" + element);
    if (element == null) {
      Log.w(TAG, "addElement element is null");
      return false;
    }
    
    if (mElementList.contains(element)) {
      Log.w(TAG, "addElement element is added");
      return false;
    }
    
    for (int i = 0; i < mElementList.size(); i++) {
      WsElement nowElement = mElementList.get(i);
      nowElement.mZIndex++;
    }
    element.mZIndex = 0;
    element.setEditRect(mEditorRect);
    mElementList.addFirst(element);
    element.add(this);
    callListener(
        elementActionListener -> elementActionListener.onAdd(element));
    autoUnSelectDecoration();
    return true;
  }
  
  /**
   * 默认删除最顶层的元素
   *
   * @return
   */
  public boolean deleteElement() {
    if (mElementList.size() <= 0) {
      return false;
    }
    
    return deleteElement(mElementList.getFirst());
  }
  
  /**
   * 删除一个元素，只能删除当前最顶层的元素
   *
   * @param element 被删除的元素
   * @return 删除是否成功
   */
  public boolean deleteElement(WsElement element) {
    Log.i(TAG, "deleteElement |||||||||| element:" + element);
    if (element == null) {
      Log.w(TAG, "deleteElement element is null");
      return false;
    }
    
    if (mElementList.getFirst() != element) {
      Log.w(TAG, "deleteElement element is not in top");
      return false;
    }
    
    mElementList.pop();
    for (int i = 0; i < mElementList.size(); i++) {
      WsElement nowElement = mElementList.get(i);
      nowElement.mZIndex--;
    }
    element.remove();
    callListener(
        elementActionListener -> elementActionListener
            .onDelete(element));
    return true;
  }
  
  /**
   * 刷新界面，具体绘制逻辑在 @Element 中
   */
  public void update() {
    if (mSelectedElement == null) {
      Log.w(TAG, "update error selected element is null");
      return;
    }
    mSelectedElement.update();
    Log.d(TAG, "update");
  }
  
  /**
   * 根据位置找到 元素
   *
   * @param x container view 中的坐标
   * @param y container view 中的坐标
   * @return
   */
  @Nullable
  public WsElement findElementByPosition(float x, float y) {
    WsElement realFoundedElement = null;
    for (int i = mElementList.size() - 1; i >= 0; i--) {
      WsElement nowElement = mElementList.get(i);
      if (nowElement.isInWholeDecoration(x, y)) {
        realFoundedElement = nowElement;
      }
    }
    
    Log.d(TAG, "findElementByPosition |||||||||| realFoundedElement:"
        + realFoundedElement + ",x:" + x + ",y:" + y);
    return realFoundedElement;
  }
  
  /**
   * 选中一个元素，如果需要选中的元素没有被添加到 container 中则选中失败
   *
   * @param element 被选中的元素
   * @return 是否选中成功
   */
  public boolean selectElement(WsElement element) {
    Log.i(TAG, "selectElement |||||||||| element:" + element);
    if (element == null) {
      Log.w(TAG, "selectElement element is null");
      return false;
    }
    
    if (!mElementList.contains(element)) {
      Log.w(TAG, "selectElement element was not added");
      return false;
    }
    
    for (int i = 0; i < mElementList.size(); i++) {
      WsElement nowElement = mElementList.get(i);
      if (!element.equals(nowElement)
          && element.mZIndex > nowElement.mZIndex) {
        nowElement.mZIndex++;
      }
    }
    mElementList.remove(element.mZIndex);
    element.select();
    mElementList.addFirst(element);
    mSelectedElement = element;
    callListener(
        elementActionListener -> elementActionListener
            .onSelect(element));
    return true;
  }
  
  /**
   * 取消选中当前元素
   *
   * @return true 表示被选中的 元素 取消选中成功，false 表示当前没有被选中的 元素
   */
  public boolean unSelectElement() {
    Log.i(TAG,
        "unSelectElement |||||||||| mSelectedElement:" + mSelectedElement);
    if (mSelectedElement == null) {
      Log.w(TAG, "unSelectElement unSelect element is null");
      return false;
    }
    
    if (!mElementList.contains(mSelectedElement)) {
      Log.w(TAG, "unSelectElement unSelect elemnt not in container");
      return false;
    }
    
    callListener(
        elementActionListener -> elementActionListener
            .onUnSelect(mSelectedElement));
    mSelectedElement.unSelect();
    mSelectedElement = null;
    return true;
  }
  
  /**
   * 选中之后再次点击选中的元素
   */
  protected void selectedClick(MotionEvent e) {
    if (mSelectedElement == null) {
      Log.w(TAG, "selectedClick selected element is null");
    } else {
      if (mSelectedElement.isShowingViewResponseSelectedClick()) {
        mUpDownMotionEvent[0].setLocation(
            mUpDownMotionEvent[0].getX() - mSelectedElement.mElementShowingView.getLeft(),
            mUpDownMotionEvent[0].getY() - mSelectedElement.mElementShowingView.getTop());
        rotateMotionEvent(mUpDownMotionEvent[0], mSelectedElement);
        
        mUpDownMotionEvent[1].setLocation(
            mUpDownMotionEvent[1].getX() - mSelectedElement.mElementShowingView.getLeft(),
            mUpDownMotionEvent[1].getY() - mSelectedElement.mElementShowingView.getTop());
        rotateMotionEvent(mUpDownMotionEvent[1], mSelectedElement);
        mSelectedElement.mElementShowingView.dispatchTouchEvent(mUpDownMotionEvent[0]);
        mSelectedElement.mElementShowingView.dispatchTouchEvent(mUpDownMotionEvent[1]);
      } else {
        mSelectedElement.selectedClick(e);
      }
      callListener(
          elementActionListener -> elementActionListener
              .onSelectedClick(mSelectedElement));
    }
  }
  
  /**
   * 将 @event 旋转 @element 中的角度
   *
   * @param event
   * @param element
   */
  protected void rotateMotionEvent(MotionEvent event, WsElement element) {
    if (element.mRotate != 0) {
      Matrix mInvertMatrix = new Matrix();
      mInvertMatrix.postRotate(
          -element.mRotate,
          element.mElementShowingView.getWidth() / 2,
          element.mElementShowingView.getHeight() / 2);
      float[] point = new float[]{event.getX(), event.getY()};
      mInvertMatrix.mapPoints(point);
      event.setLocation(point[0], point[1]);
    }
  }
  
  /**
   * 一定的时间之后自动取消当前元素的选中
   */
  public void autoUnSelectDecoration() {
    if (mIsNeedAutoUnSelect) {
      cancelAutoUnSelectDecoration();
      postDelayed(mUnSelectRunnable, mAutoUnSelectDuration);
    }
  }
  
  /**
   * 取消自动取消选中
   */
  public void cancelAutoUnSelectDecoration() {
    removeCallbacks(mUnSelectRunnable);
  }
  
  /**
   * 是否需要自动取消选中
   *
   * @param needAutoUnSelect
   */
  public void setNeedAutoUnSelect(boolean needAutoUnSelect) {
    mIsNeedAutoUnSelect = needAutoUnSelect;
  }
  
  // --------------------------------------- Element 操作结束 ---------------------------------------
  
  
  // --------------------------------------- 接口开始 ---------------------------------------
  
  protected void callListener(Consumer<ElementActionListener> decorationActionListenerConsumer) {
    for (ElementActionListener elementActionListener : mElementActionListenerSet) {
      try {
        decorationActionListenerConsumer.accept(elementActionListener);
      } catch (Exception e) {
      }
    }
  }
  
  public interface ElementActionListener {
    
    /**
     * 增加了一个元素之后的回调
     *
     * @param element
     */
    void onAdd(WsElement element);
    
    /**
     * 删除了一个元素之后的回调
     *
     * @param element
     */
    void onDelete(WsElement element);
    
    /**
     * 选中了一个元素之后再次点击该元素触发的事件
     *
     * @param element
     */
    void onSelectedClick(WsElement element);
    
    /**
     * 选中了元素之后，对元素单指移动开始的回调
     *
     * @param element
     */
    void onSingleFingerMoveStart(WsElement element);
    
    /**
     * 选中了元素之后，对元素单指移动过程的回调
     *
     * @param element
     */
    void onSingleFingerMoveProcess(WsElement element);
    
    /**
     * 一次 单指移动操作结束的回调
     */
    void onSingleFingerMoveEnd(WsElement element);
    
    /**
     * 选中了元素之后，对元素双指旋转缩放开始的回调
     *
     * @param element
     */
    void onDoubleFingerScaleAndRotateStart(WsElement element);
    
    /**
     * 选中了元素之后，对元素双指旋转缩放过程的回调
     *
     * @param element
     */
    void onDoubleFingerScaleAndRotateProcess(WsElement element);
    
    /**
     * 一次 双指旋转、缩放 操作结束的回调
     *
     * @param element
     */
    void onDoubleFingerScaleRotateEnd(WsElement element);
    
    /**
     * 选中元素
     *
     * @param element
     */
    void onSelect(WsElement element);
    
    /**
     * 取消选中元素
     *
     * @param element
     */
    void onUnSelect(WsElement element);
    
    /**
     * 点击空白区域
     *
     * @param element
     */
    void onSingleTapBlankScreen(WsElement element);
  }
  
  public static class DefaultElementActionListener implements ElementActionListener {
    
    @Override
    public void onAdd(WsElement element) {
      Log.d(TAG, "onAdd");
    }
    
    @Override
    public void onDelete(WsElement element) {
      Log.d(TAG, "onDelete");
    }
    
    @Override
    public void onSelectedClick(WsElement element) {
      Log.d(TAG, "onSelectedClick");
    }
    
    @Override
    public void onSingleFingerMoveStart(WsElement element) {
      Log.d(TAG, "onSingleFingerMoveStart");
    }
    
    @Override
    public void onSingleFingerMoveProcess(WsElement element) {
      Log.d(TAG, "onSingleFingerMoveProcess");
    }
    
    @Override
    public void onSelect(WsElement element) {
      Log.d(TAG, "onSelect");
    }
    
    @Override
    public void onUnSelect(WsElement element) {
      Log.d(TAG, "onUnSelect");
    }
    
    @Override
    public void onSingleFingerMoveEnd(WsElement element) {
      Log.d(TAG, "onSingleFingerMoveEnd");
    }
    
    @Override
    public void onDoubleFingerScaleAndRotateStart(WsElement element) {
      Log.d(TAG, "onDoubleFingerScaleAndRotateStart");
    }
    
    @Override
    public void onDoubleFingerScaleAndRotateProcess(WsElement element) {
      Log.d(TAG, "onDoubleFingerScaleAndRotateProcess");
    }
    
    @Override
    public void onDoubleFingerScaleRotateEnd(WsElement element) {
      Log.d(TAG, "onDoubleFingerScaleRotateEnd");
    }
    
    @Override
    public void onSingleTapBlankScreen(WsElement element) {
      Log.d(TAG, "onSingleTapBlankScreen");
    }
  }
  
  public interface Consumer<T> {
    
    void accept(T t);
  }
  
  public enum BaseActionMode {
    MOVE,
    SELECT,
    SELECTED_CLICK_OR_MOVE,
    SINGLE_TAP_BLANK_SCREEN,
    DOUBLE_FINGER_SCALE_AND_ROTATE,
  }
  // --------------------------------------- 接口结束 ---------------------------------------
}
