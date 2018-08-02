# 项目描述
 最近项目用到上传头像与上传照片功能，在网上找了几个别人封装的控件都没有合适的，于是自己就动手写了上传图片的功能，想到是常用功能，在闲暇时间进行了封装，共享给大家使用。
 
##### 功能描述:
- 动态权限（Rxpermissions）
- 相机（支持7.0）
- 相册
- 剪切
- 图片压缩（Luban）


  # 导入
  将其添加到存储库末尾的根build.gradle中   
步骤1.将JitPack存储库添加到构建文件中
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
步骤2.添加依赖项  
```
dependencies {
    implementation 'com.github.fuliang0721:FlPhoto:v1.0'
}
```
 # 使用
 

方法 | 描述
---|---
show | 显示
hide | 隐藏
isShow | 是否显示
isCrop | 是否剪切 true是 false否
setZipSize | 不压缩的阈值，单位为ķ

步骤1  
```
implements FlResult
```
步骤2 
```
FlPhoto flPhoto = new FlPhoto(this,this);
flPhoto.isCrop(true);
flPhoto.setZipSize(100);
```
步骤3 
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    ActivityResultBean activityResultBean = new ActivityResultBean();
    activityResultBean.setRequestCode(requestCode);
    activityResultBean.setResultCode(resultCode);
    activityResultBean.setData(data);
    flPhoto.setActivityResult(activityResultBean);
}
```
步骤4
```
@Override
public void flStart() {} //图片压缩开始

@Override
public void flSuccess(File file) {} //图片压缩成功

@Override
public void flError(Throwable e) {} //图片压缩错误
   
```
# 代码
```
public class MainActivity extends AppCompatActivity implements FlResult{

    Button button;

    ImageView main_img_show;

    FlPhoto flPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flPhoto = new FlPhoto(this,this);
        flPhoto.isCrop(true);
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
    public void flStart() { //图片压缩开始

    }

    @Override
    public void flSuccess(File file) { //图片压缩成功
        Glide.with(this)
                .load(file)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE) //不使用硬盘缓存
                .into(main_img_show);
    }

    @Override
    public void flError(Throwable e) { //图片压缩错误

    }
}
```
## [Github](https://github.com/fuliang0721/FlPhoto) 地址

如果有问题或者意见，请留言给我，我会在闲暇时间继续升级。
