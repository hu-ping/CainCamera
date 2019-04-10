package com.example.beautydemo.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;
import android.util.Size;
import android.util.TypedValue;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.example.beautydemo.R;
import com.example.beautydemo.camera.CameraEngine;
import com.example.beautydemo.camera.CameraParam;
import com.example.beautydemo.face.FaceTracker;
import com.example.beautydemo.face.FaceTrackerCallback;
import com.example.beautydemo.util.PathConstraints;
import com.example.beautydemo.util.PermissionConfirmDialogFragment;
import com.example.beautydemo.util.PermissionErrorDialogFragment;
import com.example.beautydemo.util.PermissionUtils;
import com.example.beautydemo.widget.AspectFrameLayout;
import com.example.beautydemo.widget.CainSurfaceView;
import com.example.beautydemo.widget.HorizontalIndicatorView;
import com.example.beautydemo.widget.ShutterButton;
import com.smart.smartbeauty.api.SmartBeautyRender;
import com.smart.smartbeauty.api.SmartBeautyResource;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 相机预览页面
 */
public class CameraPreviewFragment extends Fragment implements View.OnClickListener,
        HorizontalIndicatorView.OnIndicatorListener {

    private static final String TAG = "CameraPreviewFragment";
    private static final boolean VERBOSE = true;

    private static final String FRAGMENT_DIALOG = "dialog";

    // 对焦大小
    private static final int FocusSize = 100;

    // 相机权限使能标志
    private boolean mCameraEnable = false;
    // 存储权限使能标志
    private boolean mStorageWriteEnable = false;
    // 是否需要等待录制完成再跳转
    private boolean mNeedToWaitStop = false;
    // 显示贴纸页面
    private boolean isShowingStickers = false;
    // 显示滤镜页面
    private boolean isShowingFilters = false;
    // 当前索引
    private int mFilterIndex = 0;

    // 处于延时拍照状态
    private boolean mDelayTaking = false;

    // 预览参数
    private CameraParam mCameraParam;

    // Fragment主页面
    private View mContentView;
    // 预览部分
    private AspectFrameLayout mAspectLayout;
    private CainSurfaceView mCameraSurfaceView;


    private Button mBtnSwitch;

    // 倒计时
    private TextView mCountDownView;
    // 贴纸按钮
    private Button mBtnStickers;
    // 快门按钮
    private ShutterButton mBtnShutter;
    // 滤镜按钮
    private Button mBtnEffect;
    // 视频删除按钮
    private Button mBtnRecordDelete;
    // 视频预览按钮
    private Button mBtnRecordPreview;


    // 主线程Handler
    private Handler mMainHandler;
    // 持有该Fragment的Activity，onAttach/onDetach中绑定/解绑，主要用于解决getActivity() = null的情况
    private Activity mActivity;

    // 贴纸资源页面
    private PreviewResourceFragment mResourcesFragment;
    // 滤镜页面
    private PreviewEffectFragment mEffectFragment;

    private SurfaceTexture mCameraSurfaceTexture = null;

    private String mActivationCode = "xx-xx-xx" ;

    public CameraPreviewFragment() {
        mCameraParam = CameraParam.getInstance();
    }

    public void setActivationCode(String code) {
        mActivationCode = code;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();

//        int currentMode = BrightnessUtils.getSystemBrightnessMode(mActivity);
//        if (currentMode == 1) {
//            mCameraParam.brightness = -1;
//        } else {
//            mCameraParam.brightness = BrightnessUtils.getSystemBrightness(mActivity);
//        }

        mMainHandler = new Handler(context.getMainLooper());
        mCameraEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.CAMERA);
        mStorageWriteEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

//        mCameraParam.audioPermitted = PermissionUtils.permissionChecking(mActivity, Manifest.permission.RECORD_AUDIO);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化资源文件
        initResources();
        // 初始化相机渲染引擎
        SmartBeautyRender.getInstance().initRenderer(mActivity);

    }

    /**
     * 初始化动态贴纸、滤镜等资源
     */
    private void initResources() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SmartBeautyResource.initResources(mActivity);
            }
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_camera_preview, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCameraEnable) {
            initView(mContentView);
        } else {
            requestCameraPermission();
        }
        initTracker();
    }

    /**
     * 初始化页面
     * @param view
     */
    private void initView(View view) {
        mAspectLayout = (AspectFrameLayout) view.findViewById(R.id.layout_aspect);
        mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
        mCameraSurfaceView = new CainSurfaceView(mActivity);

        mCameraSurfaceView.addMultiClickListener(mMultiClickListener);
        mAspectLayout.addView(mCameraSurfaceView);
        mAspectLayout.requestLayout();
        // 绑定需要渲染的SurfaceView
        SmartBeautyRender.getInstance().setSurfaceView(mCameraSurfaceView);
        SmartBeautyRender.getInstance().setRenderListener(mRenderListener);



        mBtnSwitch = (Button) view.findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);

        mCountDownView = (TextView) view.findViewById(R.id.tv_countdown);
        mBtnStickers = (Button) view.findViewById(R.id.btn_stickers);
        mBtnStickers.setOnClickListener(this);
        mBtnEffect = (Button) view.findViewById(R.id.btn_effects);
        mBtnEffect.setOnClickListener(this);



        mBtnShutter = (ShutterButton) view.findViewById(R.id.btn_shutter);
        mBtnShutter.setOnClickListener(this);

        mBtnRecordDelete = (Button) view.findViewById(R.id.btn_record_delete);
        mBtnRecordDelete.setOnClickListener(this);
        mBtnRecordPreview = (Button) view.findViewById(R.id.btn_record_preview);
        mBtnRecordPreview.setOnClickListener(this);

        adjustBottomView();
    }

    /**
     * 调整底部视图
     */
    private void adjustBottomView() {
        boolean result = mCameraParam.currentRatio < CameraParam.Ratio_4_3;
        mBtnStickers.setBackgroundResource(result ? R.drawable.ic_camera_sticker_light : R.drawable.ic_camera_sticker_dark);
        mBtnEffect.setBackgroundResource(result ? R.drawable.ic_camera_effect_light : R.drawable.ic_camera_effect_dark);
        mBtnRecordDelete.setBackgroundResource(result ? R.drawable.ic_camera_record_delete_light : R.drawable.ic_camera_record_delete_dark);
        mBtnRecordPreview.setBackgroundResource(result ? R.drawable.ic_camera_record_done_light : R.drawable.ic_camera_record_done_dark);
        mBtnShutter.setOuterBackgroundColor(result ? R.color.shutter_gray_light : R.color.shutter_gray_dark);
    }

    @Override
    public void onResume() {
        super.onResume();
//        registerHomeReceiver();
        mBtnShutter.setEnableOpened(false);
    }



    @Override
    public void onPause() {
        super.onPause();
//        unRegisterHomeReceiver();
        hideStickerView();
        hideEffectView();
        mBtnShutter.setEnableOpened(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContentView = null;
    }

    @Override
    public void onDestroy() {
        // 销毁人脸检测器
        releaseFaceTracker();

        // 关掉渲染引擎
        SmartBeautyRender.getInstance().destroyRenderer();


        super.onDestroy();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    /**
     * 处理返回事件
     * @return
     */
    public boolean onBackPressed() {
        if (isShowingFilters) {
            hideEffectView();
            return true;
        } else if (isShowingStickers) {
            hideStickerView();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.btn_switch) {
            switchCamera();
        } else if (i == R.id.btn_stickers) {
            showStickers();
        } else if (i == R.id.btn_effects) {
            showEffectView();
        } else if (i == R.id.btn_shutter) {
            takePicture();
        }
    }

    @Override
    public void onIndicatorChanged(int currentIndex) {
//        if (currentIndex == 0) {
//            mCameraParam.mGalleryType = GalleryType.GIF;
//            mBtnShutter.setIsRecorder(false);
//        } else if (currentIndex == 1) {
//            mCameraParam.mGalleryType = GalleryType.PICTURE;
//            // 拍照状态
//            mBtnShutter.setIsRecorder(false);
//            if (!mStorageWriteEnable) {
//                requestStoragePermission();
//            }
//        } else if (currentIndex == 2) {
//            mCameraParam.mGalleryType = GalleryType.VIDEO;
//            // 录制视频状态
//            mBtnShutter.setIsRecorder(true);
//            // 请求录音权限
//            if (!mCameraParam.audioPermitted) {
//                requestRecordSoundPermission();
//            }
//        }
//        // 显示时间
//        if (currentIndex == 2) {
//            mCountDownView.setVisibility(View.VISIBLE);
//        } else {
//            mCountDownView.setVisibility(View.GONE);
//        }
    }





    // --------------------------------- 相机操作逻辑 ----------------------------------------------

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (!mCameraEnable) {
            requestCameraPermission();
            return;
        }

        mCameraParam.backCamera = !mCameraParam.backCamera;
        if (mCameraParam.backCamera) {
            mCameraParam.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraParam.cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        openCamera();
        startPreview();
    }


    private SmartBeautyRender.ISmartRenderListener mRenderListener = new SmartBeautyRender.ISmartRenderListener() {
        @Override
        public Size onRenderCreated(SurfaceTexture cameraSurfaceTexture) {
            mCameraSurfaceTexture = cameraSurfaceTexture;
            openCamera();

            int textureWidth = 0;
            int textureHeight = 0;
            if (mCameraParam.orientation == 90 || mCameraParam.orientation == 270) {
                textureWidth = mCameraParam.previewHeight;
                textureHeight = mCameraParam.previewWidth;
            } else {
                textureWidth = mCameraParam.previewWidth;
                textureHeight = mCameraParam.previewHeight;
            }
            return new Size(textureWidth, textureHeight);
        }

        @Override
        public void onRenderFinish() {
            startPreview();
        }

        @Override
        public void onRenderDestroyed() {
            releaseCamera();
        }
    };


    /**
     * 打开相机
     */
    void openCamera() {
        releaseCamera();
        CameraEngine.getInstance().openCamera(mActivity);
        CameraEngine.getInstance().setPreviewSurface(mCameraSurfaceTexture);
        CameraEngine.getInstance().setPreviewCallbackWithBuffer(mCameraPreviewCallback);



        // 相机打开回调,开始人脸
        prepareTracker();
    }

    private Camera.PreviewCallback mCameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
                      // 人脸检测
            FaceTracker.getInstance().trackFace(data,
                    mCameraParam.previewWidth, mCameraParam.previewHeight);

            if (CameraParam.getInstance().previewBuffer  != null) {
                camera.addCallbackBuffer(CameraParam.getInstance().previewBuffer);
            }

             //请求刷新
            SmartBeautyRender.getInstance().requestRender();

        }
    };

    /**
     * 开始预览
     */
    private void startPreview() {
        CameraEngine.getInstance().startPreview();

    }

    /**
     * 释放相机
     */
    private void releaseCamera() {
        CameraEngine.getInstance().releaseCamera();
    }


    /**
     * 显示动态贴纸页面
     */
    private void showStickers() {
        isShowingStickers = true;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (mResourcesFragment == null) {
            mResourcesFragment = new PreviewResourceFragment();
            ft.add(R.id.fragment_container, mResourcesFragment);
        } else {
            ft.show(mResourcesFragment);
        }
        ft.commit();
        hideBottomLayout();
    }

    /**
     * 显示滤镜页面
     */
    private void showEffectView() {

        isShowingFilters = true;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (mEffectFragment == null) {
            mEffectFragment = new PreviewEffectFragment();
            ft.add(R.id.fragment_container, mEffectFragment);
        } else {
            ft.show(mEffectFragment);
        }
        ft.commit();
        mEffectFragment.scrollToCurrentFilter(mFilterIndex);
        hideBottomLayout();
    }

    /**
     * 隐藏动态贴纸页面
     */
    private void hideStickerView() {
        if (isShowingStickers) {
            isShowingStickers = false;
            if (mResourcesFragment != null) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.hide(mResourcesFragment);
                ft.commit();
            }
        }
        resetBottomLayout();
    }

    /**
     * 隐藏滤镜页面
     */
    private void hideEffectView() {

        if (isShowingFilters) {
            isShowingFilters = false;
            if (mEffectFragment != null) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.hide(mEffectFragment);
                ft.commit();
            }
        }
        resetBottomLayout();
    }

    /**
     * 隐藏底部布局按钮
     */
    private void hideBottomLayout() {
        mBtnEffect.setVisibility(View.GONE);
        mBtnStickers.setVisibility(View.GONE);
        ViewGroup.LayoutParams layoutParams = mBtnShutter.getLayoutParams();
        layoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                60, mActivity.getResources().getDisplayMetrics());
        layoutParams.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                60, mActivity.getResources().getDisplayMetrics());
        mBtnShutter.setLayoutParams(layoutParams);
    }

    /**
     * 恢复底部布局
     */
    private void resetBottomLayout() {
        mBtnShutter.setOuterBackgroundColor(mCameraParam.currentRatio < CameraParam.Ratio_4_3
                ? R.color.shutter_gray_light : R.color.shutter_gray_dark);
        ViewGroup.LayoutParams layoutParams = mBtnShutter.getLayoutParams();
        layoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                100, mActivity.getResources().getDisplayMetrics());
        layoutParams.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                100, mActivity.getResources().getDisplayMetrics());
        mBtnShutter.setLayoutParams(layoutParams);
        mBtnEffect.setVisibility(View.VISIBLE);
        mBtnStickers.setVisibility(View.VISIBLE);
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (mStorageWriteEnable) {
            SmartBeautyRender.getInstance().takeImage(new SmartBeautyRender.ITackImageCallback() {
                @Override
                public void onCaptured(final ByteBuffer buffer, final int width, final int height) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String filePath = PathConstraints.getImageCachePath(mActivity);
                            SmartBeautyResource.saveBitmap(filePath, buffer, width, height);

                        }
                    });
                }
            });
        } else {
            requestStoragePermission();
        }
    }


    /**
     * 单双击回调监听
     */
    private CainSurfaceView.OnMultiClickListener mMultiClickListener = new CainSurfaceView.OnMultiClickListener() {

        @Override
        public void onSurfaceSingleClick(final float x, final float y) {
            // 单击隐藏贴纸和滤镜页面
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    hideStickerView();
                    hideEffectView();
                }
            });

            // 如果处于触屏拍照状态，则直接拍照，不做对焦处理
            if (mCameraParam.touchTake) {
                takePicture();
                return;
            }

            // 判断是否支持对焦模式
            if (CameraEngine.getInstance().getCamera() != null) {
                List<String> focusModes = CameraEngine.getInstance().getCamera()
                        .getParameters().getSupportedFocusModes();
                if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    CameraEngine.getInstance().setFocusArea(CameraEngine.getFocusArea((int)x, (int)y,
                            mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight(), FocusSize));
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCameraSurfaceView.showFocusAnimation();
                        }
                    });
                }
            }
        }

        @Override
        public void onSurfaceDoubleClick(float x, float y) {
            switchCamera();
        }

    };



    /**
     * 初始化人脸检测器
     */
    private void initTracker() {
        FaceTracker.getInstance().setFaceCallback(mFaceTrackerCallback);

        FaceTracker.getInstance().initTracker();
    }

    /**
     * 销毁人脸检测器
     */
    private void releaseFaceTracker() {
        FaceTracker.getInstance().destroyTracker();
    }

    /**
     * 准备人脸检测器
     */
    private void prepareTracker() {
        FaceTracker.getInstance()
                .setBackCamera(mCameraParam.backCamera);

        FaceTracker.getInstance().prepareFaceTracker(mActivity, mActivationCode, mCameraParam.orientation,
                        mCameraParam.previewWidth, mCameraParam.previewHeight);
    }

    /**
     * 检测完成回调
     */
    private FaceTrackerCallback mFaceTrackerCallback = new FaceTrackerCallback() {
        @Override
            public void onTrackingFinish() {
                // 检测完成需要请求刷新
//                SmartBeautyRender.getInstance().requestRender();
        }
    };



    /**
     * 请求相机权限
     */
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_camera_permission), PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{ Manifest.permission.CAMERA},
                    PermissionUtils.REQUEST_CAMERA_PERMISSION);
        }
    }

    /**
     * 请求存储权限
     */
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionUtils.REQUEST_STORAGE_PERMISSION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_camera_permission), PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mCameraEnable = true;
                initView(mContentView);
            }
        } else if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mStorageWriteEnable = true;
            }
        } else if (requestCode == PermissionUtils.REQUEST_SOUND_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_sound_permission), PermissionUtils.REQUEST_SOUND_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mCameraParam.audioPermitted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }




















//    /**
//     * 注册服务
//     */
//    private void registerHomeReceiver() {
//        if (mActivity != null) {
//            IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//            mActivity.registerReceiver(mHomePressReceiver, homeFilter);
//        }
//    }

//    /**
//     * 注销服务
//     */
//    private void unRegisterHomeReceiver() {
//        if (mActivity != null) {
//            mActivity.unregisterReceiver(mHomePressReceiver);
//        }
//    }

//    /**
//     * Home按键监听服务
//     */
//    private BroadcastReceiver mHomePressReceiver = new BroadcastReceiver() {
//        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
//        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
//                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
//                if (TextUtils.isEmpty(reason)) {
//                    return;
//                }
//                // 当点击了home键时需要停止预览，防止后台一直持有相机
//                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
//                    // 停止录制
//                    if (PreviewRecorder.getInstance().isRecording()) {
//                        // 取消录制
//                        PreviewRecorder.getInstance().cancelRecording();
//                        // 重置进入条
//                        mBtnShutter.setProgress((int) PreviewRecorder.getInstance().getVisibleDuration());
//                        // 删除分割线
//                        mBtnShutter.deleteSplitView();
//                        // 关闭按钮
//                        mBtnShutter.closeButton();
//                        // 更新时间
//                        mCountDownView.setText(PreviewRecorder.getInstance().getVisibleDurationString());
//                    }
//                }
//            }
//        }
//    };

}
