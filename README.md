# android-photo-picker
the photo picker library  of android

# New features


# Usage
-  1 , first you need to set the image loader , and the  photo file entity 
   factory is optional. see below.
``` java
    PhotoPickerFactory.setImageLoader(new DraweeImageLoader(0));
    //this also is the default factory
    PhotoPickerFactory.setPhotoFileEntityFactory(new PhotoPickerFactory.IPhotoFileEntityFactory<
            BasePhotoFileEntity>() {
        @Override
        public BasePhotoFileEntity create(int id, String path) {
            return new BasePhotoFileEntity(id, path);
        }
    });
    
    // in demo we just use fresco lib to load image, you can use other library.
public class DraweeImageLoader implements ViewHelper.IImageLoader {

    private final ControllerListener<ImageInfo> mListener;
    private final int mDefaultResId;

    public DraweeImageLoader(int defaultResId ) {
        this(null,defaultResId);
    }

    public DraweeImageLoader(ControllerListener<ImageInfo> mListener, int defaultResId ) {
        this.mListener = mListener;
        this.mDefaultResId = defaultResId;
    }

    @Override
    public void load(String url, ImageView iv) {
        int size  = iv.getContext().getResources().getDimensionPixelSize(R.dimen.photo_size);
        SimpleDraweeView view = (SimpleDraweeView) iv;
        Uri uri = url.startsWith("http://") || url.startsWith("https://") ? Uri.parse(url) : Uri.fromFile(new File(url));
        try {
            ImageRequest request = ImageRequestBuilder
                   // .newBuilderWithSource(Uri.fromFile(new File(url)))
                    .newBuilderWithSource(uri)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                    .setProgressiveRenderingEnabled(false)
                    .setResizeOptions(new ResizeOptions(size, size))
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setControllerListener(mListener)
                   // .setUri(uri)
                    .setImageRequest(request)
                    .build();
            if(mDefaultResId != 0) {
                view.getHierarchy().setPlaceholderImage(mDefaultResId);
            }
            view.setController(controller);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

 }
```
- 2, create the PhotoPickerHelper in you activity.
``` java
 mPickerHelper = PhotoPickerFactory.createPhotoPickerHelper(this);
 ```
 
- 3, declare the item layout of photo item. bou must declare
 {@link com.medlinker.photopicker.R.id#photo_picker_iv_image} as the photo ImageView's(or its child class) id
 and {@link com.medlinker.photopicker.R.id#photo_picker_iv_select_icon} as the select icon
 ImageView's id in item layout. 
 and at last do scan the photo files and set adapter in callback ， here is the demo.
``` java
 if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
  }else{
        mPickerHelper.scanPhotoes(null, this);
  }
  
   @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mPickerHelper.scanPhotoes(null, this);
        }else{
            showToast("permission denied ！");
        }
    }
    
    //callback method 
     @Override
    public void onResultCallback(List<PhotoDirectory<BasePhotoFileEntity>> directories) {
        this.mPhotoDirs = directories;
        if(mGridAdapter == null) {
           //directories.get(0) contains the all photoes. so this as the whole directory.
            mGridAdapter = new PhotoGridAdapter<BasePhotoFileEntity>(R.layout.item_photo,
                    directories.get(0).getPhotos(), ISelectable.SELECT_MODE_MULTI) {
                @Override
                protected void applySelectState(ImageView selectIcon, boolean selected) {
                    selectIcon.setImageResource( selected ? R.mipmap.pic_check_select : R.mipmap.pic_check_normal);
                }

                @Override //override this if you want to set up the camera item(the first item)
                protected boolean bindCameraItemSuccess(Context context, int position, ViewHelper helper) {
                   // ImageView view = helper.getView(com.medlinker.photopicker.R.id.photo_picker_iv_select_icon);
                    return false;
                }
            };
            mGridAdapter.setCallback(mCallback);
            rv_photos.setAdapter(mGridAdapter);
        }else{
            mGridAdapter.getAdapterManager().replaceAllItems(directories.get(0).getPhotos());
        }
    }
        
```
- 4, set callback for PhotoGridAdapter ( it contains the all event callback.)
 here is the demo.
``` java
 mGridAdapter.setCallback(mCallback);
 
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

```
