package com.whensunset.sticker.container;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.AbsoluteLayout;

import com.whensunset.sticker.MultiTouchGestureDetector;
import com.whensunset.sticker.container.worker.ContainerWorker;
import com.whensunset.sticker.element.WsElement;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by whensunset on 2019/3/18.
 * 容纳元素的基类，用于接收各种手势操作和维持数据结构
 */

public abstract class ElementContainerView extends AbsoluteLayout {
  private static final String TAG = "WhenSunset:ECV";
  
  private BaseActionMode mMode = BaseActionMode.SELECTED_CLICK_OR_MOVE; // 当前手势所处的模式
  private Rect mEditorRect = new Rect(); // 元素 可绘制的区域，也就是当前 View 的区域
  private GestureDetector mDetector; // 处理单指手势
  private MultiTouchGestureDetector mMultiTouchGestureDetector; // 处理双指手势
  protected WsElement mSelectedElement; // 当前选中的 元素
  protected LinkedList<WsElement> mElementList = new LinkedList<>();
  protected Set<ElementActionListener> mElementActionListenerSet = new HashSet<>(); // 监听列表
  protected Set<ContainerWorker> mContainerWorkerSet = new TreeSet<>(); // 手势的功能列表
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
  
  private void init() {
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
    callContainerWorker(ContainerWorker::viewLayoutComplete);
  }
  
  // --------------------------------------- 手势操作开始 ---------------------------------------
  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    callContainerWorker(containerWorker -> containerWorker.dispatchTouchEvent(event));
    return super.dispatchTouchEvent(event);
  }
  
  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    return true;
  }
  
  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    Log.d(TAG, "initDoubleFingerDetector |||||||||| x0:" + event.getX() + ",y0:" + event.getY());
    if (mMultiTouchGestureDetector.onTouchEvent(event)) {
      Log.d(TAG, "onTouchEvent double finger event");
      return true;
    } else {
      Log.d(TAG, "onTouchEvent single finger event");
      return mDetector.onTouchEvent(event);
    }
  }
  
  @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
  private void addDetector() {
    mDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
      @Override
      public boolean onDown(MotionEvent event) {
        mMode = BaseActionMode.SELECTED_CLICK_OR_MOVE;
        WsElement clickedElement = getElementFromMotionEvent(event);
        
        Log.d(TAG, "singleFingerDown |||||||||| clickedElement:" + clickedElement + ",mSelectedElement:" + mSelectedElement);
        if (mSelectedElement != null) {
          if (WsElement.isSameElement(clickedElement, mSelectedElement)) {
            downTapSelectElementAction(event);
          } else {
            if (clickedElement == null) {
              downTapBlank(event);
            } else {
              downTapUnSelectElementAction(event, clickedElement);
            }
          }
        } else {
          if (clickedElement == null) {
            downTapBlank(event);
          } else {
            downTapUnSelectElementAction(event, clickedElement);
          }
        }
        return true;
      }
      
      @Override
      public void onShowPress(MotionEvent e) {
      }
      
      @Override
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling |||||||||| e1:" + e1 + ",e2:" + e2 + ",velocityX:" + velocityX + ",velocityY:" + velocityY);
        return ElementContainerView.this.onFling(e1, e2, velocityX, velocityY);
      }
      
      @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent event, float distanceX, float distanceY) {
        float[] distanceXY = new float[]{distanceX, distanceY};
        WsElement scrolledElement = getElementFromMotionEvent(event);
        if (mSelectedElement != null && WsElement.isSameElement(scrolledElement, mSelectedElement)) {
          scrollTapSelectElementAction(event, distanceXY);
        } else {
          scrollTapUnSelectElementAction(event, distanceXY);
        }
        Log.d(TAG, "singleFingerScroll |||||||||| " +
            "distanceX:" + distanceXY[0] + "distanceY:" + distanceXY[1] +
            ",event:" + event + ",scrolledElement:" + scrolledElement +
            ",selectedElement:" + mSelectedElement);
        return true;
      }
      
      @Override
      public void onLongPress(MotionEvent e) {
      
      }
      
      @Override
      public boolean onSingleTapUp(MotionEvent event) {
        WsElement clickedElement = getElementFromMotionEvent(event);
        Log.d(TAG, "onSingleTapUp |||||||||| clickedElement:" + clickedElement + ",mSelectedElement:" + mSelectedElement);
        if (mSelectedElement != null) {
          if (WsElement.isSameElement(clickedElement, mSelectedElement)) {
            upTapSelectElementAction(event);
          } else {
            if (clickedElement == null) {
              upTapBlank(event);
            } else {
              upTapUnSelectElementAction(event, clickedElement);
            }
          }
        } else {
          if (clickedElement == null) {
            upTapBlank(event);
          } else {
            upTapUnSelectElementAction(event, clickedElement);
          }
        }
        return true;
      }
    });
    
    mMultiTouchGestureDetector = new MultiTouchGestureDetector(getContext(), new MultiTouchGestureDetector.SimpleOnMultiTouchGestureListener() {
      @Override
      public void onScaleOrRotate(MultiTouchGestureDetector detector) {
        if (isDoubleFingerInSelectElement(detector.getMotionEvent())) {
          doubleFingerScaleAndRotateSelectElementAction(detector);
        } else {
          doubleFingerScaleAndRotateUnSelectElementAction(detector);
        }
      }
      
      @Override
      public void onMove(MultiTouchGestureDetector detector) {
      }
      
      @Override
      public boolean onBegin(MultiTouchGestureDetector detector) {
        if (isDoubleFingerInSelectElement(detector.getMotionEvent())) {
          doubleFingerInSelectElementStart(detector);
        } else {
          doubleFingerNotInSelectElementStart(detector);
        }
        return true;
      }
      
      @Override
      public void onEnd(MultiTouchGestureDetector detector) {
        if (isDoubleFingerInSelectElement(detector.getMotionEvent())) {
          doubleFingerInSelectElementEnd(detector);
        } else {
          doubleFingerNotInSelectElementEnd(detector);
        }
      }
    });
  }
  
  public boolean isDoubleFingerInSelectElement(MotionEvent event) {
    if (mSelectedElement == null || event == null || event.getPointerCount() <= 1) {
      return false;
    }
    final float x0 = event.getX(0);
    final float y0 = event.getY(0);
    final float x1 = event.getX(1);
    final float y1 = event.getY(1);
    return mSelectedElement.isInWholeDecoration(x0, y0) || mSelectedElement.isInWholeDecoration(x1, y1);
  }
  
  private WsElement getElementFromMotionEvent(MotionEvent event) {
    final float x = event.getX(), y = event.getY();
    return findElementByPosition(x, y);
  }
  // --------------------------------------- 手势操作结束 ---------------------------------------
  
  
  // --------------------------------------- 手势覆写开始 ---------------------------------------
  /**
   * 双指手势开始时落在选中元素中
   *
   * @param detector
   */
  protected void doubleFingerInSelectElementStart(MultiTouchGestureDetector detector) {
    callContainerWorker(containerWorker -> containerWorker.doubleFingerInSelectElementStart(detector));
  }
  
  /**
   * 双指手势开始时落在没有选中元素中的回调
   *
   * @param detector
   */
  protected void doubleFingerNotInSelectElementStart(MultiTouchGestureDetector detector) {
    callContainerWorker(containerWorker -> containerWorker.doubleFingerNotInSelectElementStart(detector));
  }
  
  /**
   * 双指旋转缩放事件落在了选中的 element 中
   */
  protected void doubleFingerScaleAndRotateSelectElementAction(MultiTouchGestureDetector detector) {
    if (doubleFingerScaleAndRotateSelectElementPreAction(detector)) {
      Log.d(TAG, "doubleFingerScaleAndRotateSelectElementAction doubleFingerScaleAndRotateSelectElementPreAction");
    } else {
      callContainerWorker(containerWorker -> containerWorker.doubleFingerScaleAndRotateSelectElementAction(detector));
      Log.d(TAG, "doubleFingerScaleAndRotateSelectElementAction");
    }
  }
  
  /**
   * 双指旋转缩放事件落在了选中的 element 中，优先调用的方法
   */
  protected boolean doubleFingerScaleAndRotateSelectElementPreAction(MultiTouchGestureDetector detector) {
    callContainerWorker(containerWorker -> containerWorker.doubleFingerScaleAndRotateSelectElementPreAction(detector));
    return false;
  }
  
  /**
   * 双指旋转缩放事件没有落在选中的 element 中
   */
  protected void doubleFingerScaleAndRotateUnSelectElementAction(MultiTouchGestureDetector detector) {
    callContainerWorker(containerWorker -> containerWorker.doubleFingerScaleAndRotateUnSelectElementAction(detector));
  }
  
  /**
   * 双指手势结束时落在选中元素中
   *
   * @param detector
   */
  protected void doubleFingerInSelectElementEnd(MultiTouchGestureDetector detector) {
    callContainerWorker(containerWorker -> containerWorker.doubleFingerInSelectElementEnd(detector));
  }
  
  /**
   * 双指手势结束时没有落在选中元素中
   *
   * @param detector
   */
  protected void doubleFingerNotInSelectElementEnd(MultiTouchGestureDetector detector) {
    callContainerWorker(containerWorker -> containerWorker.doubleFingerNotInSelectElementEnd(detector));
  }
  
  /**
   * 抛事件
   * @param e1
   * @param e2
   * @param velocityX
   * @param velocityY
   * @return
   */
  protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    callContainerWorker(containerWorker -> containerWorker.onFling(e1, e2, velocityX, velocityY));
    return true;
  }
  
  /**
   * down 事件落在了选中的 element 中
   *
   * @param event
   */
  protected void downTapSelectElementAction(@NonNull MotionEvent event) {
    if (downTapSelectElementPreAction(event)) {
      Log.d(TAG, "downTapSelectElementAction downTapSelectElementPreAction");
    } else {
      callContainerWorker(containerWorker -> containerWorker.downTapSelectElementAction(event));
      Log.d(TAG, "downTapSelectElementAction");
    }
  }
  
  /**
   * down 事件落在了选中的 element 中，优先调用的方法
   *
   * @return
   */
  protected boolean downTapSelectElementPreAction(@NonNull MotionEvent event) {
    Log.d(TAG, "downTapSelectElementPreAction |||||||||| event:" + event);
    callContainerWorker(containerWorker -> containerWorker.downTapSelectElementPreAction(event));
    return false;
  }
  
  /**
   * down 事件落在了空白的区域
   *
   * @param event
   */
  protected void downTapBlank(@NonNull MotionEvent event) {
    Log.d(TAG, "downTapBlank |||||||||| event:" + event);
    callContainerWorker(containerWorker -> containerWorker.downTapBlank(event));
  }
  
  /**
   * down 事件落在了某个没有选中的 element 中
   *
   * @param event
   * @param clickedElement
   */
  @CallSuper
  protected void downTapUnSelectElementAction(@NonNull MotionEvent event, WsElement clickedElement) {
    Log.d(TAG, "downTapUnSelectElementAction |||||||||| event:" + event + ",clickedElement:" + clickedElement);
    callContainerWorker(containerWorker -> containerWorker.downTapUnSelectElementAction(event, clickedElement));
  }
  
  /**
   * scroll 事件落在了选中的 element 中
   *
   * @param event      当前的触摸事件
   * @param distanceXY size 为 2，里面分别为 x 轴的 delta 位移，和 y 轴的 delta 位移
   * @return
   */
  protected void scrollTapSelectElementAction(@NonNull MotionEvent event, float[] distanceXY) {
    if (scrollTapSelectElementPreAction(event, distanceXY)) {
      Log.i(TAG, "scrollTapSelectElementAction scrollTapSelectElementPreAction");
    } else {
      callContainerWorker(containerWorker -> containerWorker.scrollTapSelectElementAction(event, distanceXY));
    }
  }
  
  /**
   * scroll 事件落在了选中的 element 中，优先调用的方法
   *
   * @param event      当前的触摸事件
   * @param distanceXY size 为 2，里面分别为 x 轴的 delta 位移，和 y 轴的 delta 位移，更改了其中的数据可以影响移动
   * @return
   */
  protected boolean scrollTapSelectElementPreAction(@NonNull MotionEvent event, float[] distanceXY) {
    Log.d(TAG, "scrollTapSelectElementPreAction |||||||||| event:" + event + ",distanceX:" + distanceXY[0] + ",distanceY:" + distanceXY[1]);
    callContainerWorker(containerWorker -> containerWorker.scrollTapSelectElementPreAction(event, distanceXY));
    return false;
  }
  
  /**
   * scroll 事件落在了选中的 element 之外
   *
   * @param event      当前的触摸事件
   * @param distanceXY size 为 2，里面分别为 x 轴的 delta 位移，和 y 轴的 delta 位移
   * @return
   */
  protected void scrollTapUnSelectElementAction(@NonNull MotionEvent event, float[] distanceXY) {
    callContainerWorker(containerWorker -> containerWorker.scrollTapOutOfSelectElementAction(event, distanceXY));
    Log.d(TAG, "scrollTapOutOfSelectElementAction |||||||||| event:" + event + ",distanceX:" + distanceXY[0] + ",distanceY:" + distanceXY[1]);
  }
  
  /**
   * up 事件落在了选中的 element 中
   *
   * @param event
   */
  protected void upTapSelectElementAction(@NonNull MotionEvent event) {
    if (upTapSelectElementPreAction(event)) {
      Log.d(TAG, "upTapSelectElementAction upTapSelectElementPreAction");
    } else {
      callContainerWorker(containerWorker -> containerWorker.upTapSelectElementAction(event));
      Log.d(TAG, "downTapSelectElementAction");
    }
  }
  
  /**
   * up 事件落在选中的 element 中，优先调用的方法
   *
   * @param event
   * @return
   */
  protected boolean upTapSelectElementPreAction(MotionEvent event) {
    callContainerWorker(containerWorker -> containerWorker.upTapSelectElementPreAction(event));
    Log.d(TAG, "upTapSelectElementPreAction |||||||||| event:" + event);
    return false;
  }
  
  /**
   * up 事件落在了空白的区域
   *
   * @param event
   */
  protected void upTapBlank(@NonNull MotionEvent event) {
    Log.d(TAG, "upTapBlank |||||||||| event:" + event);
    callContainerWorker(containerWorker -> containerWorker.upTapBlank(event));
  }
  
  /**
   * up 事件落在了某个没有选中的 element 中
   *
   * @param event
   * @param clickedElement
   */
  @CallSuper
  protected void upTapUnSelectElementAction(@NonNull MotionEvent event, WsElement clickedElement) {
    Log.d(TAG, "upTapUnSelectElementAction |||||||||| event:" + event + ",clickedElement:" + clickedElement);
    callContainerWorker(containerWorker -> containerWorker.upTapUnSelectElementAction(event, clickedElement));
  }
  // --------------------------------------- 手势覆写结束 ---------------------------------------
  
  
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
    callContainerWorker(containerWorker -> containerWorker.addElement(element));
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
    callContainerWorker(containerWorker -> containerWorker.deleteElement(element));
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
    callContainerWorker(containerWorker -> containerWorker.selectElement(element));
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
    callContainerWorker(ContainerWorker::unSelectElement);
    mSelectedElement.unSelect();
    mSelectedElement = null;
    return true;
  }
  // --------------------------------------- Element 操作结束 ---------------------------------------
  
  
  
  // --------------------------------------- getter setter 开始 ---------------------------------------
  public Vibrator getVibrator() {
    return mVibrator;
  }
  
  public List<WsElement> getElementList() {
    return mElementList;
  }
  
  public void setMode(BaseActionMode mode) {
    mMode = mode;
  }
  
  public BaseActionMode getMode() {
    return mMode;
  }
  
  @Nullable
  public WsElement getSelectElement() {
    return mSelectedElement;
  }
  // --------------------------------------- getter setter 结束 ---------------------------------------
  
  
  // --------------------------------------- 接口开始 ---------------------------------------
  public void callListener(Consumer<ElementActionListener> decorationActionListenerConsumer) {
    for (ElementActionListener elementActionListener : mElementActionListenerSet) {
      try {
        decorationActionListenerConsumer.accept(elementActionListener);
      } catch (Exception e) {
        Log.e(TAG, "callListener error!", e);
      }
    }
  }
  
  public void callContainerWorker(Consumer<ContainerWorker> containerWorkerConsumer) {
    for (ContainerWorker containerWorker : mContainerWorkerSet) {
      try {
        containerWorkerConsumer.accept(containerWorker);
      } catch (Exception e) {
        Log.e(TAG, "callContainerWorker error!", e);
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
    public void onSelect(WsElement element) {
      Log.d(TAG, "onSelect");
    }
    
    @Override
    public void onUnSelect(WsElement element) {
      Log.d(TAG, "onUnSelect");
    }
  }
  
  public interface Consumer<T> {
    void accept(T t);
  }
  
  public enum BaseActionMode {
    MOVE,
    SELECT,
    SELECTED_CLICK_OR_MOVE,
  }
  // --------------------------------------- 接口结束 ---------------------------------------
}
