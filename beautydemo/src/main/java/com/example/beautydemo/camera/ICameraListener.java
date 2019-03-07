package com.example.beautydemo.camera;

import java.nio.ByteBuffer;

/**
 * Created by deepglint on 2019/3/4.
 */

public interface ICameraListener {
    // 相机已打开
    void onCameraOpened();

    // 预览回调
    void onPreviewCallback(byte[] data);

    // 截帧回调
    void onCaptured(ByteBuffer buffer, int width, int height);

}
