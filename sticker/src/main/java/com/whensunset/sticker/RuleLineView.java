package com.whensunset.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by whensunset on 2019/4/14.
 * 规制线条 view
 */

public class RuleLineView extends View {
  private static final String TAG = "heshixi:RuleLineView";
  private static final float LINE_WIDTH = 6; // 线的宽度，单位为 px
  private static Paint sLinePaint = new Paint();
  static {
    sLinePaint.setColor(0XFF33B5E5);
    sLinePaint.setStyle(Paint.Style.STROKE);
    sLinePaint.setAntiAlias(true);
    sLinePaint.setStrokeWidth(LINE_WIDTH);
  }
  
  protected RuleLine[] mRuleLines;
  
  public RuleLineView(Context context) {
    super(context);
  }
  
  public RuleLineView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }
  
  public RuleLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
  
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public RuleLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }
  
  public void setRuleLines(RuleLine[] ruleLines) {
    mRuleLines = ruleLines;
  }
  
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mRuleLines == null || mRuleLines.length <= 0) {
      Log.w(TAG, "onDraw no rule line");
      return;
    }
  
    for (int i = 0; i < mRuleLines.length; i++) {
      if (mRuleLines[i] == null || mRuleLines[i].mStartPoint == null || mRuleLines[i].mEndPoint == null) {
        Log.w(TAG, "onDraw start or end point is null");
        continue;
      }
      canvas.drawLine(
          mRuleLines[i].mStartPoint.x,
          mRuleLines[i].mStartPoint.y,
          mRuleLines[i].mEndPoint.x,
          mRuleLines[i].mEndPoint.y,
          sLinePaint);
    }
  }
  
  public static class RuleLine {
    public PointF mStartPoint;
    public PointF mEndPoint;
    
    @Override
    public String toString() {
      return "RuleLine{" +
          "mStartPoint=" + mStartPoint +
          ", mEndPoint=" + mEndPoint +
          '}';
    }
  }
}
