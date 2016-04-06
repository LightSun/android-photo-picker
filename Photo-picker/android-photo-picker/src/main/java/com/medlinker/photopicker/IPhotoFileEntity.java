package com.medlinker.photopicker;

import com.heaven7.adapter.ISelectable;

/**
 * Created by heaven7 on 2016/4/5.
 */
public interface IPhotoFileEntity extends ISelectable{

      void setImageId(int id);
      int getImageId();

      void setPath(String path);
      String getPath();

}
