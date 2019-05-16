package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;
import com.smart.smartbeauty.api.SmartBeautyRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

/**
 * 基类滤镜
 * Created by cain on 2017/7/9.
 */

public class GLImageAdjustFilter  extends GLImageFilter{
    private SmartBeautyRender.ITackImageCallback mImageCallback = null;
    private ByteBuffer mImageBuffer = null;

    public GLImageAdjustFilter(Context context) {
        super(context);
    }

    public void setImageCallback(SmartBeautyRender.ITackImageCallback callback) {
        mImageCallback = callback;
    }

    /**
     * 绘制图像
     */
    @Override
    protected void onDrawFrame() {
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mVertexCount);

        Log.e(TAG, "onDrawFrame...........");

        int width = mImageWidth;
        int height = mImageHeight;
        if (null == mImageBuffer) {
            mImageBuffer = ByteBuffer.allocateDirect(width * height * 4);
            mImageBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        mImageBuffer.position(0);

        GLES30.glReadPixels(0, 0, width, height,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, mImageBuffer);
        OpenGLUtils.checkGlError("glReadPixels");
        mImageBuffer.rewind();

        mImageCallback.onCaptured(mImageBuffer, width, height);
    }
}
