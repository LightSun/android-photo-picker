package com.medlinker.photopicker;

import android.os.Parcelable;

import com.heaven7.adapter.ISelectable;

import java.io.Serializable;

/**
 * Created by heaven7 on 2016/4/5.
 */
public interface IPhotoFileEntity extends ISelectable, Parcelable, Serializable{

      void setImageId(int id);
      int getImageId();

      void setPath(String path);
      String getPath();

}
