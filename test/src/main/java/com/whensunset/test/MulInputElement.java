package com.whensunset.test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.whensunset.sticker.DecorationElement;


/**
 * Created by whensunset on 2019/3/31.
 */

public class MulInputElement extends DecorationElement {
  
  private ViewGroup mContainerView;
  
  public MulInputElement() {
    super();
    mIsRealUpdateShowingViewParams = true;
    mIsResponseSelectedClick = true;
  }
  
  @Override
  protected View initView() {
    mOriginWidth = 1000;
    mOriginHeight = 1000;
    LayoutInflater layoutInflater = LayoutInflater.from(mElementContainerView.getContext());
    mContainerView = (ViewGroup) layoutInflater.inflate(R.layout.mul_input_layout, null);
    return mContainerView;
  }
}
