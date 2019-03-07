
package com.example.beautydemo.launch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.example.beautydemo.R;

/**
 * 相机预览页面
 */
public class CameraActivity extends AppCompatActivity implements OnPageOperationListener {

    private static final String FRAGMENT_CAMERA = "fragment_camera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        if (null == savedInstanceState) {
            CameraPreviewFragment fragment = new CameraPreviewFragment();
            fragment.setOnPageOperationListener(this);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, FRAGMENT_CAMERA)
                    .addToBackStack(FRAGMENT_CAMERA)
                    .commit();
        }



        faceTrackerRequestNetwork();

    }

    /**
     * 人脸检测SDK验证，可以替换成自己的SDK
     */
    private void faceTrackerRequestNetwork() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FaceTracker.requestFaceNetwork(CameraActivity.this);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    @Override
    public void onBackPressed() {
        // 判断fragment栈中的个数，如果只有一个，则表示当前只处于预览主页面点击返回状态
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount > 1) {
            getSupportFragmentManager().popBackStack();
        } else if (backStackEntryCount == 1) {
            CameraPreviewFragment fragment = (CameraPreviewFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_CAMERA);
            if (fragment != null) {
                if (!fragment.onBackPressed()) {
                    finish();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onOpenGalleryPage() {

    }

    @Override
    public void onOpenImageEditPage(String path) {

    }

    @Override
    public void onOpenVideoEditPage(String path) {

    }

    @Override
    public void onOpenCameraSettingPage() {

    }
}
