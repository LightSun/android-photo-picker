package com.medlinker.photopicker;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.heaven7.adapter.AdapterManager;
import com.heaven7.adapter.HeaderFooterHelper;
import com.heaven7.adapter.ISelectable;
import com.heaven7.adapter.QuickRecycleViewAdapter;
import com.heaven7.core.util.ViewHelper;

import java.util.List;

/**
 * photo grid adapter . help you fast setAdapter. but you must declare
 * {@link com.medlinker.photopicker.R.id#photo_picker_iv_image} as the photo ImageView's(or its child class) id
 * and {@link com.medlinker.photopicker.R.id#photo_picker_iv_select_icon} as the select icon
 * ImageView's id in item layout. camera item is optional.
 * <p></p>
 * Created by heaven7 on 2016/4/5.
 * @version 1.0
 */
public abstract class PhotoGridAdapter<T extends IPhotoFileEntity> extends QuickRecycleViewAdapter<T>{

   // private static final String TAG = "PhotoGridAdapter";

    private static final int VIEW_TYPE_CAMERA = -1;
    private static final int DEFAULT_COLOR_GRAY = Color.parseColor("#d8d8d8");
    private boolean mShowCamera = true ;
    private ICallback<T> mCallback;

    /**
     * default select mode {@link ISelectable#SELECT_MODE_SINGLE}
     * @param layoutId  item layout id
     * @param mDatas the data of the grid
     */
    public PhotoGridAdapter(int layoutId, List<T> mDatas) {
        super(layoutId, mDatas, ISelectable.SELECT_MODE_SINGLE);
    }

    /**
     * @param layoutId  item layout id
     * @param mDatas the data of the grid
     * @param selectMode the select mode ,see {@link ISelectable#SELECT_MODE_MULTI}
     *                   or  {@link ISelectable#SELECT_MODE_SINGLE}
     */
    public PhotoGridAdapter(int layoutId, List<T> mDatas, int selectMode) {
        super(layoutId , mDatas, selectMode);
        if(isShowCamera()){
            getAdapterManager().addItem(mDatas.get(0));//add a place holder for show camera
        }
    }

    public void setCallback(ICallback<T> mCallback){
        this.mCallback = mCallback;
    }
    public ICallback getCallback(){
        return mCallback;
    }

    public boolean isShowCamera(){
        return mShowCamera;
    }
    /**
     * set if show the first item as camera. default is true.
     * @param show
     */
    public void setShowCamera(boolean show){
        if(this.mShowCamera != show) {
            boolean oldShow = this.mShowCamera;
            this.mShowCamera = show;
            final AdapterManager<T> am = getAdapterManager();
            // false -> true
            if(!oldShow){
                am.addItem(am.getItemAt(0));  //just add a holder for camera
            }else{
                //true -> false
                am.removeItem(0);
            }
        }
    }

    @Override
    protected int getItemViewTypeImpl(HeaderFooterHelper hfHelper, int position) {
        if(isShowCamera() && position == 0){
            return VIEW_TYPE_CAMERA;
        }
        return super.getItemViewTypeImpl(hfHelper, position);
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolderImpl(HeaderFooterHelper hfHelper,
                                                             ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_CAMERA){
             return new ViewHolder2(LayoutInflater.from(parent.getContext()).inflate(
                     getCameraItemLayoutId(), parent, false));
        }
        return super.onCreateViewHolderImpl(hfHelper, parent, viewType);
    }

    /**
     * get the camera item layout id . default is equal to getItemLayoutId(0,null);
     */
    protected  int getCameraItemLayoutId(){
       // throw new UnsupportedOperationException("if you need camera , you must overiride method 'getCameraItemLayoutId()'");
        return getItemLayoutId(0, null);
    }

    @Override
    protected void onBindDataImpl(RecyclerView.ViewHolder holder, int position, T item) {
        IRecyclerViewHolder  vh = (IRecyclerViewHolder) holder;
        onBindData(vh.getViewHelper().getContext(), position,  item, vh.getLayoutId(), vh.getViewHelper());
    }

    @Override
    protected void onBindData(Context context, final int position, final T item,
                              int itemLayoutId, final ViewHelper helper) {
      //  Logger.i(TAG,"onBindData", "position = " + position+", item = " + item);

        if(itemLayoutId ==0){
              //camera.
              if(bindCameraItemSuccess(context, position, helper)){
                  return;
              }
              final ImageView view = helper.getView(R.id.photo_picker_iv_image);
              view.setScaleType(ImageView.ScaleType.CENTER);
              view.setImageURI(new Uri.Builder()
                      .scheme("res")
                      .path(String.valueOf(R.mipmap.ic_camera_album))
                      .build());
              helper.setVisibility(R.id.photo_picker_iv_select_icon,false)
                      .setBackgroundColor(R.id.photo_picker_iv_image, DEFAULT_COLOR_GRAY)
                      .setRootOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                                if(mCallback!=null){
                                    mCallback.onClickCamera(v);
                                }
                          }
                      });
          }else{
              final ImageView view = helper.getView(R.id.photo_picker_iv_image);
              view.setScaleType(ImageView.ScaleType.CENTER_CROP);
              ImageView iv = helper.getView(R.id.photo_picker_iv_select_icon);
              //apply select state
              applySelectState(iv, item.isSelected());

              //bind image and event
              helper.setImageUrl(R.id.photo_picker_iv_image, item.getPath(), PhotoPickerFactory.getImageLoader())
                     .setOnClickListener(R.id.photo_picker_iv_select_icon, new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             if (mCallback == null) {
                                 getSelectHelper().toogleSelected(position);
                             } else {
                                 if (!mCallback.shouldIgnoreClickEventOfSelectIcon(
                                        position , item, getSelectHelper().getSelectedItems())) {
                                     getSelectHelper().toogleSelected(position);
                                     mCallback.onClickSelectIcon(helper.getRootView(),position,
                                             item,getSelectHelper().getSelectedItems());
                                 }
                             }
                         }
                     })
                      .setRootOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                              if (mCallback != null) {
                                  mCallback.onClickItemView(v, position, item);
                              }
                          }
                      });
        }
    }

    /**
     * if you want to bind camera item by your self,please overrider this.
     * @return false means bind the camera item with default action.
     */
    protected boolean bindCameraItemSuccess(Context context, int position, ViewHelper helper) {
        return false;
    }

    /**
     * apply the select state.
     * @param selectIcon the imageview of select icon.
     * @param selected  the state , indicate is selected or not.
     */
    protected abstract void applySelectState(ImageView selectIcon, boolean selected);


    private static class ViewHolder2 extends RecyclerView.ViewHolder implements IRecyclerViewHolder{
        private final ViewHelper mViewHelper;
        public ViewHolder2(View itemView) {
            super(itemView);
            mViewHelper = new ViewHelper(itemView);
        }
        @Override
        public int getLayoutId() {
            return 0;
        }
        @Override
        public ViewHelper getViewHelper() {
            return mViewHelper;
        }
    }

    /**
     * the callback of item event
     * @param <T>
     */
    public interface ICallback<T>{

        /**
         * called when the user click the camera item view.
         */
        void onClickCamera(View itemView);

        /**
         * called when the user click this whole item view , not the select icon.
         */
        void onClickItemView(View itemView, int position ,T item);

        /**
         * called when clicked the select icon. return true if you don't want to switch the select state.
         * @param position the position
         * @param item the current item.
         * @param selectItems  the select items after switch the select state of select icon.
         */
        void onClickSelectIcon(View itemView,int position, T item,List<T> selectItems);

        /**
         * return true if you don't want to switch the select state, that means the click event of select icon is ignored.
         * @param position the position
         * @param item the current item.
         * @param selectItems the select items before this click event of  select icon.
         * @return true to ignore the event
         */
        boolean shouldIgnoreClickEventOfSelectIcon(int position, T item,List<T> selectItems);
    }
}
