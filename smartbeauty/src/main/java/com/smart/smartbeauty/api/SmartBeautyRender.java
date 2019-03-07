package com.smart.smartbeauty.api;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filterlibrary.glfilter.resource.FilterHelper;
import com.cgfay.filterlibrary.glfilter.resource.MakeupHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.smart.smartbeauty.render.SmartRenderHandler;
import com.smart.smartbeauty.render.SmartRenderThread;

import java.lang.ref.WeakReference;

/**
 * Created by deepglint on 2019/3/1.
 */

public class SmartBeautyRender{

    private ISmartRenderListener mListener = null;

    // 渲染Handler
    private SmartRenderHandler mRenderHandler = null;

    // 渲染线程
    private SmartRenderThread mPreviewRenderThread = null;

    // 操作锁
    private final Object mSynOperation = new Object();

    private WeakReference<SurfaceView> mWeakSurfaceView;
    /**
     * 单例
     */
    private SmartBeautyRender() {
    }



    private static class SmartBeautyHolder {
        private static SmartBeautyRender instance = new SmartBeautyRender();
    }

    public static SmartBeautyRender getInstance() {
        return SmartBeautyHolder.instance;
    }


    public void setRenderListener(ISmartRenderListener listener) {
        mListener = listener;

        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.setListener(new SmartRenderThread.ISmartRenderThreadListener() {
                @Override
                public void onSurfaceFinish(SurfaceTexture surfaceTexture) {
                    if (mListener != null) {
                        mListener.onRenderFinish(surfaceTexture);
                    }
                }

                @Override
                public void onSurfaceDestroyed() {
                    if (mListener != null) {
                        mListener.onRenderDestroyed();
                    }
                }
            });
        }
    }


    /**
     * 初始化渲染器
     */
    public void initRenderer(Context context) {
        synchronized (mSynOperation) {
            mPreviewRenderThread = new SmartRenderThread(context, "martRenderThread");
            mPreviewRenderThread.start();

            mRenderHandler = new SmartRenderHandler(mPreviewRenderThread);
            // 绑定Handler
            mPreviewRenderThread.setRenderHandler(mRenderHandler);
        }
    }

    /**
     * 销毁渲染器
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void destroyRenderer() {
        synchronized (mSynOperation) {
            if (mWeakSurfaceView != null) {
                mWeakSurfaceView.clear();
                mWeakSurfaceView = null;
            }
            if (mRenderHandler != null) {
                mRenderHandler.removeCallbacksAndMessages(null);
                mRenderHandler = null;
            }
            if (mPreviewRenderThread != null) {
                mPreviewRenderThread.quitSafely();
                try {
                    mPreviewRenderThread.join();
                } catch (InterruptedException e) {

                }
                mPreviewRenderThread = null;
            }
        }
    }


    /**
     * 绑定需要渲染的SurfaceView
     * @param surfaceView
     */
    public void setSurfaceView(SurfaceView surfaceView) {
        mWeakSurfaceView = new WeakReference<>(surfaceView);
        surfaceView.getHolder().addCallback(mSurfaceCallback);
    }


    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mRenderHandler != null) {
                mRenderHandler.sendMessage(mRenderHandler
                        .obtainMessage(SmartRenderHandler.MSG_SURFACE_CREATED, holder));
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mRenderHandler != null) {
                mRenderHandler.sendMessage(mRenderHandler
                        .obtainMessage(SmartRenderHandler.MSG_SURFACE_CHANGED, width, height));
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mRenderHandler != null) {
                mRenderHandler.sendMessage(mRenderHandler
                        .obtainMessage(SmartRenderHandler.MSG_SURFACE_DESTROYED));
            }
        }

    };

    /**
     * 绘制帧
     */
    public void drawFrame() {
        if (mRenderHandler != null) {
            mRenderHandler.removeMessages(SmartRenderHandler.MSG_RENDER);
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(SmartRenderHandler.MSG_RENDER));

        }
    }

    /**
     * 切换动态资源
     * @param color
     */
    protected void changeDynamicResource(DynamicColor color) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(SmartRenderHandler.MSG_CHANGE_DYNAMIC_RESOURCE, color));
        }
    }

    /**
     * 切换动态资源
     * @param sticker
     */
    protected void changeDynamicResource(DynamicSticker sticker) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(SmartRenderHandler.MSG_CHANGE_DYNAMIC_RESOURCE, sticker));
        }
    }

    /**
     * 切换彩妆
     * @param makeup
     */
    protected void changeDynamicMakeup(DynamicMakeup makeup) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(SmartRenderHandler.MSG_CHANGE_DYNAMIC_MAKEUP, makeup));
        }
    }

    /**
     * 切换滤镜
     * @param color
     */
    protected void changeDynamicFilter(DynamicColor color) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(SmartRenderHandler.MSG_CHANGE_DYNAMIC_COLOR, color));
        }
    }


    public interface ISmartRenderListener {
        void onRenderFinish(SurfaceTexture cameraSurfaceTexture);
        void onRenderDestroyed();
    }

}
