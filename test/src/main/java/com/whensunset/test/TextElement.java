package com.whensunset.test;

import android.view.View;
import android.widget.EditText;

import com.whensunset.sticker.element.DecorationElement;

/**
 * Created by whensunset on 2019/3/31.
 */

public class TextElement extends DecorationElement {
  
  private EditText mEditText;
  
  public TextElement(float originWidth, float originHeight) {
    super(originWidth, originHeight);
  }
  
  @Override
  protected View initView() {
    mIsResponseSelectedClick = true;
    mIsRealUpdateShowingViewParams = true;
    mEditText = new EditText(mElementContainerView.getContext());
    mEditText.setTextSize(15);
    return mEditText;
  }
  
  @Override
  public void update() {
    super.update();
    mEditText.setTextSize(15 * mScale);
  }
}
