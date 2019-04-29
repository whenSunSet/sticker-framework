package com.whensunset.sticker.container.worker;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AbsoluteLayout;

import com.whensunset.sticker.container.ElementContainerView;
import com.whensunset.sticker.element.WsElement;
import com.whensunset.sticker.widget.RuleLineView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by whensunset on 2019/4/29.
 * 让 element 有规则限制的 worker
 */

public class RegularizedElementWorker extends DefaultContainerWorker {
  private static final String TAG = "heshixi:REW";
  private static final float IS_CHECK_X_RULE_THRESHOLD = 8; // 在 x 轴移动元素 deltaX 低于阈值的时候进行移动规则监测
  private static final float IS_CHECK_Y_RULE_THRESHOLD = 8; // 在 y 轴移动元素 deltaY 低于阈值的时候进行移动规则监测
  private static final float CHECK_X_IS_IN_RULE_THRESHOLD = 2; // 检测元素的 x 是否在规则中的阈值
  private static final float CHECK_Y_IS_IN_RULE_THRESHOLD = 2; // 检测元素的 y 是否在规则中的阈值
  private static final float X_RULE_TOTAL_ABSORPTION_MAX = 60; // 某次 x 轴移动规则累计吸收的 x 轴移动距离的最大值
  private static final float Y_RULE_TOTAL_ABSORPTION_MAX = 60; // 某次 y 轴移动规则累计吸收的 y 轴移动距离的最大值
  protected static final long VIBRATOR_DURATION_IN_RULE = 10; // 进入规则时的震动的时长
  protected static final float NOT_IN_RULE = -1; // 当前元素不处于任何规则中
  private static final float[] X_RULES = new float[]{0.05f, 0.5f, 0.95f}; // x 轴上的规则监测点，单位为 view 的百分比
  private static final float[] Y_RULES = new float[]{0.10f, 0.5f, 0.90f}; // y 轴上的规则监测点，单位为 view 的百分比
  
  private RuleLineView mRuleLineView;
  RuleLineView.RuleLine[] mRuleLines = new RuleLineView.RuleLine[2];
  private float mXRuleTotalAbsorption = 0; // 某次 x 轴移动规则累计吸收的 x 轴移动距离
  private float mYRuleTotalAbsorption = 0; // 某次 y 轴移动规则累计吸收的 y 轴移动距离
  protected List<RectF> mNoRuleRectList = new ArrayList<>();// 不在规则范围内的 Rect 列表
  
  @Override
  public void viewLayoutComplete() {
    super.viewLayoutComplete();
    mRuleLineView = initRuleView();
    mElementContainerView.addView(mRuleLineView);
    mRuleLineView.setVisibility(GONE);
  }
  
  public RegularizedElementWorker(ElementContainerView elementContainerView) {
    super(elementContainerView);
  }
  
  /**
   * 初始化 RuleLineView 子类可以实现自己的样式
   *
   * @return
   **/
  @NonNull
  protected RuleLineView initRuleView() {
    RuleLineView ruleLineView = new RuleLineView(mElementContainerView.getContext());
    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(mElementContainerView.getWidth(), mElementContainerView.getHeight(), 0, 0);
    ruleLineView.setLayoutParams(layoutParams);
    ruleLineView.setRuleLines(mRuleLines);
    return ruleLineView;
  }
  
  @Override
  public void scrollTapSelectElementPreAction(@NonNull MotionEvent event, float[] distanceXY) {
    if (mElementContainerView.getSelectElement() == null) {
      Log.w(TAG, "scrollTapSelectElementPreAction mSelectedElement is null");
      return;
    }
    
    boolean xCanCheckRule = (Math.abs(distanceXY[0]) <= IS_CHECK_X_RULE_THRESHOLD);
    boolean yCanCheckRule = (Math.abs(distanceXY[1]) <= IS_CHECK_Y_RULE_THRESHOLD);
    boolean xInRule = false;
    boolean yInRule = false;
    float containerViewWidth = mElementContainerView.getWidth();
    float containerViewHeight = mElementContainerView.getHeight();
    
    float xRulePercent = 0;
    if (xCanCheckRule) {
      xRulePercent = checkElementInXRule(event, distanceXY);
      xInRule = (xRulePercent != NOT_IN_RULE);
      if (xInRule) {
        RuleLineView.RuleLine xRuleLine = new RuleLineView.RuleLine();
        xRuleLine.mStartPoint = new PointF(xRulePercent * containerViewWidth, 0);
        xRuleLine.mEndPoint = new PointF(xRulePercent * containerViewWidth, containerViewHeight);
        mRuleLines[0] = xRuleLine;
        
        if (mXRuleTotalAbsorption == 0 && distanceXY[0] != 0) {
          mElementContainerView.getVibrator().vibrate(VIBRATOR_DURATION_IN_RULE);
          Log.d(TAG, "scrollTapSelectElementPreAction x vibrate");
        }
        mXRuleTotalAbsorption += distanceXY[0];
        if (Math.abs(mXRuleTotalAbsorption) >= X_RULE_TOTAL_ABSORPTION_MAX) {
          Log.d(TAG, "scrollTapSelectElementPreAction clear mXRuleTotalAbsorption:" + mXRuleTotalAbsorption);
          mXRuleTotalAbsorption = 0;
          distanceXY[0] += (distanceXY[0] < 0 ? -2 * CHECK_X_IS_IN_RULE_THRESHOLD : 2 * CHECK_X_IS_IN_RULE_THRESHOLD);
        } else {
          distanceXY[0] = 0;
          Log.d(TAG, "scrollTapSelectElementPreAction add mXRuleTotalAbsorption |||||||||| mXRuleTotalAbsorption:" + mXRuleTotalAbsorption);
        }
      } else {
        mRuleLines[0] = null;
      }
    } else {
      mRuleLines[0] = null;
      mXRuleTotalAbsorption = 0;
    }
    
    float yRulePercent = 0;
    if (yCanCheckRule) {
      yRulePercent = checkElementInYRule(event, distanceXY);
      yInRule = (yRulePercent != NOT_IN_RULE);
      if (yInRule) {
        RuleLineView.RuleLine yRuleLine = new RuleLineView.RuleLine();
        yRuleLine.mStartPoint = new PointF(0, yRulePercent * containerViewHeight);
        yRuleLine.mEndPoint = new PointF(containerViewWidth, yRulePercent * containerViewHeight);
        mRuleLines[1] = yRuleLine;
        
        if (mYRuleTotalAbsorption == 0 && distanceXY[1] != 0) {
          mElementContainerView.getVibrator().vibrate(VIBRATOR_DURATION_IN_RULE);
          Log.d(TAG, "scrollTapSelectElementPreAction y vibrate");
        }
        mYRuleTotalAbsorption += distanceXY[1];
        if (Math.abs(mYRuleTotalAbsorption) >= Y_RULE_TOTAL_ABSORPTION_MAX) {
          Log.d(TAG, "scrollTapSelectElementPreAction clear mYRuleTotalAbsorption:" + mYRuleTotalAbsorption);
          mYRuleTotalAbsorption = 0;
          distanceXY[1] += (distanceXY[1] < 0 ? -2 * CHECK_Y_IS_IN_RULE_THRESHOLD : 2 * CHECK_Y_IS_IN_RULE_THRESHOLD);
        } else {
          distanceXY[1] = 0;
          Log.d(TAG, "scrollTapSelectElementPreAction add mYRuleTotalAbsorption |||||||||| mYRuleTotalAbsorption:" + mYRuleTotalAbsorption);
        }
      } else {
        mRuleLines[1] = null;
      }
    } else {
      mRuleLines[1] = null;
      mYRuleTotalAbsorption = 0;
    }
    
    if ((xCanCheckRule && xInRule) || (yCanCheckRule && yInRule)) {
      mRuleLineView.setVisibility(VISIBLE);
      mRuleLineView.invalidate();
    } else {
      mRuleLineView.setVisibility(GONE);
    }
  }
  
  @Override
  public void upTapSelectElementPreAction(@NonNull MotionEvent event) {
    mRuleLineView.setVisibility(GONE);
  }
  
  /**
   * 检查当前元素是否处于哪条规则中view 中心的时候可以进行提醒
   *
   * @return 返回当前元素处于哪条规则中
   */
  protected float checkElementInXRule(@NonNull MotionEvent event, float[] distance) {
    WsElement selectedElement = mElementContainerView.getSelectElement();
    float containerViewWidth = mElementContainerView.getWidth();
    if (selectedElement == null) {
      Log.w(TAG, "checkElementInXRule selectedElement is null");
      return NOT_IN_RULE;
    }
    
    if (!selectedElement.isSingerFingerMove()) {
      return NOT_IN_RULE;
    }
    
    float elementCenterX = selectedElement.getContentRect().centerX();
    float elementCenterY = selectedElement.getContentRect().centerY();
    for (int i = 0; i < mNoRuleRectList.size(); i++) {
      if (mNoRuleRectList.get(i).contains(elementCenterX, elementCenterY)) {
        return NOT_IN_RULE;
      }
    }
    
    float viewCenterX = containerViewWidth * X_RULES[1];
    if (Math.abs(viewCenterX - elementCenterX) < CHECK_X_IS_IN_RULE_THRESHOLD) {
      return X_RULES[1];
    }
    
    float elementLeft = selectedElement.getContentRect().left;
    float viewLeft = containerViewWidth * X_RULES[0];
    if (Math.abs(viewLeft - elementLeft) < CHECK_X_IS_IN_RULE_THRESHOLD) {
      return X_RULES[0];
    }
    
    float elementRight = selectedElement.getContentRect().right;
    float viewRight = containerViewWidth * X_RULES[2];
    if (Math.abs(viewRight - elementRight) < CHECK_X_IS_IN_RULE_THRESHOLD) {
      return X_RULES[2];
    }
    
    return NOT_IN_RULE;
  }
  
  /**
   * 同 checkElementInXRule
   */
  protected float checkElementInYRule(@NonNull MotionEvent event, float[] distance) {
    WsElement selectedElement = mElementContainerView.getSelectElement();
    float containerViewHeight = mElementContainerView.getHeight();
    if (selectedElement == null) {
      Log.w(TAG, "checkElementInYRule selectedElement is null");
      return NOT_IN_RULE;
    }
    
    if (!selectedElement.isSingerFingerMove()) {
      return NOT_IN_RULE;
    }
    
    float elementCenterX = selectedElement.getContentRect().centerX();
    float elementCenterY = selectedElement.getContentRect().centerY();
    for (int i = 0; i < mNoRuleRectList.size(); i++) {
      if (mNoRuleRectList.get(i).contains(elementCenterX, elementCenterY)) {
        return NOT_IN_RULE;
      }
    }
    
    float viewCenterY = containerViewHeight * Y_RULES[1];
    if (Math.abs(viewCenterY - elementCenterY) < CHECK_Y_IS_IN_RULE_THRESHOLD) {
      return Y_RULES[1];
    }
    
    float elementTop = selectedElement.getContentRect().top;
    float viewTop = containerViewHeight * Y_RULES[0];
    if (Math.abs(viewTop - elementTop) < CHECK_Y_IS_IN_RULE_THRESHOLD) {
      return Y_RULES[0];
    }
    
    float elementBottom = selectedElement.getContentRect().bottom;
    float viewBottom = containerViewHeight * Y_RULES[2];
    if (Math.abs(viewBottom - elementBottom) < CHECK_Y_IS_IN_RULE_THRESHOLD) {
      return Y_RULES[2];
    }
    
    return NOT_IN_RULE;
  }
  
  @Override
  public int getPriority() {
    return 0;
  }
}
