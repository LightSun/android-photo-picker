package com.medlinker.photopicker.demo.demo2;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.heaven7.core.util.Logger;
import com.medlinker.photopicker.BasePhotoFileEntity;
import com.medlinker.photopicker.PhotoPagerAdapter;
import com.medlinker.photopicker.PhotoPickerHelper;
import com.medlinker.photopicker.demo.BaseActivity;
import com.medlinker.photopicker.demo.R;

import java.util.ArrayList;

import butterknife.InjectView;
import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created by heaven7 on 2016/4/6.
 */
public class PhotoPagerActivity extends BaseActivity {

    private static final String TAG = "PhotoPagerActivity";

    @InjectView(R.id.vp_photos)
    ViewPager vp_photos;

    @InjectView(R.id.iv_selected)
    ImageView iv_selected;

    private ArrayList<BasePhotoFileEntity> mPhotos;
    private ArrayList<BasePhotoFileEntity> mSelectPhotoes;
    private int mSelectIndex ;
    @Override
    protected int getlayoutId() {
        return R.layout.ac_photo_picker_pager;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mPhotos = getIntent().getParcelableArrayListExtra(PhotoPickerHelper.KEY_PHOTOES);
        mSelectPhotoes = getIntent().getParcelableArrayListExtra(PhotoPickerHelper.KEY_PHOTOES_SELECTED);
        mSelectIndex = getIntent().getIntExtra(PhotoPickerHelper.KEY_SELECT_INDEX, 0);
        Logger.i(TAG," mSelectIndex = " + mSelectIndex);
        Logger.i(TAG," mPhotos = " + mPhotos.size() );
        if(mSelectPhotoes!=null) {
            Logger.i(TAG, " mSelectPhotoes = " + (mSelectPhotoes == null ? 0 : mSelectPhotoes.size()));
        }

        vp_photos.setAdapter(new PhotoPagerAdapter<BasePhotoFileEntity>(mPhotos) {
            @Override
            protected View onInstantiateItem(ViewGroup container, int position, BasePhotoFileEntity item) {
                PhotoDraweeView view = new PhotoDraweeView(container.getContext()) ;
                view.loadImage(item.getPath(), null, R.mipmap.ic_launcher, R.mipmap.ic_broken_image_black, ScalingUtils.ScaleType.CENTER);
                //if you need listener
               /*
               photoDraweeView.setOnViewTapListener(new OnViewTapListener() {
                });
                photoDraweeView.setOnLongClickListener(new View.OnLongClickListener() {
                });*/
                container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                return view;
            }
        });
        vp_photos.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                onPageSelected(position);
            }

            @Override
            public void onPageSelected(int position) {
                mSelectIndex = position;
                if(mSelectPhotoes!=null && mSelectPhotoes.contains(mPhotos.get(position))){
                    iv_selected.setImageResource(R.mipmap.pic_check_select);
                }else{
                    iv_selected.setImageResource(R.mipmap.pic_check_normal);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        vp_photos.setCurrentItem(mSelectIndex);
        iv_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO -----
            }
        });
    }

}
