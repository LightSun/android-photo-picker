package me.relex.photodraweeview;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

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

/**
 * a common class of fresco load image.from local or network
 * Created by heaven7 on 2016/4/8.
 */
public class CommonDraweeView extends SimpleDraweeView{

    public CommonDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public CommonDraweeView(Context context) {
        super(context);
    }

    public CommonDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommonDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CommonDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void loadLocalImage(String file){
        loadLocalImage(file,getWidth(),getHeight());
    }

    /**
     * load image from local
     * @param file image file
     */
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

    /**
     * @see  #loadImage(String, String, int, int, ScalingUtils.ScaleType, int, int, BaseControllerListener)
     */
    public void loadImage(String url, int placeHolder, int errorResId){
        loadImage(url, placeHolder, errorResId, ScalingUtils.ScaleType.CENTER_CROP ,null);
    }

    /**
     * @see  #loadImage(String, String, int, int, ScalingUtils.ScaleType, int, int, BaseControllerListener)
     */
    public void loadImage(String url, int placeHolder, int errorResId,
                          ScalingUtils.ScaleType scaleType, final BaseControllerListener<ImageInfo> l){
        loadImage(url,null, placeHolder, errorResId, scaleType,getWidth() , getHeight(),l);
    }
    /**
     * @see  #loadImage(String, String, int, int, ScalingUtils.ScaleType, int, int, BaseControllerListener)
     */
    public void loadImage(String url, String lowUrl,int placeHolder, int errorResId,
                          ScalingUtils.ScaleType scaleType,
                          final BaseControllerListener<ImageInfo> l){
        loadImage(url, lowUrl, placeHolder, errorResId, scaleType,getWidth() , getHeight(), l);
    }


    /***
     * load net work image.
     * @param url the main url of image, from net
     * @param lowUrl the low url, from net or local image file name, can be null.
     * @param placeHolder the place holder. 0 with no place holder
     * @param errorResId error res id , 0 with no error res.
     * @param width the width you want
     * @param height the height you want
     * @param scaleType scale type
     * @param l BaseControllerListener,can be null
     */
    public void loadImage(String url, String lowUrl,int placeHolder, int errorResId,
                          ScalingUtils.ScaleType scaleType, int width , int height ,
                          final BaseControllerListener<ImageInfo> l){
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                //.setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        getHierarchy().setActualImageScaleType(scaleType);
        if(placeHolder!=0) {
            getHierarchy().setPlaceholderImage(getResources().getDrawable( placeHolder ),scaleType);
        }
        if(errorResId!=0){
            getHierarchy().setFailureImage(getResources().getDrawable( errorResId ),scaleType);
        }
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(l)
                .setImageRequest(request)
                .setOldController(getController());
        if(lowUrl!=null){
            Uri lowUri = lowUrl.startsWith("http") ? Uri.parse(lowUrl) : Uri.fromFile(new File(lowUrl));
            controller.setLowResImageRequest(ImageRequestBuilder.newBuilderWithSource(lowUri)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build() );
        }
        setController(controller.build());
    }
}
