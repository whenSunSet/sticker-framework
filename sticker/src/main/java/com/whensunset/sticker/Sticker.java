package com.whensunset.sticker;

import android.content.Context;

/**
 * Created by whensunset on 2019/4/15.
 * sticker framework 的工具类
 */

public final class Sticker {
  
  /**
   * sticker framework 的初始化入口，需要在使用前调用一次
   * @param context
   */
  public static void initialize(Context context) {
    DecorationView.initDecorationView(context.getResources(), context);
  }
}
