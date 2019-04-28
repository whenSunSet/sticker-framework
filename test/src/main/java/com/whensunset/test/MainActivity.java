package com.whensunset.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.whensunset.sticker.Sticker;
import com.whensunset.sticker.container.RuleLineElementContainerView;
import com.whensunset.sticker.element.AnimationElement;

public class MainActivity extends Activity {
  private RuleLineElementContainerView mElementContainerView;
  
  @SuppressLint("ShowToast")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_main);
    Fresco.initialize(this);
    Sticker.initialize(this);
    mElementContainerView = findViewById(R.id.element_container_view);
    mElementContainerView.setNeedAutoUnSelect(false);
    
    
    Button addTestElement = findViewById(R.id.add_test_element);
    addTestElement.setOnClickListener(v -> {
      TestElement testElementDrawer = new TestElement(500, 500);
      mElementContainerView.unSelectElement();
      mElementContainerView.addSelectAndUpdateElement(testElementDrawer);
    });
    
    Button addStaticElement = findViewById(R.id.add_static_element);
    addStaticElement.setOnClickListener(v -> {
      StaticStickerElement staticStickerElement = StaticStickerElement.getStaticStickerElementByUri(
          500, 500, "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2335883016,884179302&fm=200&gp=0.jpg");
      mElementContainerView.unSelectElement();
      mElementContainerView.addSelectAndUpdateElement(staticStickerElement);
    });
    
    Button startAnimation = findViewById(R.id.start_animation);
    startAnimation.setOnClickListener(v -> {
      AnimationElement element = (AnimationElement) mElementContainerView.getSelectElement();
      AnimationElement.TransformParam to = new AnimationElement.TransformParam(element);
      to.mRotate = 0;
      to.mScale = 1;
      to.mMoveY += 500;
      to.mMoveX += 500;
      element.startElementAnimation(to);
    });
    
    Button restoreAnimation = findViewById(R.id.restore_animation);
    restoreAnimation.setOnClickListener(v -> {
      AnimationElement element = (AnimationElement) mElementContainerView.getSelectElement();
      element.restoreToBeforeAnimation();
    });
  }
}
