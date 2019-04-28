package com.whensunset.sticker;

import android.content.Context;

import com.whensunset.sticker.container.worker.ContainerWorker;
import com.whensunset.sticker.container.worker.DoubleFingerScaleAndRotateElementWorker;
import com.whensunset.sticker.container.worker.SelectElementWorker;
import com.whensunset.sticker.container.worker.SelectedClickElementWorker;
import com.whensunset.sticker.container.worker.SingleFingerMoveElementWorker;
import com.whensunset.sticker.container.worker.TapBlackUnSelectElementWorker;
import com.whensunset.sticker.widget.DecorationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by whensunset on 2019/4/15.
 * sticker framework 的工具类
 */

public final class Sticker {
  private static final String TAG = "Sticker";
  private static final List<Class<? extends ContainerWorker>> DEFAULT_CONTAINER_WORKER_CLASS_LIST = new ArrayList<>();
  
  static {
    DEFAULT_CONTAINER_WORKER_CLASS_LIST.add(SelectElementWorker.class);
    DEFAULT_CONTAINER_WORKER_CLASS_LIST.add(SingleFingerMoveElementWorker.class);
    DEFAULT_CONTAINER_WORKER_CLASS_LIST.add(TapBlackUnSelectElementWorker.class);
    DEFAULT_CONTAINER_WORKER_CLASS_LIST.add(SelectedClickElementWorker.class);
    DEFAULT_CONTAINER_WORKER_CLASS_LIST.add(DoubleFingerScaleAndRotateElementWorker.class);
  }
  
  /**
   * sticker framework 的初始化入口，需要在使用前调用一次
   *
   * @param context
   */
  public static void initialize(Context context) {
    DecorationView.initDecorationView(context.getResources(), context);
  }
  
  /**
   * 每个 Worker 在创建之后都需要在这里注册一下，否则无法使用
   *
   * @param clazz
   */
  public static void registerContainerWorker(Class<? extends ContainerWorker> clazz) {
    DEFAULT_CONTAINER_WORKER_CLASS_LIST.add(clazz);
  }
}
