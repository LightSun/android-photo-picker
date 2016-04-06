package com.medlinker.photopicker.demo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.medlinker.photopicker.demo.demo2.PhotoPickerTestActivity;

import java.io.File;

public class MainActivity extends ListActivity {

    public static final String[] options = {
            "PhotoPickerTestActivity",
           // "PhotoPickerTestActivity",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder()
                .setBaseDirectoryPath(new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "Medlinker Pictures"))
                .setBaseDirectoryName("fresco")
                .setMaxCacheSize(Runtime.getRuntime().maxMemory() / 8)
                .build();
        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, imagePipelineConfig);

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent;

        switch (position) {
            case 0:
                intent = new Intent(this, PhotoPickerTestActivity.class);
                break;
        /*    case 1:
                intent = new Intent(this, PhotoPagerActivity.class);
                break;*/
            default:
                throw new UnsupportedOperationException();
        }
        startActivity(intent);
    }
}
