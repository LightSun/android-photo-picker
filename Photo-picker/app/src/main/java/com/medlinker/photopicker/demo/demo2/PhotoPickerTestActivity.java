package com.medlinker.photopicker.demo.demo2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.heaven7.adapter.ISelectable;
import com.heaven7.core.util.ViewHelper;
import com.medlinker.photopicker.BasePhotoFileEntity;
import com.medlinker.photopicker.PhotoDirectory;
import com.medlinker.photopicker.PhotoGridAdapter;
import com.medlinker.photopicker.PhotoPickerFactory;
import com.medlinker.photopicker.PhotoPickerHelper;
import com.medlinker.photopicker.demo.BaseActivity;
import com.medlinker.photopicker.demo.DraweeImageLoader;
import com.medlinker.photopicker.demo.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;

/**
 * Created by heaven7 on 2016/4/5.
 */
public class PhotoPickerTestActivity extends BaseActivity implements PhotoPickerHelper.PhotoLoadResultCallback<BasePhotoFileEntity>{

    @InjectView(R.id.tv_all_image)
    TextView tv_all_image;
    @InjectView(R.id.tv_done_notice)
    TextView tv_done_notice;
    @InjectView(R.id.iv_back)
    ImageView iv_Back;

    @InjectView(R.id.rv_photos)
    RecyclerView rv_photos;

    private PhotoPickerHelper mPickerHelper;
    private List<PhotoDirectory<BasePhotoFileEntity>> mPhotoDirs;

    private PhotoGridAdapter<BasePhotoFileEntity> mGridAdapter;
    private final PhotoGridAdapter.ICallback<BasePhotoFileEntity> mCallback = new PhotoGridAdapter.ICallback<BasePhotoFileEntity>() {
        @Override
        public void onClickCamera(View itemView) {
            showToast("onClickCamera");
            try {
                startActivityForResult(mPickerHelper.makeTakePhotoIntent(), PhotoPickerHelper.REQUEST_TAKE_PHOTO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClickItemView(View itemView, int position, BasePhotoFileEntity item) {
            showToast("onClickItemView: position = " + position);
            List<BasePhotoFileEntity> selectItems = mGridAdapter.getSelectHelper().getSelectedItems();
            ArrayList<BasePhotoFileEntity> photoes = new ArrayList<>(mGridAdapter.getAdapterManager().getItems());
            if(mGridAdapter.isShowCamera()){
                photoes.remove(0);
                position -= 1;
            }

            Bundle b = new Bundle();
            b.putInt(PhotoPickerHelper.KEY_SELECT_INDEX, position);
            b.putParcelableArrayList(PhotoPickerHelper.KEY_PHOTOES, photoes);
            if(selectItems!=null) {
                b.putParcelableArrayList(PhotoPickerHelper.KEY_PHOTOES_SELECTED,
                        new ArrayList<>(mGridAdapter.getSelectHelper().getSelectedItems()));
            }
            getIntentExecutor().launchActivityForResult(PhotoPagerActivity.class,
                    PhotoPickerHelper.REQUEST_CODE_SEE_BIG_PIC, b);
        }

        @Override
        public void onClickSelectIcon(View itemView, int position, BasePhotoFileEntity item,List<BasePhotoFileEntity> selectItems){
            int size = selectItems!=null ? selectItems.size() : 0;
            tv_done_notice.setText(getString(R.string.template_done ,size));
            showToast("onClickSelectIcon: position = " + position + " ,size = " + size);
        }

        @Override
        public boolean shouldIgnoreClickEventOfSelectIcon(int position, BasePhotoFileEntity item,List<BasePhotoFileEntity> selectItems) {
            if(selectItems!=null && selectItems.size() == 9){
                showToast(R.string.only_permit_9_image);
                return true;
            }
            return false;
        }

    };

    @Override
    protected int getlayoutId() {
        return R.layout.ac_photo_picker;
    }

    @Override
    protected void initView() {
        setCommonBackListener(iv_Back);

        //StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        //layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        // rv_photos.setItemAnimator(new DefaultItemAnimator());
        //adapter
        GridLayoutManager layoutManager = new GridLayoutManager(this,3,GridLayoutManager.VERTICAL,false);
        rv_photos.setLayoutManager(layoutManager);
        rv_photos.addItemDecoration(new SpacesItemDecoration((int) getResources().getDimension(R.dimen.photo_width)));

        PhotoPickerFactory.setImageLoader(new DraweeImageLoader(0));
        //this also is the default factory
        PhotoPickerFactory.setPhotoFileEntityFactory(new PhotoPickerFactory.IPhotoFileEntityFactory<
                BasePhotoFileEntity>() {
            @Override
            public BasePhotoFileEntity create(int id, String path) {
                return new BasePhotoFileEntity(id, path);
            }
        });

        mPickerHelper = PhotoPickerFactory.createPhotoPickerHelper(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }else{
            mPickerHelper.scanPhotoes(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case PhotoPickerHelper.REQUEST_CODE_SEE_BIG_PIC:
                finishSelect();
                break;
            case PhotoPickerHelper.REQUEST_TAKE_PHOTO:
                 mPickerHelper.scanFileToDatabase();
                 String path = mPickerHelper.getCurrentPhotoPath();
                 BasePhotoFileEntity  entity = (BasePhotoFileEntity) PhotoPickerFactory.getPhotoFileEntityFactory()
                         .create(path.hashCode(), path);
                //add to dir
                final PhotoDirectory<BasePhotoFileEntity> dirs = mPhotoDirs.get(PhotoPickerHelper.INDEX_ALL_PHOTOS);
                dirs.getPhotos().add(0, entity);
                dirs.setPath(path);
                //notify adapter
                mGridAdapter.clearAllSelected();
                mGridAdapter.getAdapterManager().getItems().add(0,entity);
                finishSelect();
                break;
        }
    }
    private void finishSelect() {
        Intent sIntent = new Intent();
        List<BasePhotoFileEntity> selectedPhotos = mGridAdapter.getSelectHelper().getSelectedItems();
        sIntent.putParcelableArrayListExtra(PhotoPickerHelper.KEY_PHOTOES_SELECTED, (ArrayList<? extends Parcelable>) selectedPhotos);
        setResult(RESULT_OK, sIntent);
        finish();
    }

    @Override
    public void onResultCallback(List<PhotoDirectory<BasePhotoFileEntity>> directories) {
        this.mPhotoDirs = directories;
        ////directories.get(0) contains the all photoes. so this as the whole directory.
        final List<BasePhotoFileEntity> photos = directories.get(0).getPhotos();
        if(mGridAdapter == null) {
            if(photos.size()==0){
                showToast("no photoes");
               // return;
            }
            mGridAdapter = new PhotoGridAdapter<BasePhotoFileEntity>(R.layout.item_photo,
                    photos, ISelectable.SELECT_MODE_MULTI) {
                @Override
                protected void applySelectState(ImageView selectIcon, boolean selected) {
                    selectIcon.setImageResource( selected ? R.mipmap.pic_check_select : R.mipmap.pic_check_normal);
                }

                @Override
                protected boolean bindCameraItemSuccess(Context context, int position, ViewHelper helper) {
                   // ImageView view = helper.getView(com.medlinker.photopicker.R.id.photo_picker_iv_select_icon);
                    return false;
                }
            };
            mGridAdapter.setCallback(mCallback);
            rv_photos.setAdapter(mGridAdapter);
        }else{
            mGridAdapter.getAdapterManager().replaceAllItems(photos);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mPickerHelper.scanPhotoes(this);
        }else{
            showToast("permission denied ！");
        }
    }

    static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;
        public SpacesItemDecoration(int space) {
            this.space = space;
        }
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            if ((parent.getChildLayoutPosition(view) + 1) % 3 == 0) {
                outRect.right = space;
            } else {
                outRect.right = space;
            }
            outRect.bottom = space;
        }
    }

}
