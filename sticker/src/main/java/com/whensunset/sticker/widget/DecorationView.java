package com.whensunset.sticker.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import com.whensunset.sticker.R;
import com.whensunset.sticker.element.DecorationElement;

import static com.whensunset.sticker.element.DecorationElement.ELEMENT_REMOVE_ICON_WIDTH;
import static com.whensunset.sticker.element.DecorationElement.ELEMENT_SCALE_ROTATE_ICON_WIDTH;


/**
 * Created by whensunset on 2019/3/19.
 * 绘制四周的边框和装饰的 view
 */

public class DecorationView extends View {
  private static final int DECORATION_OUT_BOX_LINE_WIDTH = 2;
  
  private static Bitmap sRemoveButtonBitmap; // 文字/贴纸移除按钮
  private static Bitmap sScaleAndRotateButtonBitmap; // 文字/贴纸旋转缩放按钮
  private static Paint sLinePaint = new Paint();
  private static boolean sIsInit = false;
  
  public static void initDecorationView(Resources resources, Context context) {
    if (resources == null || context == null || sIsInit) {
      return;
    }
    // 删除按钮
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.outHeight = ELEMENT_SCALE_ROTATE_ICON_WIDTH;
    options.outWidth = ELEMENT_SCALE_ROTATE_ICON_WIDTH;
    sRemoveButtonBitmap = BitmapFactory.decodeResource(
        resources, R.drawable.default_decoration_delete, options);
    
    // 旋转缩放按钮
    options.outHeight = ELEMENT_REMOVE_ICON_WIDTH;
    options.outWidth = ELEMENT_REMOVE_ICON_WIDTH;
    sScaleAndRotateButtonBitmap = BitmapFactory.decodeResource(
        resources, R.drawable.default_decoration_scale, options);
    
    sLinePaint.setColor(0XFFFFFFFF);
    sLinePaint.setStyle(Paint.Style.STROKE);
    sLinePaint.setAntiAlias(true);
    sLinePaint.setStrokeWidth(DECORATION_OUT_BOX_LINE_WIDTH);
    sIsInit = true;
  }
  
  protected DecorationElement mDecorationElement;
  
  public DecorationView(Context context) {
    super(context);
    if (!sIsInit) {
      throw new RuntimeException("need call initDecorationView");
    }
  }
  
  public DecorationView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    if (!sIsInit) {
      throw new RuntimeException("need call initDecorationView");
    }
  }
  
  public DecorationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    if (!sIsInit) {
      throw new RuntimeException("need call initDecorationView");
    }
  }
  
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public DecorationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                        int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    if (!sIsInit) {
      throw new RuntimeException("need call initDecorationView");
    }
  }
  
  public void setDecorationElement(DecorationElement decorationElement) {
    mDecorationElement = decorationElement;
  }
  
  @SuppressLint("DrawAllocation")
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    if (mDecorationElement == null) {
      return;
    }
    
    canvas.save();
    
    // 绘制内容外面的框，只有处于选中态的时候才绘制
    Rect outBoxRect = new Rect(
        ELEMENT_REMOVE_ICON_WIDTH / 2,
        ELEMENT_REMOVE_ICON_WIDTH / 2,
        getWidth() - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
        getHeight() - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2);
    canvas.drawRect(outBoxRect, sLinePaint);
    canvas.setDrawFilter(
        new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
    
    if (sRemoveButtonBitmap != null) {
      // 绘制删除按钮
      canvas.drawBitmap(sRemoveButtonBitmap,
          new Rect(0, 0, sRemoveButtonBitmap.getWidth(),
              sRemoveButtonBitmap.getHeight()),
          new Rect(0, 0, ELEMENT_REMOVE_ICON_WIDTH, ELEMENT_REMOVE_ICON_WIDTH), sLinePaint);
    }
    
    if (sScaleAndRotateButtonBitmap != null) {
      // 绘制旋转缩放按钮
      canvas.drawBitmap(sScaleAndRotateButtonBitmap,
          new Rect(0, 0, sScaleAndRotateButtonBitmap.getWidth(),
              sScaleAndRotateButtonBitmap.getHeight()),
          new Rect(getWidth() - ELEMENT_SCALE_ROTATE_ICON_WIDTH, getHeight() - ELEMENT_SCALE_ROTATE_ICON_WIDTH, getWidth(), getHeight()), sLinePaint);
      
    }
    
    canvas.restore();
  }
}
