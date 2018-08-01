package com.flphoto;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.io.File;

/**
 * Created by Administrator on 2018/7/30 0030.
 */

public class FlPhoto implements FlPhotoImpl{

    private Context context;

    private PopupWindow popwindow;

    private View popview;

    private final int REQUEST_CODE_CAMERA = 1000;

    private final int REQUEST_CODE_GALLERY = 1001;

    private RelativeLayout btnBendi;

    private RelativeLayout btnCapture;

    private RelativeLayout btnCancel;

    FlPhotoPersenter flPhotoPersenter;

    public FlPhoto(final Context context,FlResult flResult) {

        this.context = context;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        popview = layoutInflater.inflate(R.layout.pop_picture_select, null);
        btnBendi = (RelativeLayout) popview.findViewById(R.id.btn_bendi);
        btnCancel = (RelativeLayout) popview.findViewById(R.id.btn_cancel);
        btnCapture = (RelativeLayout) popview.findViewById(R.id.btn_paizhao);
        popwindow = new PopupWindow(popview, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popwindow.setBackgroundDrawable(new ColorDrawable());
        popwindow.setAnimationStyle(R.style.AnimBottom);
        popwindow.setTouchable(true);
        popwindow.setOutsideTouchable(true);
        popwindow.setFocusable(true);

        popwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(context, 1.0f);
            }
        });

        //取消
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        //本地相册
        btnBendi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                flPhotoPersenter.toAndroidPhoto();
            }
        });

        //拍照
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                flPhotoPersenter.toAndroidCamera();
            }
        });

        flPhotoPersenter = new FlPhotoPersenter((Activity) context,flResult);

    }

    @Override
    public void show() {
        if (!isShow()) {
            setBackgroundAlpha(context, 0.5f);
            popwindow.showAtLocation(popview, Gravity.BOTTOM, 0, 0);
        }
    }

    @Override
    public void hide() {
        if (isShow()) {
            popwindow.dismiss();
        }
    }

    private void setBackgroundAlpha(Context context, float bgAlpha) {
        WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
        lp.alpha = bgAlpha;
        ((Activity) context).getWindow().setAttributes(lp);
    }

    @Override
    public boolean isShow() {
        return popwindow.isShowing();
    }

    public void setActivityResult(ActivityResultBean activityResultBean){
        flPhotoPersenter.activityResult(activityResultBean);
    }

    public void isCrop(boolean isCrop){
        flPhotoPersenter.isCrop(isCrop);
    }

    public void setZipSize(int size){
        flPhotoPersenter.zipSize(size);
    }
}
