package com.smart.smartbeauty.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES30;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.cgfay.filterlibrary.gles.EglCore;
import com.cgfay.filterlibrary.gles.WindowSurface;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filterlibrary.glfilter.stickers.StaticStickerNormalFilter;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.smart.smartbeauty.filter.SmartRenderManager;

import java.nio.ByteBuffer;

/**
 * 渲染线程
 * Created by cain on 2017/11/4.
 */

public class SmartRenderThread extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener,
        Camera.PreviewCallback {

    private static final String TAG = "SmartRenderThread";
    private static final boolean VERBOSE = false;

    // 操作锁
    private final Object mSynOperation = new Object();
    // 更新帧的锁
    private final Object mSyncFrameNum = new Object();
    private final Object mSyncFence = new Object();

    private boolean isPreviewing = false;       // 是否预览状态
    private boolean isRecording = false;        // 是否录制状态
    private boolean isRecordingPause = false;   // 是否处于暂停录制状态

    // EGL共享上下文
    private EglCore mEglCore;
    // 预览用的EGLSurface
    private WindowSurface mDisplaySurface;

    private int mInputTexture;
    private int mCurrentTexture;
    private SurfaceTexture mSurfaceTexture;

    // 矩阵
    private final float[] mMatrix = new float[16];

    // 预览回调
    private byte[] mPreviewBuffer;
    // 输入图像大小
    private int mTextureWidth, mTextureHeight;

    // 可用帧
    private int mFrameNum = 0;

    // 渲染Handler回调
    private SmartRenderHandler mRenderHandler;

//    // 计算帧率
//    private FrameRateMeter mFrameRateMeter;

    // 上下文
    private Context mContext;

    // 正在拍照
    private volatile boolean mTakingPicture;

    // 渲染管理器
    private SmartRenderManager mRenderManager;


    private ISmartRenderThreadListener mListener = null;

    public SmartRenderThread(Context context, String name) {
        super(name);
        mContext = context;
        mRenderManager = SmartRenderManager.getInstance();
    }

    /**
     * 设置预览Handler回调
     * @param handler
     */
    public void setRenderHandler(SmartRenderHandler handler) {
        mRenderHandler = handler;
    }

    public void setListener(ISmartRenderThreadListener listener)m {
        mListener = listener;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    /**
     * Surface创建
     * @param holder
     */
    void surfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        //TODO: huping. mDisplaySurface使用CainSurfaceView的holder创建的，用于显示图像。
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDisable(GLES30.GL_CULL_FACE);

        // 渲染器初始化
        mRenderManager.init(mContext);

        //TODO: huping.  mSurfaceTexture使用OESTexture创建用于将摄像头采集的图像渲染到其上面。
        mInputTexture = OpenGLUtils.createOESTexture();
        mSurfaceTexture = new SurfaceTexture(mInputTexture);
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    /**
     * Surface改变
     * @param width
     * @param height
     */
    void surfaceChanged(int width, int height) {
        mRenderManager.setDisplaySize(width, height);

        if(mListener != null) {
            mListener.onSurfaceFinish(mSurfaceTexture);
        }
    }

    /**
     * Surface销毁
     */
    void surfaceDestroyed() {
        mTakingPicture = false;
        mRenderManager.release();

        if(mListener != null) {
            mListener.onSurfaceDestroyed();
        }

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mDisplaySurface != null) {
            mDisplaySurface.release();
            mDisplaySurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    /**
     * 绘制帧
     */
    void drawFrame() {
        // 如果存在新的帧，则更新帧
        synchronized (mSyncFrameNum) {
            synchronized (mSyncFence) {
                if (mSurfaceTexture != null) {
                    while (mFrameNum != 0) {
                        mSurfaceTexture.updateTexImage();
                        --mFrameNum;
                    }
                } else {
                    return;
                }
            }
        }

        // 切换渲染上下文
        mDisplaySurface.makeCurrent();
        mSurfaceTexture.getTransformMatrix(mMatrix);

        // 绘制渲染
        mCurrentTexture = mRenderManager.drawFrame(mInputTexture, mMatrix);

        // 是否绘制人脸关键点
        mRenderManager.drawFacePoint(mCurrentTexture);

        // 显示到屏幕
        mDisplaySurface.swapBuffers();

//        // 执行拍照
//        if (mCameraParam.isTakePicture && !mTakingPicture) {
//            synchronized (mSyncFence) {
//                mTakingPicture = true;
//                mRenderHandler.sendEmptyMessage(SmartRenderHandler.MSG_TAKE_PICTURE);
//            }
//        }
//
//        // 是否处于录制状态
//        if (isRecording && !isRecordingPause) {
//            HardcodeEncoder.getInstance().frameAvailable();
//            HardcodeEncoder.getInstance()
//                    .drawRecorderFrame(mCurrentTexture, mSurfaceTexture.getTimestamp());
//        }
    }




    /**
     * 拍照
     */
    void takePicture() {
        synchronized (mSyncFence) {
            ByteBuffer buffer = mDisplaySurface.getCurrentFrame();
            mCameraParam.captureCallback.onCapture(buffer,
                    mDisplaySurface.getWidth(), mDisplaySurface.getHeight());
            mTakingPicture = false;
            mCameraParam.isTakePicture = false;
        }
    }

//    /**
//     * 计算fps
//     */
//    void calculateFps() {
//        // 帧率回调
//        if ((mCameraParam).fpsCallback != null) {
//            mFrameRateMeter.drawFrameCount();
//            (mCameraParam).fpsCallback.onFpsCallback(mFrameRateMeter.getFPS());
//        }
//    }


    /**
     * 切换边框模糊
     * @param enableEdgeBlur
     */
    void changeEdgeBlurFilter(boolean enableEdgeBlur) {
        synchronized (mSynOperation) {
            mRenderManager.changeEdgeBlurFilter(enableEdgeBlur);
        }
    }

    /**
     * 切换动态滤镜
     * @param color
     */
    void changeDynamicFilter(DynamicColor color) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicFilter(color);
        }
    }

    /**
     * 切换动态彩妆
     * @param makeup
     */
    void changeDynamicMakeup(DynamicMakeup makeup) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicMakeup(makeup);
        }
    }

    /**
     * 切换动态资源
     * @param color
     */
    void changeDynamicResource(DynamicColor color) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicResource(color);
        }
    }

    /**
     * 切换动态资源
     * @param sticker
     */
    void changeDynamicResource(DynamicSticker sticker) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicResource(sticker);
        }
    }

//    /**
//     * 开始录制
//     */
//    void startRecording() {
//        if (mEglCore != null) {
//            // 设置渲染Texture 的宽高
//            HardcodeEncoder.getInstance().setTextureSize(mTextureWidth, mTextureHeight);
//            // 这里将EGLContext传递到录制线程共享。
//            // 由于EGLContext是当前线程手动创建，也就是OpenGLES的main thread
//            // 这里需要传自己手动创建的EglContext
//            HardcodeEncoder.getInstance().startRecording(mContext, mEglCore.getEGLContext());
//        }
//        isRecording = true;
//    }
//
//    /**
//     * 停止录制
//     */
//    void stopRecording() {
//        HardcodeEncoder.getInstance().stopRecording();
//        isRecording = false;
//    }

//    /**
//     * 请求刷新
//     */
//    public void requestRender() {
//        synchronized (mSyncFrameNum) {
//            if (isPreviewing) {
//                ++mFrameNum;
//                if (mRenderHandler != null) {
//                    mRenderHandler.removeMessages(SmartRenderHandler.MSG_RENDER);
//                    mRenderHandler.sendMessage(mRenderHandler
//                            .obtainMessage(SmartRenderHandler.MSG_RENDER));
//                }
//            }
//        }
//    }



    public interface ISmartRenderThreadListener {
        void onSurfaceFinish(SurfaceTexture surfaceTexture);
        void onSurfaceDestroyed();
    }



    public StaticStickerNormalFilter touchDown(MotionEvent e) {
        synchronized (mSyncFrameNum) {
            if (mRenderManager != null) {
               return mRenderManager.touchDown(e);
            }
        }
        return null;
    }
}
