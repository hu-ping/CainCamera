package com.smart.smartbeauty.thread;

import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.smart.smartbeauty.api.SmartBeautyRender;

import java.lang.ref.WeakReference;

/**
 * 预览渲染Handler
 * Created by cain.huang on 2017/11/3.
 */

public class SmartRenderHandler extends Handler {

    // Surface创建
    public static final int MSG_SURFACE_CREATED = 0x001;
    // Surface改变
    public static final int MSG_SURFACE_CHANGED = 0x002;
    // Surface销毁
    public static final int MSG_SURFACE_DESTROYED = 0x003;
    // 渲染
    public static final int MSG_RENDER = 0x004;
    // 开始录制
    public static final int MSG_START_RECORDING = 0x006;
    // 停止录制
    public static final int MSG_STOP_RECORDING = 0x008;
    // 重新打开相机
    public static final int MSG_REOPEN_CAMERA = 0x009;
    // 切换相机
    public static final int MSG_SWITCH_CAMERA = 0x010;
    // 预览帧回调
    public static final int MSG_PREVIEW_CALLBACK = 0x011;
    // 拍照
    public static final int MSG_TAKE_IMAGE = 0x012;
    // 计算fps
    public static final int MSG_CALCULATE_FPS = 0x013;
    // 切换边框模糊功能
    public static final int MSG_CHANGE_EDGE_BLUR = 0x14;
    // 切换动态滤镜
    public static final int MSG_CHANGE_DYNAMIC_COLOR = 0x15;
    // 切换动态彩妆
    public static final int MSG_CHANGE_DYNAMIC_MAKEUP = 0x16;
    // 切换动态动态资源
    public static final int MSG_CHANGE_DYNAMIC_RESOURCE = 0x17;


    public static final int MSG_SET_TEXTURE_SIZE = 0x18;

    public static final int MSG_DRAW_STICKER = 0x19;


    private WeakReference<SmartRenderThread> mWeakRenderThread;

    public SmartRenderHandler(SmartRenderThread thread) {
        super(thread.getLooper());
        mWeakRenderThread = new WeakReference<SmartRenderThread>(thread);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mWeakRenderThread == null || mWeakRenderThread.get() == null) {
            return;
        }
        SmartRenderThread thread = mWeakRenderThread.get();
        switch (msg.what) {

            // surfaceCreated
            case MSG_SURFACE_CREATED:
                thread.surfaceCreated((SurfaceHolder)msg.obj);
                break;

            // surfaceChanged
            case MSG_SURFACE_CHANGED:
                thread.surfaceChanged(msg.arg1, msg.arg2);
                break;

            // surfaceDestroyed;
            case MSG_SURFACE_DESTROYED:
                thread.surfaceDestroyed();
                break;

            // 帧可用（考虑同步的问题）
            case MSG_RENDER:
                thread.drawFrame();
                break;

//            // 开始录制
//            case MSG_START_RECORDING:
//                thread.startRecording();
//                break;
//
//            // 停止录制
//            case MSG_STOP_RECORDING:
//                thread.stopRecording();
//                break;

//            // 重新打开相机
//            case MSG_REOPEN_CAMERA:
//                thread.openCamera();
//                break;
//
//            // 切换相机
//            case MSG_SWITCH_CAMERA:
//                thread.switchCamera();
//                break;
//
//            // 预览帧回调
//            case MSG_PREVIEW_CALLBACK:
//                thread.onPreviewCallback((byte[])msg.obj);
//                break;

            // 拍照
            case MSG_TAKE_IMAGE:
                thread.takeImage((SmartBeautyRender.ITackImageCallback)msg.obj);
                break;

//            // 计算fps
//            case MSG_CALCULATE_FPS:
//                thread.calculateFps();
//                break;

            // 切换边框模糊
            case MSG_CHANGE_EDGE_BLUR:
                thread.changeEdgeBlurFilter((boolean)msg.obj);
                break;

            // 切换动态滤镜
            case MSG_CHANGE_DYNAMIC_COLOR: {
                thread.changeDynamicFilter((DynamicColor) msg.obj);
                break;
            }

            // 切换动态彩妆
            case MSG_CHANGE_DYNAMIC_MAKEUP: {
                if (msg.obj == null) {
                    thread.changeDynamicMakeup((DynamicMakeup) null);
                } else {
                    thread.changeDynamicMakeup((DynamicMakeup) msg.obj);
                }
                break;
            }

            // 切换动态贴纸
            case MSG_CHANGE_DYNAMIC_RESOURCE:
                if (msg.obj == null) {
                    thread.changeDynamicResource((DynamicSticker) null);
                } else if (msg.obj instanceof DynamicColor) {
                    thread.changeDynamicResource((DynamicColor) msg.obj);
                } else if (msg.obj instanceof DynamicSticker) {
                    thread.changeDynamicResource((DynamicSticker) msg.obj);
                }
                break;

            case MSG_DRAW_STICKER:
                thread.switchDrawSticker((boolean)msg.obj);
                break;

            default:
                throw new IllegalStateException("Can not handle message what is: " + msg.what);
        }
    }
}
