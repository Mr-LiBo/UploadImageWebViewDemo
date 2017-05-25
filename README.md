# Android Upload Image WebView Demo

> 安卓APP的WebView默认屏蔽了&lt;input type="file"&gt;该控件的使用，需要通过java代码实现WebView内拍照、选择相册图片。

## Get Start
新建一个Activity extends WebViewActivity，并且实现WebViewActivity的抽象方法public abstract String getUrl();

### 添加权限Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 添加混淆，支持release版
-keepclassmembers class * extends android.webkit.WebChromeClient{
   		public void openFileChooser(...);
}

### demo
Github：https://github.com/nuisanceless/UploadImageWebViewDemo
