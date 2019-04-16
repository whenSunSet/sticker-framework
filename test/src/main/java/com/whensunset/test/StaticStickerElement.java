package com.whensunset.test;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.whensunset.sticker.DecorationElement;

import java.io.File;

/**
 * Created by whensunset on 2019/4/14.
 * 静态贴纸元素
 */

public class StaticStickerElement extends DecorationElement {
  private static final String TAG = "heshixi:SSElement";
  
  private SimpleDraweeView mStaticDreweeView;
  private final ImageRequest mImageRequest;
  
  @Nullable
  public static final StaticStickerElement getStaticStickerElementByLocalFilePath(float originWidth, float originHeight, String localFilePath) {
    if (TextUtils.isEmpty(localFilePath) || !new File(localFilePath).exists()) {
      Log.e(TAG, "getStaticStickerElementByLocalFilePath localFilePath invalid");
      return null;
    }
    ImageRequest imageRequest = ImageRequest.fromFile(new File(localFilePath));
    return new StaticStickerElement(originWidth, originHeight, imageRequest);
  }
  
  @Nullable
  public static StaticStickerElement getStaticStickerElementByUri(float originWidth, float originHeight, String uriString) {
    if (TextUtils.isEmpty(uriString)) {
      Log.e(TAG, "getStaticStickerElementByLocalFilePath uriString is empty");
      return null;
    }
    Uri uri = Uri.parse(uriString);
    ImageRequest imageRequest = ImageRequest.fromUri(uri);
    return new StaticStickerElement(originWidth, originHeight, imageRequest);
  }
  
  private StaticStickerElement(float originWidth, float originHeight, ImageRequest imageRequest) {
    super(originWidth, originHeight);
    mImageRequest = imageRequest;
  }
  
  @Override
  protected View initView() {
    mStaticDreweeView = new SimpleDraweeView(mElementContainerView.getContext());
    mStaticDreweeView.setImageRequest(mImageRequest);
    return mStaticDreweeView;
  }
}
