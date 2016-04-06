package com.medlinker.photopicker;

import android.app.Activity;

import com.heaven7.core.util.ViewHelper;

/**
 * Created by heaven7 on 2016/4/5.
 * @version 1.0
 */
public final class PhotoPickerFactory {

    private static IPhotoFileEntityFactory sPhotoFactory;
    private static ViewHelper.IImageLoader sImageLoader;

    /**
     *
     * @param <T>
     */
    public interface IPhotoFileEntityFactory<T extends IPhotoFileEntity>{
        T create(int id, String path);
    }

    /**
     * create an instance of PhotoPickerHelper
     * @param activity the activity
     */
    public static PhotoPickerHelper createPhotoPickerHelper(Activity activity){
        return new PhotoPickerHelper(activity);
    }

    public static <T extends IPhotoFileEntity> void setPhotoFileEntityFactory(IPhotoFileEntityFactory<T> factory){
        sPhotoFactory = factory;
    }
    public static void setImageLoader(ViewHelper.IImageLoader imageLoader){
        sImageLoader = imageLoader;
    }

    public static IPhotoFileEntityFactory getPhotoFileEntityFactory(){
        return sPhotoFactory;
    }
    public static ViewHelper.IImageLoader getImageLoader(){
        return  sImageLoader;
    }

}
