package com.medlinker.photopicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

/**
 * a photo picker helper, help you take photo, scan photoes.the more to see in demo
 * Created by heaven7 on 2016/4/5.
 */
public final class PhotoPickerHelper {

    public static final String KEY_PHOTOES            = "photoes";
    public static final String KEY_PHOTOES_SELECTED   = "photoes_selected";
    public static final String KEY_SELECT_INDEX       = "photo_select_index";

    /** the request code for take photo */
    public static final int REQUEST_TAKE_PHOTO       = 101 ;

    /** the request code for big picture */
    public final static int REQUEST_CODE_SEE_BIG_PIC = 102 ;

    /** the index of all photo in the {@link PhotoLoadResultCallback#onResultCallback(List<PhotoDirectory>)}*/
    public static final int INDEX_ALL_PHOTOS = 0;
    /**
     * the root dir to save the capture image.
     */
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getPath() +
            File.separator + "Medlinker";

    private static final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
    };

    private final ImageCaptureManager mCaptureManager;

    /**
     * the photo async loader callback
     * @param <T>
     */
    public interface PhotoLoadResultCallback<T extends IPhotoFileEntity> {
        /**
         * called on load finished.
         * @param directories the all photo dirs , every dir contains some photoes/images.
         */
        void onResultCallback(List<PhotoDirectory<T>> directories);
    }

    /*public*/ PhotoPickerHelper(Activity context) {
        this.mCaptureManager = new ImageCaptureManager(context);
    }

    /**
     *  make an activity intent of take photo.
     * @return the intent to to launch take photo activity.
     * @throws IOException if create temp image file failed.
     */
    public Intent makeTakePhotoIntent() throws IOException {
        return mCaptureManager.makeTakePhotoIntent(ROOT_DIR);
    }

    /**
     *  get the absolute path of take photo.
     * @return the absolute path of take photo
     */
    public String getCurrentPhotoPath(){
        return mCaptureManager.getCurrentPhotoPath();
    }

    /**
     * scan the photo file to the local media database which is saved by take photo.
     */
    public void scanFileToDatabase() {
        mCaptureManager.scanFileToDatabase();
    }
    /**
     * scan the photoes, this is async.
     * @param args the param
     * @param resultCallback the callback
     */
    public <T extends IPhotoFileEntity> void scanPhotoes(Bundle args, PhotoLoadResultCallback<T> resultCallback) {
        Context context =  mCaptureManager.getContext();
        if(context instanceof FragmentActivity){
            ((FragmentActivity) context).getSupportLoaderManager().initLoader(0, args,
                    new PhotoDirLoaderCallbacks<T>(context, resultCallback));
        }else{
            ((Activity) context).getLoaderManager().initLoader(0, args,
                    new PhotoDirLoaderCallbacks2<T>(context, resultCallback));
        }
    }

    /**
     * an abstract class for loader callback
     */
    public static class AbsLoaderCallbacks<T extends IPhotoFileEntity>{
        private final Context context;
        private final PhotoLoadResultCallback<T> resultCallback;

        public AbsLoaderCallbacks(Context context, PhotoLoadResultCallback<T> resultCallback) {
            this.context = context;
            this.resultCallback = resultCallback;
        }

        public Context getContext() {
            return context;
        }
        public PhotoLoadResultCallback<T> getResultCallback() {
            return resultCallback;
        }

        /**
         * set up the directiry of the all photo's. default return false.
         * @param allPhotoesDir   the directiry of the all photo's
         * @return true if you set up it.otherwise return false.
         */
        protected boolean setUpAllPhotoedDirectory(PhotoDirectory<T> allPhotoesDir) {
            return false;
        }

        protected void doOnLoadFinished( Cursor data) {
            if (data == null) return;
            List<PhotoDirectory<T>> dirs = new ArrayList<>();
            //all photo with directory
            PhotoDirectory<T> allPhotoesDir = new PhotoDirectory<>();
            if(!setUpAllPhotoedDirectory(allPhotoesDir)){
                allPhotoesDir.setName(getContext().getString(R.string.all_image));
                allPhotoesDir.setId("ALL");
            }

            int imageId;
            String bucketId;
            String name;
            String path;

            PhotoDirectory<T> dir;
            int index;  //index of dir
            while (data.moveToNext()) {

                imageId = data.getInt(data.getColumnIndexOrThrow(_ID));
                bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
                name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                path = data.getString(data.getColumnIndexOrThrow(DATA));

                dir = new PhotoDirectory<>();
                dir.setId(bucketId);
                dir.setName(name);

                if ((index = dirs.indexOf(dir))== -1) {
                    dir.setPath(path);
                    dir.addPhoto(imageId, path);
                    dir.setDate(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                    dirs.add(dir);
                } else {
                    dirs.get(index).addPhoto(imageId, path);
                }
                allPhotoesDir.addPhoto(imageId, path);
            }

            if (allPhotoesDir.getPhotoPaths().size() > 0) {
                allPhotoesDir.setPath((String) allPhotoesDir.getPhotoPaths().get(0));
            }
            dirs.add(INDEX_ALL_PHOTOS, allPhotoesDir);
            if (getResultCallback() != null) {
                getResultCallback().onResultCallback(dirs);
            }
        }

    }

    private static class PhotoDirLoaderCallbacks2<T extends IPhotoFileEntity> extends AbsLoaderCallbacks<T>
            implements android.app.LoaderManager.LoaderCallbacks<Cursor>{

        public PhotoDirLoaderCallbacks2(Context context, PhotoLoadResultCallback<T> resultCallback) {
            super(context, resultCallback);
        }

        @Override
        public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new PhotoDirectoryLoader2(getContext());
        }

        @Override
        public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
            doOnLoadFinished(data);
        }

        @Override
        public void onLoaderReset(android.content.Loader<Cursor> loader) {

        }
    }
    private static class PhotoDirLoaderCallbacks<T extends IPhotoFileEntity>  extends AbsLoaderCallbacks<T>
            implements LoaderManager.LoaderCallbacks<Cursor>{

        public PhotoDirLoaderCallbacks(Context context, PhotoLoadResultCallback<T> resultCallback) {
            super(context,resultCallback);
        }
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new PhotoDirectoryLoader(getContext());
        }
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            doOnLoadFinished(data);
        }
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private static class PhotoDirectoryLoader2 extends android.content.CursorLoader{

        public PhotoDirectoryLoader2(Context context) {
            super(context);

            setProjection(IMAGE_PROJECTION);
            setUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            setSortOrder(MediaStore.Images.Media.DATE_ADDED + " DESC");

            setSelection(MIME_TYPE + "=? or " + MIME_TYPE + "=? ");

            String[] selectionArgs;
            selectionArgs = new String[]{"image/jpeg", "image/png"};
            setSelectionArgs(selectionArgs);
        }
    }

    private static class PhotoDirectoryLoader extends CursorLoader {

        public PhotoDirectoryLoader(Context context) {
            super(context);

            setProjection(IMAGE_PROJECTION);
            setUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            setSortOrder(MediaStore.Images.Media.DATE_ADDED + " DESC");

            setSelection(MIME_TYPE + "=? or " + MIME_TYPE + "=? ");

            String[] selectionArgs;
            selectionArgs = new String[]{"image/jpeg", "image/png"};
            setSelectionArgs(selectionArgs);
        }

      /*  public PhotoDirectoryLoader(Context context, Uri uri, String[] projection, String selection,
                                       String[] selectionArgs, String sortOrder) {
             super(context, uri, projection, selection, selectionArgs, sortOrder);
        }*/
    }
}
