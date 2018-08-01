package com.example.flphotodemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.flphoto.ActivityResultBean;
import com.flphoto.FlPhoto;
import com.flphoto.FlResult;

import java.io.File;

public class MainActivity extends AppCompatActivity implements FlResult{

    Button button;

    ImageView main_img_show;

    FlPhoto flPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flPhoto = new FlPhoto(this,this);
        flPhoto.isCrop(false);
        flPhoto.setZipSize(100);
        button = findViewById(R.id.main_btn_button);
        main_img_show = findViewById(R.id.main_img_show);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flPhoto.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultBean activityResultBean = new ActivityResultBean();
        activityResultBean.setRequestCode(requestCode);
        activityResultBean.setResultCode(resultCode);
        activityResultBean.setData(data);
        flPhoto.setActivityResult(activityResultBean);
    }

    @Override
    public void flStart() {

    }

    @Override
    public void flSuccess(File file) {
        Glide.with(this)
                .load(file)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE) //不使用硬盘缓存
                .into(main_img_show);
    }

    @Override
    public void flError(Throwable e) {

    }
}
