package com.flphoto;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * Created by Administrator on 2018/7/30 0030.
 */

public class FlPhotoPersenter {

    private static String TAG = FlPhotoPersenter.class.getSimpleName();

    Activity mActivity;

    private File mTempImageFile;

    private static final int CODE_GALLERY_REQUEST = 0xa00;//相册选取
    private static final int CODE_CAMERA_REQUEST = 0xa11; //拍照
    private static final int CODE_RESULT_REQUEST = 0xa22; //剪裁图片

    private boolean isClickCamera = false;

    Uri imageUri = null;

    private String imagePath;//图片保存地址

    private boolean isCrop = true;//是否剪切图片

    private boolean isGrantedAll = false;//是否剪切图片

    private int size = 100;

    FlResult flResult;

    public FlPhotoPersenter(Activity ativity, FlResult flResult) {
        this.mActivity = ativity;
        this.flResult = flResult;
    }

    public void requestPermissions() {
        RxPermissions rxPermission = new RxPermissions(mActivity);
        rxPermission.request(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new io.reactivex.functions.Consumer<Boolean>() {

                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            isGrantedAll = true;
                            if (isClickCamera) {
                                toAndroidCamera();
                            } else {
                                toAndroidPhoto();
                            }
                        } else {
                            isGrantedAll = false;
                            Toast.makeText(mActivity, "您没有授权该权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    /**
     * 启动系统相机
     */
    public void toAndroidCamera() {
        isClickCamera = true;
        if (!isGrantedAll) {
            requestPermissions();
            return;
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            createCameraTempImageFile();
            if (mTempImageFile != null && mTempImageFile.exists()) {
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(mActivity, mActivity.getPackageName() + ".fileprovider", mTempImageFile);//通过FileProvider创建一个content类型的Uri
                } else {
                    imageUri = Uri.fromFile(mTempImageFile);
                }
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //对目标应用临时授权该Uri所代表的文件

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI
                mActivity.startActivityForResult(cameraIntent, CODE_CAMERA_REQUEST);
            } else {
                Toast.makeText(mActivity, R.string.camera_temp_file_error_easy_photos, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mActivity, R.string.msg_no_camera_easy_photos, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从相册选择
     */
    public void toAndroidPhoto() {
        isClickCamera = false;
        if (!isGrantedAll) {
            requestPermissions();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        mActivity.startActivityForResult(intent, CODE_GALLERY_REQUEST);
    }

    /**
     * 裁剪
     */
    private void toAndroidCrop() {
        File dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FlPhoto" + File.separator + "crop_flphoto.jpg");
        Uri outputUri = Uri.fromFile(dir);//缩略图保存地址
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(imageUri, "image/*");
//        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        mActivity.startActivityForResult(intent, CODE_RESULT_REQUEST);
    }

    private void createCameraTempImageFile() {
        File dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FlPhoto" + File.separator + "Camera");
        if (!dir.exists() || !dir.isDirectory()) {
            if (!dir.mkdirs()) {
                dir = mActivity.getExternalFilesDir(null);
                if (null == dir || !dir.exists()) {
                    dir = mActivity.getFilesDir();
                    if (null == dir || !dir.exists()) {
                        String cacheDirPath = File.separator + "data" + File.separator + "data" + File.separator + mActivity.getPackageName() + File.separator + "cache" + File.separator;
                        dir = new File(cacheDirPath);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                    }
                }
            }
        }

        try {
            mTempImageFile = File.createTempFile("IMG", ".jpg", dir);
        } catch (IOException e) {
            e.printStackTrace();
            mTempImageFile = null;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        imagePath = null;
        imageUri = data.getData();
        if (Build.VERSION.SDK_INT >= 19) {
            if (DocumentsContract.isDocumentUri(mActivity, imageUri)) {
                //如果是document类型的uri,则通过document id处理
                String docId = DocumentsContract.getDocumentId(imageUri);
                if ("com.android.providers.media.documents".equals(imageUri.getAuthority())) {
                    String id = docId.split(":")[1];//解析出数字格式的id
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                } else if ("com.android.downloads.documents".equals(imageUri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null);
                }
            } else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
                //如果是content类型的Uri，则使用普通方式处理
                imagePath = getImagePath(imageUri, null);
            } else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
                //如果是file类型的Uri,直接获取图片路径即可
                imagePath = imageUri.getPath();
            }
        } else {
            imagePath = getImagePath(imageUri, null);
        }
        if (isCrop) {
            toAndroidCrop();
        } else {
            saveImg();
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection老获取真实的图片路径
        Cursor cursor = mActivity.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 系统回调
     *
     * @param activityResultBean
     */
    public void activityResult(ActivityResultBean activityResultBean) {
        Log.i(TAG, "getRequestCode:" + activityResultBean.getRequestCode());
        Log.i(TAG, "getResultCode:" + activityResultBean.getResultCode());
        if (activityResultBean.getResultCode() == 0) {
            return;
        }
        switch (activityResultBean.getRequestCode()) {
            //相机返回
            case CODE_CAMERA_REQUEST:
                if (isCrop) {
                    toAndroidCrop();
                } else {
                    saveImg();
                }
                break;
            //相册返回
            case CODE_GALLERY_REQUEST:
                handleImageOnKitKat(activityResultBean.getData());
                break;
            //裁剪返回
            case CODE_RESULT_REQUEST:
                saveImg();
                break;
        }
    }

    /**
     * 保存图片并压缩
     */
    private void saveImg() {
        String filePath = null;
        String saveImg = createPhotoaTempImageFile();
        try {
            if (isCrop) {
                filePath = Environment.getExternalStorageDirectory() + File.separator + "FlPhoto" + File.separator + "crop_flphoto.jpg";
            } else {
                if (isClickCamera) {
                    filePath = mTempImageFile.getAbsolutePath();
                } else {
                    filePath = imagePath;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Luban.with(mActivity)
                .load(filePath)
                .ignoreBy(size)
                .setTargetDir(saveImg)
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.i(TAG, " TODO 压缩开始前调用，可以在方法内启动 loading UI");
                        // TODO 压缩开始前调用，可以在方法内启动 loading UI
                        flResult.flStart();
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.i(TAG, "TODO 压缩成功后调用，返回压缩后的图片文件");
                        Log.i(TAG, file.getAbsolutePath());
                        Log.i(TAG, file.length() + "");
                        flResult.flSuccess(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "TODO 当压缩过程出现问题时调用" + e.getMessage());
                        // TODO 当压缩过程出现问题时调用
                        flResult.flError(e);
                    }
                }).launch();
    }

    private String createPhotoaTempImageFile() {
        File dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FlPhoto" + File.separator + "ZIP");
        if (!dir.exists() || !dir.isDirectory()) {
            if (!dir.mkdirs()) {
                dir = mActivity.getExternalFilesDir(null);
                if (null == dir || !dir.exists()) {
                    dir = mActivity.getFilesDir();
                    if (null == dir || !dir.exists()) {
                        String cacheDirPath = File.separator + "data" + File.separator + "data" + File.separator + mActivity.getPackageName() + File.separator + "cache" + File.separator;
                        dir = new File(cacheDirPath);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                    }
                }
            }
        }
        return dir.getAbsolutePath();
    }

    /**
     * 是否剪切图片
     *
     * @param isCrop
     */
    public void isCrop(Boolean isCrop) {
        this.isCrop = isCrop;
    }

    /**
     * 压缩大小 位置k
     *
     * @param size
     */
    public void zipSize(int size) {
        this.size = size;
    }
}
