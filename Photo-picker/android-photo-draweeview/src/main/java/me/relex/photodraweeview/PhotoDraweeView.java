package me.relex.photodraweeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

public class PhotoDraweeView extends SimpleDraweeView implements IAttacher {

    private Attacher mAttacher;

    public PhotoDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public PhotoDraweeView(Context context) {
        super(context);
        init();
    }

    public PhotoDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        if (mAttacher == null || mAttacher.getDraweeView() == null) {
            mAttacher = new Attacher(this);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    @Override protected void onDraw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        canvas.concat(mAttacher.getDrawMatrix());
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override protected void onAttachedToWindow() {
        init();
        super.onAttachedToWindow();
    }

    @Override protected void onDetachedFromWindow() {
        mAttacher.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override public float getMinimumScale() {
        return mAttacher.getMinimumScale();
    }

    @Override public float getMediumScale() {
        return mAttacher.getMediumScale();
    }

    @Override public float getMaximumScale() {
        return mAttacher.getMaximumScale();
    }

    @Override public void setMinimumScale(float minimumScale) {
        mAttacher.setMinimumScale(minimumScale);
    }

    @Override public void setMediumScale(float mediumScale) {
        mAttacher.setMediumScale(mediumScale);
    }

    @Override public void setMaximumScale(float maximumScale) {
        mAttacher.setMaximumScale(maximumScale);
    }

    @Override public float getScale() {
        return mAttacher.getScale();
    }

    @Override public void setScale(float scale) {
        mAttacher.setScale(scale);
    }

    @Override public void setScale(float scale, boolean animate) {
        mAttacher.setScale(scale, animate);
    }

    @Override public void setScale(float scale, float focalX, float focalY, boolean animate) {
        mAttacher.setScale(scale, focalX, focalY, animate);
    }

    @Override public void setZoomTransitionDuration(long duration) {
        mAttacher.setZoomTransitionDuration(duration);
    }

    @Override public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacher.setAllowParentInterceptOnEdge(allow);
    }

    @Override public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener listener) {
        mAttacher.setOnDoubleTapListener(listener);
    }

    @Override public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        mAttacher.setOnScaleChangeListener(listener);
    }

    @Override public void setOnLongClickListener(OnLongClickListener listener) {
        mAttacher.setOnLongClickListener(listener);
    }

    @Override public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mAttacher.setOnPhotoTapListener(listener);
    }

    @Override public void setOnViewTapListener(OnViewTapListener listener) {
        mAttacher.setOnViewTapListener(listener);
    }

    @Override public OnPhotoTapListener getOnPhotoTapListener() {
        return mAttacher.getOnPhotoTapListener();
    }

    @Override public OnViewTapListener getOnViewTapListener() {
        return mAttacher.getOnViewTapListener();
    }

    @Override public void update(int imageInfoWidth, int imageInfoHeight) {
        mAttacher.update(imageInfoWidth, imageInfoHeight);
    }

    public void loadLocalImage(String file,int width, int height) {
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.fromFile(new File(file)))
                .setLocalThumbnailPreviewsEnabled(true)
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .setProgressiveRenderingEnabled(false)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(this.getController())
                .build();
        this.setController(controller);
    }

    public void loadImage(Uri uri, int placeHolder, final BaseControllerListener<ImageInfo> l){
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                      /*  if (imageInfo == null) {
                            return;
                        }
                        PhotoDraweeView.this.update(imageInfo.getWidth(), imageInfo.getHeight());*/
                        if (l != null) {
                            l.onFinalImageSet(id, imageInfo, animatable);
                        }
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        if (l != null) {
                            l.onFailure(id, throwable);
                        }
                    }

                    @Override //渐进式
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                        if (l != null) {
                            l.onIntermediateImageSet(id, imageInfo);
                        }
                    }
                })
                .setUri(uri)
                .build();
        getHierarchy().setPlaceholderImage(placeHolder);
        setController(controller);
    }

    public void loadImage(String url){
        Uri uri = url.startsWith("http://") || url.startsWith("https://") ? Uri.parse(url) : Uri.fromFile(new File(url));
        DraweeController controller = Fresco.newDraweeControllerBuilder().setUri(uri).build();
        setController(controller);
    }

    public void loadImage(String url,String lowurl, int placeHolderResId, int errorResId,ScalingUtils.ScaleType scaleType){
        final boolean isHttp = url.startsWith("http");
        Uri uri = isHttp ? Uri.parse(url) :  Uri.fromFile(new File(url));
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder()
             .setImageRequest(ImageRequest.fromUri(uri));
        if(lowurl!=null &&  lowurl.startsWith("http")) {
            controller .setLowResImageRequest(ImageRequest.fromUri(lowurl));
        }
        getHierarchy().setActualImageScaleType(scaleType);
                        // controller.setUri(URI);
        getHierarchy().setPlaceholderImage(getResources().getDrawable( placeHolderResId ),scaleType);
        getHierarchy().setFailureImage(getResources().getDrawable(errorResId),scaleType);
        controller.setOldController(this.getController())
             .setControllerListener(new BaseControllerListener<ImageInfo>() {
                 @Override
                 public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                     if (imageInfo == null) {
                         return;
                     }
                     PhotoDraweeView.this.update(imageInfo.getWidth(), imageInfo.getHeight());
                 }
             });
        this.setController(controller.build());
    }
}
