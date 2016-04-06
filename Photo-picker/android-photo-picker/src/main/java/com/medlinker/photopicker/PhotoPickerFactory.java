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

    private static final IPhotoFileEntityFactory<BasePhotoFileEntity> sDefaultFacory
             = new IPhotoFileEntityFactory<BasePhotoFileEntity>() {
        @Override
        public BasePhotoFileEntity create(int id, String path) {
            return new BasePhotoFileEntity(id,path);
        }
    };

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

    /***
     * set the photo entity factory
     * @param factory the factory
     */
    public static <T extends IPhotoFileEntity> void setPhotoFileEntityFactory(IPhotoFileEntityFactory<T> factory){
        sPhotoFactory = factory;
    }

    /**
     * set the image loader to load image .this will use in {@link PhotoGridAdapter}
     * you can cross this use frecro image library or glide or other image library to load.
     * this  is useful.
     * @param imageLoader the image loader
     */
    public static void setImageLoader(ViewHelper.IImageLoader imageLoader){
        sImageLoader = imageLoader;
    }

    public static IPhotoFileEntityFactory getPhotoFileEntityFactory(){
        return sPhotoFactory!=null ? sPhotoFactory : sDefaultFacory;
    }
    public static ViewHelper.IImageLoader getImageLoader(){
        return  sImageLoader;
    }

}
