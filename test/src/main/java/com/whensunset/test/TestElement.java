package com.whensunset.test;

import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.ImageView;

import com.whensunset.sticker.element.DecorationElement;

/**
 * Created by whensunset on 2019/3/31.
 */

public class TestElement extends DecorationElement {
  private ImageView mTestImageView;
  
  public TestElement(float originWidth, float originHeight) {
    super(originWidth, originHeight);
  }
  
  @Override
  protected View initView() {
    mTestImageView = new AppCompatImageView(mElementContainerView.getContext());
    mTestImageView.setImageResource(R.mipmap.ic_launcher);
    return mTestImageView;
  }
}
