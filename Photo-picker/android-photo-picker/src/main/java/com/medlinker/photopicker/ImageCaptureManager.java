package com.medlinker.photopicker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author heaven7
 * @version 1.0
 */
/*public*/ class ImageCaptureManager {

    private final Context mContext;
    private String mCurrentPhotoPath;

    public ImageCaptureManager(Context mContext) {
        this.mContext = mContext;
    }

    public Context getContext(){
        return mContext;
    }

    // permission
    private File createImageFile(String rootDir) throws IOException {
        //create a new image file name
        File image = File.createTempFile(UUID.randomUUID().toString(), ".jpg", ensureSaveDir(rootDir));
        //Save a file:path for user with ACTION_VIE intents
        mCurrentPhotoPath = image.getPath();
        return image;
    }

    public Intent makeTakePhotoIntent(String rootDir) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File photoFile = createImageFile(rootDir);
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
        }
        return takePictureIntent;
    }

    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public void scanFileToDatabase() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(mCurrentPhotoPath));
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }

    private static File ensureSaveDir(String rootDir){
        File dir = new File(rootDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }



}
