package com.example.beautydemo.launch;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.beautydemo.R;
import com.example.beautydemo.util.PermissionUtils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 0;
    private static final String TAG = "MainActivity";

    private boolean mOnClick;

    private String activationCode = "6d3fe3b7-36d7-4382-bf7b-e6b8bfe639cc" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        initView();
    }

    private void checkPermissions() {
        boolean cameraEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.CAMERA);
        boolean storageWriteEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean recordAudio = PermissionUtils.permissionChecking(this,
                Manifest.permission.RECORD_AUDIO);
        if (!cameraEnable || !storageWriteEnable || !recordAudio) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CODE);
        }
    }

    private void initView() {
        findViewById(R.id.btn_camera).setOnClickListener(this);
//        findViewById(R.id.btn_edit_video).setOnClickListener(this);
//        findViewById(R.id.btn_edit_picture).setOnClickListener(this);



        // initialize url.
        final EditText activationCodeInput = (EditText) findViewById(R.id.activation_input_edit_text);
        activationCodeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String fu = activationCodeInput.getText().toString();
                if (fu.isEmpty()) {
                    return;
                }

                activationCode = fu;

                Log.i(TAG, "face sdk activation code is " + activationCode);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOnClick = false;
    }

    @Override
    public void onClick(View v) {
        if (mOnClick) {
            return;
        }
        mOnClick = true;
        switch (v.getId()) {
            case R.id.btn_camera: {
                previewCamera();
                break;
            }

//            case R.id.btn_edit_video: {
//                scanMedia(false, false,true);
//                break;
//            }
//
//            case R.id.btn_edit_picture: {
//                scanMedia(false, true, false);
//                break;
//            }
        }
    }



    /**
     * 打开预览页面
     */
    private void previewCamera() {
//        PreviewEngine.from(this)
//                .setCameraRatio(AspectRatio.Ratio_16_9)
//                .showFacePoints(false)
//                .showFps(true)
//                .setGalleryListener(new OnGallerySelectedListener() {
//                    @Override
//                    public void onGalleryClickListener(GalleryType type) {
//                        scanMedia(type == GalleryType.ALL);
//                    }
//                })
//                .setPreviewCaptureListener(new OnPreviewCaptureListener() {
//                    @Override
//                    public void onMediaSelectedListener(String path, GalleryType type) {
//                        if (type == GalleryType.PICTURE) {
//                            Intent intent = new Intent(MainActivity.this, ImageEditActivity.class);
//                            intent.putExtra(ImageEditActivity.PATH, path);
//                            startActivity(intent);
//                        } else if (type == GalleryType.VIDEO) {
//                            Intent intent = new Intent(MainActivity.this, VideoCropActivity.class);
//                            intent.putExtra(VideoCropActivity.PATH, path);
//                            startActivity(intent);
//                        }
//                    }
//                })
//                .startPreview();


        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("activationCode", activationCode);
        startActivity(intent);
    }

//    /**
//     * 扫描媒体库
//     */
//    private void scanMedia(boolean enableGif) {
//        scanMedia(enableGif, true, true);
//    }
//
//    /**
//     * 扫描媒体库
//     * @param enableGif
//     * @param enableImage
//     * @param enableVideo
//     */
//    private void scanMedia(boolean enableGif, boolean enableImage, boolean enableVideo) {
//        MediaScanEngine.from(this)
//                .setMimeTypes(MimeType.ofAll())
//                .ImageLoader(new GlideMediaLoader())
//                .spanCount(4)
//                .showCapture(true)
//                .showImage(enableImage)
//                .showVideo(enableVideo)
//                .enableSelectGif(enableGif)
//                .setCaptureListener(new OnCaptureListener() {
//                    @Override
//                    public void onCapture() {
//                        previewCamera();
//                    }
//                })
//                .setMediaSelectedListener(new OnMediaSelectedListener() {
//                    @Override
//                    public void onSelected(List<Uri> uriList, List<String> pathList, boolean isVideo) {
//                        if (isVideo) {
//                            Intent intent = new Intent(MainActivity.this, VideoCropActivity.class);
//                            intent.putExtra(VideoCropActivity.PATH, pathList.get(0));
//                            startActivity(intent);
//                        } else {
//                            Intent intent = new Intent(MainActivity.this, ImageEditActivity.class);
//                            intent.putExtra(ImageEditActivity.PATH, pathList.get(0));
//                            startActivity(intent);
//                        }
//                    }
//                })
//                .scanMedia();
//    }



}
