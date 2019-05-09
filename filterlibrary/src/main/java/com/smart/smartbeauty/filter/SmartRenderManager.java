package com.smart.smartbeauty.filter;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;


import com.badlogic.gdx.math.Vector3;
import com.cgfay.filterlibrary.glfilter.base.GLImageDepthBlurFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageOESInputFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageVignetteFilter;
import com.cgfay.filterlibrary.glfilter.beauty.GLImageBeautyFilter;
import com.cgfay.filterlibrary.glfilter.beauty.bean.BeautyParam;
import com.cgfay.filterlibrary.glfilter.beauty.bean.IBeautify;
import com.cgfay.filterlibrary.glfilter.color.GLImageDynamicColorFilter;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.face.GLImageFacePointsFilter;
import com.cgfay.filterlibrary.glfilter.face.GLImageFaceReshapeFilter;
import com.cgfay.filterlibrary.glfilter.makeup.GLImageMakeupFilter;
import com.cgfay.filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filterlibrary.glfilter.multiframe.GLImageFrameEdgeBlurFilter;
import com.cgfay.filterlibrary.glfilter.stickers.GLImageDynamicStickerFilter;
import com.cgfay.filterlibrary.glfilter.stickers.GestureHelp;
import com.cgfay.filterlibrary.glfilter.stickers.StaticStickerNormalFilter;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;
import com.cgfay.filterlibrary.landmark.LandmarkEngine;

import java.nio.FloatBuffer;

/**
 * 渲染管理器
 */
public final class SmartRenderManager {

    private static final String TAG = "SmartRenderManager";

    private static class RenderManagerHolder {
        public static SmartRenderManager instance = new SmartRenderManager();
    }

    private SmartRenderManager() {
        mBeautyParam = BeautyParam.getInstance();
    }

    public static SmartRenderManager getInstance() {
        return RenderManagerHolder.instance;
    }

    // 滤镜列表
    private SparseArray<GLImageFilter> mFilterArrays = new SparseArray<GLImageFilter>();

    // 坐标缓冲
    private SmartScaleType mScaleType = SmartScaleType.CENTER_CROP;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    // 用于显示裁剪的纹理顶点缓冲
    private FloatBuffer mDisplayVertexBuffer;
    private FloatBuffer mDisplayTextureBuffer;


    // 用于显示裁剪的纹理顶点缓冲
    private FloatBuffer mScaleVertexBuffer;
    private FloatBuffer mScaleTextureBuffer;

    // 视图宽高
    private int mViewWidth, mViewHeight;
    // 输入图像大小
    private int mTextureWidth, mTextureHeight;

    private int mOrientation;

    // 相机参数
    private BeautyParam mBeautyParam;
    // 上下文
    private Context mContext;

    private float mXScale = 1;
    private float mYScale = 1;

    /**
     * 初始化
     */
    public void init(Context context) {
        initBuffers();
        initFilters(context);
        mContext = context;
    }

    /**
     * 释放资源
     */
    public void release() {
        releaseBuffers();
        releaseFilters();
        mContext = null;
    }

    /**
     * 释放滤镜
     */
    private void releaseFilters() {
        for (int i = 0; i < mFilterArrays.size(); i++) {
            if (mFilterArrays.get(i) != null) {
                mFilterArrays.get(i).release();
            }
        }
        mFilterArrays.clear();
    }

    /**
     * 释放缓冲区
     */
    private void releaseBuffers() {
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
        if (mDisplayVertexBuffer != null) {
            mDisplayVertexBuffer.clear();
            mDisplayVertexBuffer = null;
        }
        if (mDisplayTextureBuffer != null) {
            mDisplayTextureBuffer.clear();
            mDisplayTextureBuffer = null;
        }


        if (mScaleVertexBuffer != null) {
            mScaleVertexBuffer.clear();
            mScaleVertexBuffer = null;
        }
        if (mScaleTextureBuffer != null) {
            mScaleTextureBuffer.clear();
            mScaleTextureBuffer = null;
        }
    }

    /**
     * 初始化缓冲区
     */
    private void initBuffers() {
        releaseBuffers();
        mDisplayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mDisplayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);


        mScaleVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mScaleTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    /**
     * 初始化滤镜
     * @param context
     */
    private void initFilters(Context context) {
        releaseFilters();
        // 相机输入滤镜
        mFilterArrays.put(SmartRenderIndex.CameraIndex, new GLImageOESInputFilter(context));
        // 美颜滤镜
        mFilterArrays.put(SmartRenderIndex.BeautyIndex, new GLImageBeautyFilter(context));
        // 彩妆滤镜
        mFilterArrays.put(SmartRenderIndex.MakeupIndex, new GLImageMakeupFilter(context, null));
        // 美型滤镜
        mFilterArrays.put(SmartRenderIndex.FaceAdjustIndex, new GLImageFaceReshapeFilter(context));
        // LUT/颜色滤镜
        mFilterArrays.put(SmartRenderIndex.FilterIndex, null);
        // 贴纸资源滤镜
        mFilterArrays.put(SmartRenderIndex.ResourceIndex, null);
        // 景深滤镜
        mFilterArrays.put(SmartRenderIndex.DepthBlurIndex, new GLImageDepthBlurFilter(context));
        // 暗角滤镜
        mFilterArrays.put(SmartRenderIndex.VignetteIndex, new GLImageVignetteFilter(context));
        // 显示输出
        mFilterArrays.put(SmartRenderIndex.DisplayIndex, new GLImageFilter(context));
        // 人脸关键点调试
        mFilterArrays.put(SmartRenderIndex.FacePointIndex, new GLImageFacePointsFilter(context));
    }

    /**
     * 是否切换边框模糊
     * @param enableEdgeBlur
     */
    public synchronized void changeEdgeBlurFilter(boolean enableEdgeBlur) {
        if (enableEdgeBlur) {
            mFilterArrays.get(SmartRenderIndex.DisplayIndex).release();
            GLImageFrameEdgeBlurFilter filter = new GLImageFrameEdgeBlurFilter(mContext);
            filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
            mFilterArrays.put(SmartRenderIndex.DisplayIndex, filter);
        } else {
            mFilterArrays.get(SmartRenderIndex.DisplayIndex).release();
            GLImageFilter filter = new GLImageFilter(mContext);
            filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
            mFilterArrays.put(SmartRenderIndex.DisplayIndex, filter);
        }
    }

    /**
     * 切换动态滤镜
     * @param color
     */
    public synchronized void changeDynamicFilter(DynamicColor color) {
        if (mFilterArrays.get(SmartRenderIndex.FilterIndex) != null) {
            mFilterArrays.get(SmartRenderIndex.FilterIndex).release();
            mFilterArrays.put(SmartRenderIndex.FilterIndex, null);
        }
        if (color == null) {
            return;
        }
        GLImageDynamicColorFilter filter = new GLImageDynamicColorFilter(mContext, color);
        filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        filter.initFrameBuffer(mTextureWidth, mTextureHeight);
        filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        mFilterArrays.put(SmartRenderIndex.FilterIndex, filter);
    }

    /**
     * 切换动态滤镜
     * @param dynamicMakeup
     */
    public synchronized void changeDynamicMakeup(DynamicMakeup dynamicMakeup) {
        if (mFilterArrays.get(SmartRenderIndex.MakeupIndex) != null) {
            ((GLImageMakeupFilter)mFilterArrays.get(SmartRenderIndex.MakeupIndex)).changeMakeupData(dynamicMakeup);
        } else {
            GLImageMakeupFilter filter = new GLImageMakeupFilter(mContext, dynamicMakeup);
            filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            filter.initFrameBuffer(mTextureWidth, mTextureHeight);
            filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
            mFilterArrays.put(SmartRenderIndex.MakeupIndex, filter);
        }
    }

    /**
     * 切换动态资源
     * @param color
     */
    public synchronized void changeDynamicResource(DynamicColor color) {
        if (mFilterArrays.get(SmartRenderIndex.ResourceIndex) != null) {
            mFilterArrays.get(SmartRenderIndex.ResourceIndex).release();
            mFilterArrays.put(SmartRenderIndex.ResourceIndex, null);
        }
        if (color == null) {
            return;
        }
        GLImageDynamicColorFilter filter = new GLImageDynamicColorFilter(mContext, color);
        filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        filter.initFrameBuffer(mTextureWidth, mTextureHeight);
        filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        mFilterArrays.put(SmartRenderIndex.ResourceIndex, filter);
    }

    /**
     * 切换动态资源
     * @param sticker
     */
    public synchronized void changeDynamicResource(DynamicSticker sticker) {
        // 释放旧滤镜
        if (mFilterArrays.get(SmartRenderIndex.ResourceIndex) != null) {
            mFilterArrays.get(SmartRenderIndex.ResourceIndex).release();
            mFilterArrays.put(SmartRenderIndex.ResourceIndex, null);
        }
        if (sticker == null) {
            return;
        }
        GLImageDynamicStickerFilter filter = new GLImageDynamicStickerFilter(mContext, sticker);
        // 设置输入输入大小，初始化fbo等
        filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        filter.initFrameBuffer(mTextureWidth, mTextureHeight);
        filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        mFilterArrays.put(SmartRenderIndex.ResourceIndex, filter);
    }

    /**
     * 绘制纹理
     * @param inputTexture
     * @param mMatrix
     * @return
     */
    public int drawFrame(int inputTexture, float[] mMatrix) {
        int currentTexture = inputTexture;
        if (mFilterArrays.get(SmartRenderIndex.CameraIndex) == null
                || mFilterArrays.get(SmartRenderIndex.DisplayIndex) == null) {
            return currentTexture;
        }
        if (mFilterArrays.get(SmartRenderIndex.CameraIndex) instanceof GLImageOESInputFilter) {
            ((GLImageOESInputFilter)mFilterArrays.get(SmartRenderIndex.CameraIndex)).setTextureTransformMatrix(mMatrix);
        }
        currentTexture = mFilterArrays.get(SmartRenderIndex.CameraIndex)
                .drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        // 如果处于对比状态，不做处理
        if (!mBeautyParam.showCompare) {
            // 美颜滤镜
            if (mFilterArrays.get(SmartRenderIndex.BeautyIndex) != null) {
                if (mFilterArrays.get(SmartRenderIndex.BeautyIndex) instanceof IBeautify
                        && mBeautyParam != null) {
                    ((IBeautify) mFilterArrays.get(SmartRenderIndex.BeautyIndex)).onBeauty(mBeautyParam);
                }
                currentTexture = mFilterArrays.get(SmartRenderIndex.BeautyIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 彩妆滤镜
            if (mFilterArrays.get(SmartRenderIndex.MakeupIndex) != null) {
                currentTexture = mFilterArrays.get(SmartRenderIndex.MakeupIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 美型滤镜
            if (mFilterArrays.get(SmartRenderIndex.FaceAdjustIndex) != null) {
                if (mFilterArrays.get(SmartRenderIndex.FaceAdjustIndex) instanceof IBeautify) {
                    ((IBeautify) mFilterArrays.get(SmartRenderIndex.FaceAdjustIndex)).onBeauty(mBeautyParam);
                }
                currentTexture = mFilterArrays.get(SmartRenderIndex.FaceAdjustIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 绘制颜色滤镜
            if (mFilterArrays.get(SmartRenderIndex.FilterIndex) != null) {
                currentTexture = mFilterArrays.get(SmartRenderIndex.FilterIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            long lastTime = System.currentTimeMillis();
            // 资源滤镜，可以是贴纸、滤镜甚至是彩妆类型
            if (mFilterArrays.get(SmartRenderIndex.ResourceIndex) != null && mBeautyParam.drawSticker) {
                currentTexture = mFilterArrays.get(SmartRenderIndex.ResourceIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }
            long now = System.currentTimeMillis();
//            Log.e(TAG, "consume = " + (now - lastTime));

            // 景深
            if (mFilterArrays.get(SmartRenderIndex.DepthBlurIndex) != null) {
                mFilterArrays.get(SmartRenderIndex.DepthBlurIndex).setFilterEnable(mBeautyParam.enableDepthBlur);
                currentTexture = mFilterArrays.get(SmartRenderIndex.DepthBlurIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 暗角
            if (mFilterArrays.get(SmartRenderIndex.VignetteIndex) != null) {
                mFilterArrays.get(SmartRenderIndex.VignetteIndex).setFilterEnable(mBeautyParam.enableVignette);
                currentTexture = mFilterArrays.get(SmartRenderIndex.VignetteIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }
        }

//        // 显示输出，需要调整视口大小
//        mFilterArrays.get(SmartRenderIndex.DisplayIndex).drawFrame(currentTexture, mDisplayVertexBuffer, mDisplayTextureBuffer);


        //TODO: huping add.
        // 显示输出，需要调整视口大小
        mFilterArrays.get(SmartRenderIndex.DisplayIndex).drawFrame(currentTexture, mScaleVertexBuffer, mScaleTextureBuffer);

        return currentTexture;
    }

    /**
     * 绘制调试用的人脸关键点
     * @param mCurrentTexture
     */
    public void drawFacePoint(int mCurrentTexture) {
        mBeautyParam.drawFacePoints = false;
        if (mFilterArrays.get(SmartRenderIndex.FacePointIndex) != null) {
            if (mBeautyParam.drawFacePoints && LandmarkEngine.getInstance().hasFace()) {
                mFilterArrays.get(SmartRenderIndex.FacePointIndex).drawFrame(mCurrentTexture, mDisplayVertexBuffer, mDisplayTextureBuffer);
            }
        }
    }

    /**
     * 设置输入纹理大小
     * @param width
     * @param height
     */
    public void setTextureSize(int width, int height, int orientation) {
        mTextureWidth = width;
        mTextureHeight = height;
        mOrientation = orientation;

    }

    /**
     * 设置纹理显示大小
     * @param width
     * @param height
     */
    public void setDisplaySize(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;

        //TODO: huping add.
        if(mOrientation == 0 && mTextureWidth > mTextureHeight) {
            mXScale = mViewWidth * mTextureHeight/mViewHeight;
            mXScale = (mTextureWidth - mXScale)/2/mTextureWidth;
        }


        adjustCoordinateSize();
        onFilterChanged();
    }

    /**
     * 调整滤镜
     */
    private void onFilterChanged() {
        for (int i = 0; i < mFilterArrays.size(); i++) {
            if (mFilterArrays.get(i) != null) {
                mFilterArrays.get(i).onInputSizeChanged(mTextureWidth, mTextureHeight);
                // 到显示之前都需要创建FBO，这里限定是防止创建多余的FBO，节省GPU资源
                if (i < SmartRenderIndex.DisplayIndex) {
                    mFilterArrays.get(i).initFrameBuffer(mTextureWidth, mTextureHeight);
                }
                mFilterArrays.get(i).onDisplaySizeChanged(mViewWidth, mViewHeight);
            }
        }
    }

    /**
     * 调整由于surface的大小与SurfaceView大小不一致带来的显示问题
     */
    private void adjustCoordinateSize() {
        Log.e(TAG, "mViewWidth == " + mViewWidth + ", mViewHeight == "  + mViewHeight);
        Log.e(TAG, "mTextureWidth == " + mTextureWidth + ", mTextureHeight == "  + mTextureHeight);
        Log.e(TAG, "mX == " + mXScale);


        float[] textureCoord = null;
        float[] vertexCoord = null;
        float[] textureVertices = TextureRotationUtils.TextureVertices;
        float[] vertexVertices = TextureRotationUtils.CubeVertices;


//        //TODO: huping add.
//        textureCoord = new float[] {
//                textureVertices[0] + mXScale, textureVertices[1],
//                textureVertices[2] - mXScale, textureVertices[3],
//                textureVertices[4] + mXScale, textureVertices[5],
//                textureVertices[6] - mXScale, textureVertices[7],
//        };
//





//        float ratioMax = Math.max((float) mViewWidth / mTextureWidth,
//                (float) mViewHeight / mTextureHeight);
//        // 新的宽高
//        int imageWidth = Math.round(mTextureWidth * ratioMax);
//        int imageHeight = Math.round(mTextureHeight * ratioMax);
//        // 获取视图跟texture的宽高比
//        float ratioWidth = (float) imageWidth / (float) mViewWidth;
//        float ratioHeight = (float) imageHeight / (float) mViewHeight;
//        if (mScaleType == SmartScaleType.CENTER_INSIDE) {
//            vertexCoord = new float[] {
//                    vertexVertices[0] / ratioHeight, vertexVertices[1] / ratioWidth, vertexVertices[2],
//                    vertexVertices[3] / ratioHeight, vertexVertices[4] / ratioWidth, vertexVertices[5],
//                    vertexVertices[6] / ratioHeight, vertexVertices[7] / ratioWidth, vertexVertices[8],
//                    vertexVertices[9] / ratioHeight, vertexVertices[10] / ratioWidth, vertexVertices[11],
//            };
//        } else if (mScaleType == SmartScaleType.CENTER_CROP) {
//            float distHorizontal = (1 - 1 / ratioWidth) / 2;
//            float distVertical = (1 - 1 / ratioHeight) / 2;
//            textureCoord = new float[] {
//                    addDistance(textureVertices[0], distVertical), addDistance(textureVertices[1], distHorizontal),
//                    addDistance(textureVertices[2], distVertical), addDistance(textureVertices[3], distHorizontal),
//                    addDistance(textureVertices[4], distVertical), addDistance(textureVertices[5], distHorizontal),
//                    addDistance(textureVertices[6], distVertical), addDistance(textureVertices[7], distHorizontal),
//            };
//        }
        if (vertexCoord == null) {
            vertexCoord = vertexVertices;
        }
        if (textureCoord == null) {
            textureCoord = textureVertices;
        }
        // 更新VertexBuffer 和 TextureBuffer
        mDisplayVertexBuffer.clear();
        mDisplayVertexBuffer.put(vertexCoord).position(0);
        mDisplayTextureBuffer.clear();
        mDisplayTextureBuffer.put(textureCoord).position(0);


        adjustScaleCoordinateSize();
    }


    /**
     * 调整由于surface的大小与SurfaceView大小不一致带来的显示问题
     */
    private void adjustScaleCoordinateSize() {
        Log.e(TAG, "mViewWidth == " + mViewWidth + ", mViewHeight == "  + mViewHeight);
        Log.e(TAG, "mTextureWidth == " + mTextureWidth + ", mTextureHeight == "  + mTextureHeight);
        Log.e(TAG, "mX == " + mXScale);


        float[] textureCoord = null;
        float[] vertexCoord = null;
        float[] textureVertices = TextureRotationUtils.TextureVertices;
        float[] vertexVertices = TextureRotationUtils.CubeVertices;

        textureCoord = new float[] {
                textureVertices[0] + mXScale, textureVertices[1],
                textureVertices[2] - mXScale, textureVertices[3],
                textureVertices[4] + mXScale, textureVertices[5],
                textureVertices[6] - mXScale, textureVertices[7],
        };


        if (vertexCoord == null) {
            vertexCoord = vertexVertices;
        }
        if (textureCoord == null) {
            textureCoord = textureVertices;
        }
        // 更新VertexBuffer 和 TextureBuffer
        mScaleVertexBuffer.clear();
        mScaleVertexBuffer.put(vertexCoord).position(0);
        mScaleTextureBuffer.clear();
        mScaleTextureBuffer.put(textureCoord).position(0);
    }



    /**
     * 计算距离
     * @param coordinate
     * @param distance
     * @return
     */
    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public static final Vector3 tempVec=new Vector3();
    public StaticStickerNormalFilter touchDown(MotionEvent e) {

        if (mFilterArrays.get(SmartRenderIndex.ResourceIndex) != null) {
          GLImageFilter  glImageFilter = mFilterArrays.get(SmartRenderIndex.ResourceIndex);
          if(glImageFilter instanceof GLImageDynamicStickerFilter) {
              GLImageDynamicStickerFilter glImageDynamicStickerFilter= (GLImageDynamicStickerFilter) glImageFilter;
              tempVec.set(e.getX(), e.getY(), 0);
              StaticStickerNormalFilter staticStickerNormalFilter=GestureHelp.hit(tempVec,glImageDynamicStickerFilter.getmFilters());
              if(staticStickerNormalFilter!=null){
                  Log.d("touchSticker","找到贴纸");
              }else{
                  Log.d("touchSticker","没有贴纸");
              }
              return staticStickerNormalFilter;
          }
        }

        return null;

    }
}
