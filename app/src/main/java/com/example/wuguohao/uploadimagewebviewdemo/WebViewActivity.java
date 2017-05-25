package com.example.wuguohao.uploadimagewebviewdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class WebViewActivity extends AppCompatActivity {
    private static final String TAG = WebViewActivity.class.getSimpleName();
    public static final String URL_EXTRA_NAME_STR = "URL_EXTRA_NAME_STR";
    private String mUrlString;
    private WebView mWebView;
    private String mPhotoPath;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageArray;
    private final static int IMAGE_CHOOSE_REQUEST_CODE = 102;

    public abstract String getUrl();

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout rl = new RelativeLayout(getApplicationContext());
        mWebView = new WebView(getApplicationContext());
        rl.addView(mWebView);
        setContentView(rl);
        mUrlString = getUrl();


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }

        assert mWebView != null;
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);

        if(Build.VERSION.SDK_INT >= 21){
            webSettings.setMixedContentMode(0);
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT >= 19){
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT < 19){
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mWebView.setWebViewClient(new Callback());
        mWebView.loadUrl(mUrlString);
        mWebView.setWebChromeClient(mWebChromeClient);
    }

    WebChromeClient mWebChromeClient = new WebChromeClient() {

        //For Android 3.0-
        public void openFileChooser(ValueCallback<Uri> uploadMsg){
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            WebViewActivity.this.startActivityForResult(Intent.createChooser(i,"文件选择"),
                    IMAGE_CHOOSE_REQUEST_CODE);
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg, String acceptType){
            openFileChooser(uploadMsg);
        }

        //For Android 4.1+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
            openFileChooser(uploadMsg);
        }

        //For Android 5.0+
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(
                WebView webView,
                ValueCallback<Uri[]> filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams) {

            if(mUploadMessageArray != null){
                mUploadMessageArray.onReceiveValue(null);
            }
            mUploadMessageArray = filePathCallback;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(WebViewActivity.this.getPackageManager()) != null) {
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mPhotoPath);
                }catch(IOException ex){
                    Log.e(TAG, "Image file creation failed", ex);
                }
                if(photoFile != null){
                    mPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                }else{
                    takePictureIntent = null;
                }
            }
            Intent[] intentArray;
            if(takePictureIntent != null){
                intentArray = new Intent[]{takePictureIntent};
            }else{
                intentArray = new Intent[0];
            }


            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");


            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "从相机、相册选择图片");
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);//相册
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);//拍照
            startActivityForResult(chooserIntent, IMAGE_CHOOSE_REQUEST_CODE);
            return true;

        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Uri[] results = null;
            //Check if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == IMAGE_CHOOSE_REQUEST_CODE){
                    if(null == mUploadMessageArray){
                        return;
                    }
                    if(intent == null){
                        //Capture Photo if no image available
                        if(mPhotoPath != null){
                            results = new Uri[]{Uri.parse(mPhotoPath)};
                        }
                    }else{
                        String dataString = intent.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUploadMessageArray.onReceiveValue(results);
            mUploadMessageArray = null;
        }else{
            if(requestCode == IMAGE_CHOOSE_REQUEST_CODE){
                if(null == mUploadMessage) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }

    public class Callback extends WebViewClient {
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
            //Toast.makeText(getApplicationContext(), "加载页面失败.", Toast.LENGTH_SHORT).show();
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if( url.startsWith("http:") || url.startsWith("https:") ) {
                return false;
            }
            try{
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity( intent );
            } catch (Exception e) {

            }
            return true;
        }
    }

    /**
     * Create an image file
     * */
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    if(mWebView.canGoBack()){
                        mWebView.goBack();
                    }else{
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
}
