package com.medlinker.photopicker;

import com.heaven7.core.util.ViewHelper;

/**
 * Created by heaven7 on 2016/4/5.
 */
public class PhotoPickerFactory {

    private static IPhotoFileEntityFactory sPhotoFactory;
    private static ViewHelper.IImageLoader sImageLoader;

    public interface IPhotoFileEntityFactory<T extends IPhotoFileEntity>{
        T create(int id, String path);
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
    /*public*/ static ViewHelper.IImageLoader getsImageLoader(){
        return  sImageLoader;
    }

}
