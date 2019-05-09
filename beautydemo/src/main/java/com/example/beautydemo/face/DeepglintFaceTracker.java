package com.example.beautydemo.face;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deepglint.hri.face.FaceSDKManager;
import com.deepglint.hri.facesdk.FaceAttributes;
import com.deepglint.hri.facesdk.FaceInfo;
import com.deepglint.hri.facesdk.FaceSDKOptions;
import com.smart.smartbeauty.api.SmartLandmark;
import com.smart.smartbeauty.api.SmartOneFace;
import com.smart.smartbeauty.math.SmartLeastSquares;
import com.smart.smartbeauty.math.SmartSplineInterpolator;

import org.opencv.core.Core;
import org.opencv.core.Mat;


/**
 * 人脸检测器
 */
public  class DeepglintFaceTracker implements IFaceTracker {

    private static final String TAG = "DeepglintFaceTracker";
    private static final boolean VERBOSE = false;

    private final Object mSyncFence = new Object();

    // 人脸检测参数
    private FaceTrackParam mFaceTrackParam;

    // 检测线程
    private TrackerThread mTrackerThread;

    public boolean isBackCamera = false;

    private static class FaceTrackerHolder {
        private static DeepglintFaceTracker instance = new DeepglintFaceTracker();
    }

    private DeepglintFaceTracker() {
        mFaceTrackParam = FaceTrackParam.getInstance();
    }

    public static DeepglintFaceTracker getInstance() {
        return FaceTrackerHolder.instance;
    }

    /**
     * 检测回调
     * @param callback
     * @return
     */
    @Override
    public void setFaceCallback(FaceTrackerCallback callback) {
        FaceTrackParam.getInstance().trackerCallback = callback;
    }

    /**
     * 准备检测器
     */
    @Override
    public void initTracker(Context context) {
        synchronized (mSyncFence) {
            mTrackerThread = new TrackerThread("FaceTrackerThread");
            mTrackerThread.start();
        }
    }

    /**
     * 初始化人脸检测
     * @param context       上下文
     * @param orientation   图像角度
     * @param width         图像宽度
     * @param height        图像高度
     */
    @Override
    public void prepareFaceTracker(Context context, String code, int orientation, int width, int height) {
        synchronized (mSyncFence) {
            if (mTrackerThread != null) {
                mTrackerThread.prepareFaceTracker(context, code, orientation, width, height);
            }
        }
    }

    /**
     * 检测人脸
     * @param data
     * @param width
     * @param height
     */
    @Override
    public void trackFace(byte[] data, int width, int height) {
        synchronized (mSyncFence) {
            if (mTrackerThread != null) {
                mTrackerThread.trackFace(data, width, height);
            }
        }
    }

    /**
     * 销毁检测器
     */
    @Override
    public void destroyTracker() {
        synchronized (mSyncFence) {
            mTrackerThread.quitSafely();
        }
    }

    /**
     * 是否后置摄像头
     * @param backCamera
     * @return
     */
    @Override
    public void setBackCamera(boolean backCamera) {
        mFaceTrackParam.isBackCamera = backCamera;
    }

    /**
     * 是否允许3D姿态角
     * @param enable
     * @return
     */
    public DeepglintFaceTracker enable3DPose(boolean enable) {
        mFaceTrackParam.enable3DPose = enable;
        return this;
    }

    /**
     * 是否允许区域检测
     * @param enable
     * @return
     */
    public DeepglintFaceTracker enableROIDetect(boolean enable) {
        mFaceTrackParam.enableROIDetect = enable;
        return this;
    }

    /**
     * 是否允许106个关键点
     * @param enable
     * @return
     */
    public DeepglintFaceTracker enable106Points(boolean enable) {
        mFaceTrackParam.enable106Points = enable;
        return this;
    }

    /**
     * 是否允许多人脸检测
     * @param enable
     * @return
     */
    public DeepglintFaceTracker enableMultiFace(boolean enable) {
        mFaceTrackParam.enableMultiFace = enable;
        return this;
    }

    /**
     * 是否允许人脸年龄检测
     * @param enable
     * @return
     */
    public DeepglintFaceTracker enableFaceProperty(boolean enable) {
        mFaceTrackParam.enableFaceProperty = enable;
        return this;
    }

    /**
     * 最小检测人脸大小
     * @param size
     * @return
     */
    public DeepglintFaceTracker minFaceSize(int size) {
        mFaceTrackParam.minFaceSize = size;
        return this;
    }

    /**
     * 检测时间间隔
     * @param interval
     * @return
     */
    public DeepglintFaceTracker detectInterval(int interval) {
        mFaceTrackParam.detectInterval = interval;
        return this;
    }

    /**
     * 检测模式
     * @param mode
     * @return
     */
    public DeepglintFaceTracker trackMode(int mode) {
//        mFaceTrackParam.trackMode = mode;
        return this;
    }

//    /**
//     * Face++SDK联网请求验证
//     */
//    public static void requestFaceNetwork(Context context) {
//        if (Facepp.getSDKAuthType(ConUtil.getFileContent(context, R.raw
//                .megviifacepp_0_4_7_model)) == 2) {// 非联网授权
//            FaceTrackParam.getInstance().canFaceTrack = true;
//            return;
//        }
//        final LicenseManager licenseManager = new LicenseManager(context);
//        licenseManager.setExpirationMillis(Facepp.getApiExpirationMillis(context,
//                ConUtil.getFileContent(context, R.raw.megviifacepp_0_4_7_model)));
//        String uuid = ConUtil.getUUIDString(context);
//        long apiName = Facepp.getApiName();
//        licenseManager.setAuthTimeBufferMillis(0);
//        licenseManager.takeLicenseFromNetwork(uuid, FaceppConstraints.API_KEY, FaceppConstraints.API_SECRET, apiName,
//                LicenseManager.DURATION_30DAYS, "Landmark", "1", true,
//                new LicenseManager.TakeLicenseCallback() {
//                    @Override
//                    public void onSuccess() {
//                        if (VERBOSE) {
//                            Log.d(TAG, "success to register license!");
//                        }
//                        FaceTrackParam.getInstance().canFaceTrack = true;
//                    }
//
//                    @Override
//                    public void onFailed(int i, byte[] bytes) {
//                        if (VERBOSE) {
//                            Log.d(TAG, "Failed to register license!");
//                        }
//                        FaceTrackParam.getInstance().canFaceTrack = false;
//                    }
//                });
//    }


    /**
     * 检测线程
     */
    private static class TrackerThread extends Thread {

        // 人脸检测实体
//        private Facepp facepp;
        // 传感器监听器
//        private SensorEventUtil mSensorUtil;

        private FaceSDKManager faceSDKManager = null;
        private int cameraRotation = 0;

        private Looper mLooper;
        private @Nullable Handler mHandler;

        public TrackerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (this) {
                mLooper = Looper.myLooper();
                notifyAll();
                mHandler = new Handler(mLooper);
            }
            Looper.loop();
            synchronized (this) {
                release();
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
        }

        /**
         * 安全退出
         * @return
         */
        public boolean quitSafely() {
            Looper looper = getLooper();
            if (looper != null) {
                looper.quitSafely();
                return true;
            }
            return false;
        }

        /**
         * 获取Looper
         * @return
         */
        public Looper getLooper() {
            if (!isAlive()) {
                return null;
            }
            synchronized (this) {
                while (isAlive() && mLooper == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            return mLooper;
        }

        /**
         * 获取线程Handler
         * @return
         */
        public Handler getThreadHandler() {
            return mHandler;
        }

        /**
         * 初始化人脸检测
         * @param context       上下文
         * @param orientation   图像角度
         * @param width         图像宽度
         * @param height        图像高度
         */
        public void prepareFaceTracker(final Context context, final String code,
                                       final int orientation,
                                       final int width, final int height) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    internalPrepareFaceTracker(context, code , orientation, width, height);
                }
            });
        }

        /**
         * 检测人脸
         * @param data      图像数据， NV21 或者 RGBA格式
         * @param width     图像宽度
         * @param height    图像高度
         * @return          是否检测成功
         */
        public void trackFace(final byte[] data, final int width, final int height) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    internalTrackFace(data, width, height);
                }
            });
        }


        /**
         * 释放资源
         */
        private void release() {
            FaceSDKManager.getInstance().release();

            // 清理关键点
            SmartLandmark.getInstance().clearAll();
        }


        private synchronized void internalPrepareFaceTracker(Context context, String code, int orientation, int width, int height) {
            cameraRotation = orientation;

            FaceSDKOptions options = new FaceSDKOptions();
            options.detectorOptions.minTrackingSize = 30;
            options.detectorOptions.minConfidence = 0.7f;
            FaceSDKManager.getInstance().init(
                    context,
                    code,
                    options
            );

            int status = FaceSDKManager.getInstance().getActivationStatus();
            if (status == 0) {
                Log.i(TAG, "初始化成功");
            } else if(status == -1 || status == -2){
                Log.e(TAG,"初始化失败，请检查网络状态和激活码有效日期");
            } else if(status == -3) {
                Log.i(TAG,"初始化失败，请检查模型");
            }

            faceSDKManager = FaceSDKManager.getInstance();
        }

        static int faceIndex = 0;
        static int faceCaptureIndex = 0;
        private ImageFrame frame = new ImageFrame();
        private synchronized void internalTrackFace(byte[] data, int width, int height) {
            FaceTrackParam faceTrackParam = FaceTrackParam.getInstance();

            frame.setRawData(data, width, height);
            Mat image = frame.getBgrMat();
            faceTrackParam.isBackCamera = false;


            if(cameraRotation == 0) {
//               Core.transpose(image, image);
               Core.flip(image, image, 1);
            } else if(cameraRotation == 90) {
//                Core.transpose(image, image);
//                Core.flip(image, image, 1);

                Core.transpose(image, image);
                Core.flip(image, image, 0);
            } else if(cameraRotation == 180) {
                Core.flip(image, image, 0);
                Core.flip(image, image, 1);
            } else if(cameraRotation == 270) {
//                Core.transpose(image, image);
//                Core.flip(image, image, 0);


                Core.transpose(image, image);
                Core.flip(image, image, 1);
            }

            long now = System.currentTimeMillis();
            FaceInfo[] faceInfos = faceSDKManager.getTrackingFaces(image, System.currentTimeMillis());



            int rotation = 0;
            if (cameraRotation == 0) {         // 0
                rotation = 3;
            } else if (cameraRotation == 90) {  // 90
                rotation = 0;
            } else if (cameraRotation == 270) {  // 270
                rotation = 1;
            } else if (cameraRotation == 180) {  // 180
                rotation = 2;
            }

            // 设置旋转方向
//            SmartLandmark.getInstance().setOrientation(cameraRotation);
            SmartLandmark.getInstance().setOrientation(rotation);
            // 设置是否需要翻转
            SmartLandmark.getInstance().setNeedFlip(faceTrackParam.previewTrack && faceTrackParam.isBackCamera);

//            Log.e(TAG, "cameraRotation == " + cameraRotation
//                    + ", previewTrack = " + faceTrackParam.previewTrack
//                    + ", isBackCamera = " + faceTrackParam.isBackCamera);

            // 计算人脸关键点
            if (faceInfos != null && faceInfos.length > 0) {
                for (int index = 0; index < faceInfos.length; index++) {
                    // 关键点个数
//                    if (faceTrackParam.enable106Points) {
//                        facepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK106);
//                    } else {
//                        facepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK81);
//                    }
//                    // 获取姿态角信息
//                    if (faceTrackParam.enable3DPose) {
//                        facepp.get3DPose(faces[index]);
//                    }

                    FaceInfo face = faceInfos[index];

                    SmartOneFace oneFace = SmartLandmark.getInstance().getOneFace(index);
//                    // 是否检测性别年龄属性
//                    if (faceTrackParam.enableFaceProperty) {
//                        facepp.getAgeGender(face);
//                        oneFace.gender = face.female > face.male ? OneFace.GENDER_WOMAN
//                                : OneFace.GENDER_MAN;
//                        oneFace.age = Math.max(face.age, 1);
//                    } else {
//                        oneFace.gender = -1;
//                        oneFace.age = -1;
//                    }

                    if(image == null) {
                        return;
                    }
                    Mat faceImage = new Mat();
                    faceSDKManager.alignFace(image, face, faceImage);
                    FaceAttributes attributes = new FaceAttributes();
                    FaceSDKManager.ErrorCode code = faceSDKManager.generateAttributes(
                            faceImage,
                            attributes
                    );

                    if(attributes.gender == 0) {
                        oneFace.gender = SmartOneFace.GENDER_MAN;
                    }else if(attributes.gender == 1){
                        oneFace.gender = SmartOneFace.GENDER_WOMAN;
                    }else{
                        oneFace.gender = -1;
                    }

                    if(attributes.age > 0) {
                        oneFace.age = attributes.age;
                    }else{
                        oneFace.age = -1;
                    }


                    // 姿态角和置信度
                    float[] orientation = face.orientation;
                    oneFace.yaw = orientation[0];
                    oneFace.pitch = orientation[1];
                    oneFace.roll = orientation[2];

//                    Log.e(TAG, "yaw = " + oneFace.yaw + ", pitch = " + oneFace.pitch + ", roll = " + oneFace.roll);

//                    if(oneFace.yaw > 40 || oneFace.yaw < -40) {
//                        SmartLandmark.getInstance().clearAll();
//                        return;
//                    }
//                    if(oneFace.pitch > 20 || oneFace.pitch < -20) {
//                        SmartLandmark.getInstance().clearAll();
//                        return;
//                    }
//                    if(oneFace.roll > 40 || oneFace.roll < -40) {
//                        SmartLandmark.getInstance().clearAll();
//                        return;
//                    }

//                    if (faceTrackParam.previewTrack) {
//
//                        if (faceTrackParam.isBackCamera) {
//                            oneFace.roll = oneFace.roll;
//                        } else {
//                            oneFace.roll = -oneFace.roll;
//                        }
//                    }

                    oneFace.confidence = face.confidence;

//                    if (faceTrackParam.previewTrack) {
//
//                        if (faceTrackParam.isBackCamera) {
//                            oneFace.roll = (float) (Math.PI / 2.0f + oneFace.roll);
//                        } else {
//                            oneFace.roll = (float) (Math.PI / 2.0f - oneFace.roll);
//                        }
//                    }

                    // 预览状态下，宽高交换
//                    if (faceTrackParam.previewTrack) {
//                        if (cameraRotation == 90 || cameraRotation == 270) {
//                            int temp = width;
//                            width = height;
//                            height = temp;
//                        }
//                    }

                    // 获取一个人的关键点坐标
                    if (oneFace.vertexPoints == null || oneFace.vertexPoints.length != face.landmarks.length * 2) {
                        oneFace.vertexPoints = new float[212];
                    }



                    long lastTime = System.currentTimeMillis();
                    float[] updateLandmarks  = convertTo106(face);
//                    float[] updateLandmarks  = face.landmarks.clone();
                    long nowTime = System.currentTimeMillis();
//                    Log.e(TAG, "statistics internalTrackFace  convertTo106 consume = " + (nowTime - lastTime));


                    image = null;
                    for (int i = 0; i <  updateLandmarks.length/2; i++) {
//                        Log.e(TAG, i + " == x:" +  updateLandmarks[i * 2] + ", y:" +  updateLandmarks[i * 2 + 1]);

//                        // orientation = 0、3 表示竖屏，1、2 表示横屏
//                        float x = ( updateLandmarks[i * 2] / height) * 2 - 1;
//                        float y = ( updateLandmarks[i * 2 + 1] /width ) * 2 - 1;
//                        float[] point = new float[] {x, -y};


                        //TODO: huping add.
                        // orientation = 0、3 表示竖屏，1、2 表示横屏
                        float x = ( updateLandmarks[i * 2] /width ) * 2 - 1;
                        float y = ( updateLandmarks[i * 2 + 1] /height) * 2 - 1;
                        float[] point = new float[] {x, -y};




//                        if (cameraRotation == 180) {
//                            if (faceTrackParam.previewTrack && faceTrackParam.isBackCamera) {
//                                point[0] = -y;
//                                point[1] = -x;
//                            } else {
//                                point[0] = y;
//                                point[1] = x;
//                            }
//                        } else if (cameraRotation == 270) {
//                            if (faceTrackParam.previewTrack && faceTrackParam.isBackCamera) {
//                                point[0] = y;
//                                point[1] = x;
//                            } else {
//                                point[0] = -y;
//                                point[1] = -x;
//                            }
//                        } else if (cameraRotation == 90) {
//                            point[0] = -x;
//                            point[1] = y;
//                        }



                        // 顶点坐标
                        if (faceTrackParam.previewTrack) {
                            if (faceTrackParam.isBackCamera) {
                                oneFace.vertexPoints[2 * i] = point[0];
                            } else {
                                oneFace.vertexPoints[2 * i] = -point[0];
                            }
                        } else { // 非预览状态下，左右不需要翻转
                            oneFace.vertexPoints[2 * i] = point[0];
                        }
                        oneFace.vertexPoints[2 * i + 1] = point[1];
                    }

//                    Log.e(TAG, "faceIndex == " + faceIndex++ + ", landmark size == " + oneFace.vertexPoints.length);
//                    for(int i = 0; i < oneFace.vertexPoints.length; i += 2) {
//                        Log.e(TAG, (i/2) + "== [" + oneFace.vertexPoints[i]
//                                + "," + oneFace.vertexPoints[i + 1]  + "]");
//                    }



                    captureFrameCount++;
                    long currentTime = System.currentTimeMillis();
                    if(nextCaptureStatisticsTime == -1) {
                        nextCaptureStatisticsTime = currentTime + UNIT_TIME_INTERVAL;
                    }
                    if(trackFaceInitTime == -1) {
                        trackFaceInitTime = currentTime;
                    }
                    if(currentTime > nextCaptureStatisticsTime) {
//                        Log.e(TAG,"statistics internalTrackFace frame count:" + captureFrameCount);
//                        Log.e(TAG,"statistics internalTrackFace frame average delay :"
//                                + ((currentTime - trackFaceInitTime) / captureFrameCount) + "ms");
                        nextCaptureStatisticsTime = currentTime + UNIT_TIME_INTERVAL;
                        captureFrameCount = 0;
                        trackFaceInitTime = currentTime;
                    }


                    // 插入人脸对象
                    SmartLandmark.getInstance().putOneFace(index, oneFace);

                }
            }



            // 设置人脸个数
            SmartLandmark.getInstance().setFaceSize(faceInfos!= null ? faceInfos.length : 0);
            // 检测完成回调
            if (faceTrackParam.trackerCallback != null) {
                faceTrackParam.trackerCallback.onTrackingFinish();
            }

        }
        static int captureFrameCount = 0;
        static double nextCaptureStatisticsTime = -1;
        static double UNIT_TIME_INTERVAL = 1000;
        static long trackFaceInitTime = -1;
        static double nextCapturePreprocessingStatisticsTime = -1;



        private float[] convertTo106(FaceInfo face) {
            float[] updateLandmarks = new float[212];


            //脸颊
            double[] xArr = new double[13];
            double[] yArr = new double[13];

            for (int i = 0; i < 13; i++) {
                xArr[i] = face.landmarks[i * 2];
                yArr[i] = face.landmarks[i * 2 + 1];
            }

//            for (int i = 0; i < 13; i++) {
//                Log.i(TAG, i + " == x:" + xArr[i] + ", y:" + yArr[i]);
//            }


//            for(int i = 0; i < 30; i++) {
//                updateLandmarks[i * 2] = (float) xArr[0] + 10 * i;
//                updateLandmarks[i * 2 + 1] = (float) SmartLeastSquares.getY(xArr[0] + 10 * i);
//            }

            SmartSplineInterpolator.initSplineInterpolator(xArr, yArr, 13);
            SmartLeastSquares.initCoefficients(xArr, yArr, 13);


            //脸颊(左)
            updateLandmarks[0 * 2] = (float) xArr[0];
            updateLandmarks[0 * 2 + 1] = (float) yArr[0];

            double distance = xArr[1] - xArr[0];
            double average = distance / 3;
            double yDistance = yArr[1] - yArr[0];
            double yAverage = yDistance / 3;
            updateLandmarks[1 * 2] = (float) (updateLandmarks[0 * 2] + average);
            updateLandmarks[1 * 2 + 1] = (float) (updateLandmarks[0 * 2 + 1] + yAverage);

            updateLandmarks[2 * 2] = (float) (updateLandmarks[1 * 2] + average);
            updateLandmarks[2 * 2 + 1] = (float) (updateLandmarks[1 * 2 + 1] + yAverage);

            updateLandmarks[3 * 2] = (float) xArr[1];
            updateLandmarks[3 * 2 + 1] = (float) yArr[1];

            updateLandmarks[4 * 2] = (float)(updateLandmarks[3 * 2] + average);
            updateLandmarks[4 * 2 + 1] = (float) (updateLandmarks[3 * 2 + 1] + yAverage);

            updateLandmarks[5 * 2] = (float)xArr[2];
            updateLandmarks[5 * 2 + 1] = (float) yArr[2];


            distance = xArr[3] - xArr[2];
            average = distance / 3;
            yDistance = yArr[3] - yArr[2];
            yAverage = yDistance / 3;
            updateLandmarks[6 * 2] = (float)(updateLandmarks[5 * 2] + average);
            updateLandmarks[6 * 2 + 1] = (float) (updateLandmarks[5 * 2 + 1] + yAverage);

            updateLandmarks[7 * 2] = (float) (updateLandmarks[6 * 2] + average);
            updateLandmarks[7 * 2 + 1] = (float) (updateLandmarks[6 * 2 + 1] + yAverage);

            updateLandmarks[8 * 2] = (float)xArr[3];
            updateLandmarks[8 * 2 + 1] = (float) yArr[3];


            distance = xArr[4] - xArr[3];
            average = distance / 3;
            yDistance = yArr[4] - yArr[3];
            yAverage = yDistance / 3;
            updateLandmarks[9 * 2] = (float)(updateLandmarks[8 * 2] + average);
            updateLandmarks[9 * 2 + 1] = (float) (updateLandmarks[8 * 2 + 1] + yAverage);

            updateLandmarks[10 * 2] = (float)(updateLandmarks[9 * 2] + average);
            updateLandmarks[10 * 2 + 1] = (float) (updateLandmarks[9 * 2 + 1] + yAverage);

            updateLandmarks[11 * 2] = (float)xArr[4];
            updateLandmarks[11 * 2 + 1] = (float) yArr[4];


            distance = xArr[5] - xArr[4];
            average = distance / 2;
            yDistance = yArr[5] - yArr[4];
            yAverage = yDistance / 2;
            updateLandmarks[12 * 2] = (float)(updateLandmarks[11 * 2] + average);
            updateLandmarks[12 * 2 + 1] = (float) (updateLandmarks[11 * 2 + 1] + yAverage);

            updateLandmarks[13 * 2] = (float)xArr[5];
            updateLandmarks[13 * 2 + 1] = (float) yArr[5];


            distance = xArr[6] - xArr[5];
            average = distance / 3;
            yDistance = yArr[6] - yArr[5];
            yAverage = yDistance / 3;
            updateLandmarks[14 * 2] = (float)(updateLandmarks[13 * 2] + average);
            updateLandmarks[14 * 2 + 1] = (float) (updateLandmarks[13 * 2 + 1] + yAverage);

            updateLandmarks[15 * 2] = (float)(updateLandmarks[14 * 2] + average);
            updateLandmarks[15 * 2 + 1] = (float) (updateLandmarks[14 * 2 + 1] + yAverage);

            updateLandmarks[16 * 2] = (float) xArr[6];
            updateLandmarks[16 * 2 + 1] = (float)yArr[6];


            //脸颊(右)
            distance = xArr[7] - xArr[6];
            average = distance / 3;
            yDistance = yArr[7] - yArr[6];
            yAverage = yDistance / 3;
            updateLandmarks[17 * 2] = (float) (updateLandmarks[16 * 2] + average);
            updateLandmarks[17 * 2 + 1] = (float) (updateLandmarks[16 * 2 + 1] + yAverage);

            updateLandmarks[18 * 2] = (float) (updateLandmarks[17 * 2] + average);
            updateLandmarks[18 * 2 + 1] = (float) (updateLandmarks[17 * 2 + 1] + yAverage);

            updateLandmarks[19 * 2] = (float) xArr[7];
            updateLandmarks[19 * 2 + 1] = (float) yArr[7];


            distance = xArr[8] - xArr[7];
            average = distance / 2;
            yDistance = yArr[8] - yArr[7];
            yAverage = yDistance / 2;
            updateLandmarks[20 * 2] = (float)(updateLandmarks[19 * 2] + average);
            updateLandmarks[20 * 2 + 1] = (float) (updateLandmarks[19 * 2 + 1] + yAverage);

            updateLandmarks[21 * 2] = (float)xArr[8];
            updateLandmarks[21 * 2 + 1] = (float) yArr[8];


            distance = xArr[9] - xArr[8];
            average = distance / 3;
            yDistance = yArr[9] - yArr[8];
            yAverage = yDistance / 3;
            updateLandmarks[22 * 2] = (float)(updateLandmarks[21 * 2] + average);
            updateLandmarks[22 * 2 + 1] = (float) (updateLandmarks[21 * 2 + 1] + yAverage);

            updateLandmarks[23 * 2] = (float) (updateLandmarks[22 * 2] + average);
            updateLandmarks[23 * 2 + 1] = (float) (updateLandmarks[22 * 2 + 1] + yAverage);

            updateLandmarks[24 * 2] = (float)xArr[9];
            updateLandmarks[24 * 2 + 1] = (float) yArr[9];


            distance = xArr[10] - xArr[9];
            average = distance / 3;
            yDistance = yArr[10] - yArr[9];
            yAverage = yDistance / 3;
            updateLandmarks[25 * 2] = (float)(updateLandmarks[24 * 2] + average);
            updateLandmarks[25 * 2 + 1] = (float) (updateLandmarks[24 * 2 + 1] + yAverage);

            updateLandmarks[26 * 2] = (float)(updateLandmarks[25 * 2] + average);
            updateLandmarks[26 * 2 + 1] = (float) (updateLandmarks[25 * 2 + 1] + yAverage);


            updateLandmarks[27 * 2] = (float)xArr[10];
            updateLandmarks[27 * 2 + 1] = (float) yArr[10];

            updateLandmarks[28 * 2] = (float)(updateLandmarks[27 * 2] + average);
            updateLandmarks[28 * 2 + 1] = (float) (updateLandmarks[27 * 2 + 1] + yAverage);

            updateLandmarks[29 * 2] = (float)xArr[11];
            updateLandmarks[29 * 2 + 1] = (float) yArr[11];


            distance = xArr[12] - xArr[11];
            average = distance / 3;
            yDistance = yArr[12] - yArr[11];
            yAverage = yDistance / 3;
            updateLandmarks[30 * 2] = (float)(updateLandmarks[29 * 2] + average);
            updateLandmarks[30 * 2 + 1] = (float) (updateLandmarks[29 * 2 + 1] + yAverage);

            updateLandmarks[31 * 2] = (float)(updateLandmarks[30 * 2] + average);
            updateLandmarks[31 * 2 + 1] = (float) (updateLandmarks[30 * 2 + 1] + yAverage);

            updateLandmarks[32 * 2] = (float)xArr[12];
            updateLandmarks[32 * 2 + 1] = (float)yArr[12];


            //眉毛上沿
            for (int i = 33, j = 22; i < 38; i++, j++) {
                updateLandmarks[i * 2] = face.landmarks[j * 2];
                updateLandmarks[i * 2 + 1] = face.landmarks[j * 2 + 1];
            }

            for (int i = 38, j = 39; i < 43; i++, j++) {
                updateLandmarks[i * 2] = face.landmarks[j * 2];
                updateLandmarks[i * 2 + 1] = face.landmarks[j * 2 + 1];
            }


            //鼻子中央
            distance = face.landmarks[38 * 2] + face.landmarks[21 * 2];
            average = distance / 2;
            updateLandmarks[43 * 2] = face.landmarks[57 * 2];
            distance = face.landmarks[38 * 2 + 1] + face.landmarks[21 * 2 + 1];
            average = distance / 2;
            updateLandmarks[43 * 2 + 1] = (float) average;

            distance = face.landmarks[57 * 2 + 1] - updateLandmarks[43 * 2 + 1];
            average = distance / 3;
            updateLandmarks[44 * 2] = face.landmarks[57 * 2];
            updateLandmarks[44 * 2 + 1] = (float) (updateLandmarks[43 * 2 + 1] + average);
            updateLandmarks[45 * 2] = face.landmarks[57 * 2];
            updateLandmarks[45 * 2 + 1] = (float) (updateLandmarks[44 * 2 + 1] + average);
            updateLandmarks[46 * 2] = face.landmarks[57 * 2];
            updateLandmarks[46 * 2 + 1] = face.landmarks[57 * 2 + 1];


            //鼻子两翼
            updateLandmarks[78 * 2] = face.landmarks[47 * 2];
            updateLandmarks[78 * 2 + 1] = face.landmarks[47 * 2 + 1];
            updateLandmarks[79 * 2] = face.landmarks[56 * 2];
            updateLandmarks[79 * 2 + 1] = face.landmarks[56 * 2 + 1];
            updateLandmarks[80 * 2] = face.landmarks[49 * 2];
            updateLandmarks[80 * 2 + 1] = face.landmarks[49 * 2 + 1];
            updateLandmarks[81 * 2] = face.landmarks[54 * 2];
            updateLandmarks[81 * 2 + 1] = face.landmarks[54 * 2 + 1];
            updateLandmarks[82 * 2] = face.landmarks[50 * 2];
            updateLandmarks[82 * 2 + 1] = face.landmarks[50 * 2 + 1];
            updateLandmarks[83 * 2] = face.landmarks[53 * 2];
            updateLandmarks[83 * 2 + 1] = face.landmarks[53 * 2 + 1];


            //鼻子下沿
            distance = face.landmarks[52 * 2] - face.landmarks[51 * 2];
            average = distance / 4;
            updateLandmarks[47 * 2] = face.landmarks[51 * 2];
            updateLandmarks[47 * 2 + 1] = face.landmarks[51 * 2 + 1];

            updateLandmarks[48 * 2] = (float) (updateLandmarks[47 * 2] + average);
            updateLandmarks[48 * 2 + 1] = face.landmarks[51 * 2 + 1] + 2;

            updateLandmarks[49 * 2] = (float) (updateLandmarks[48 * 2] + average);
            updateLandmarks[49 * 2 + 1] = face.landmarks[51 * 2 + 1] + 3;

            updateLandmarks[50 * 2] = (float) (updateLandmarks[49 * 2] + average);
            updateLandmarks[50 * 2 + 1] = face.landmarks[51 * 2 + 1] + 2;

            updateLandmarks[51 * 2] = face.landmarks[52 * 2];
            updateLandmarks[51 * 2 + 1] = face.landmarks[52 * 2 + 1];


            //左眼
            updateLandmarks[52 * 2] = face.landmarks[13 * 2];
            updateLandmarks[52 * 2 + 1] = face.landmarks[13 * 2 + 1];
            updateLandmarks[53 * 2] = face.landmarks[14 * 2];
            updateLandmarks[53 * 2 + 1] = face.landmarks[14 * 2 + 1];
            updateLandmarks[54 * 2] = face.landmarks[16 * 2];
            updateLandmarks[54 * 2 + 1] = face.landmarks[16 * 2 + 1];
            updateLandmarks[55 * 2] = face.landmarks[17 * 2];
            updateLandmarks[55 * 2 + 1] = face.landmarks[17 * 2 + 1];
            updateLandmarks[56 * 2] = face.landmarks[18 * 2];
            updateLandmarks[56 * 2 + 1] = face.landmarks[18 * 2 + 1];
            updateLandmarks[57 * 2] = face.landmarks[20 * 2];
            updateLandmarks[57 * 2 + 1] = face.landmarks[20 * 2 + 1];
            updateLandmarks[72 * 2] = face.landmarks[15 * 2];
            updateLandmarks[72 * 2 + 1] = face.landmarks[15 * 2 + 1];
            updateLandmarks[73 * 2] = face.landmarks[19 * 2];
            updateLandmarks[73 * 2 + 1] = face.landmarks[19 * 2 + 1];
            updateLandmarks[74 * 2] = face.landmarks[21 * 2];
            updateLandmarks[74 * 2 + 1] = face.landmarks[21 * 2 + 1];
            updateLandmarks[104 * 2] = face.landmarks[21 * 2];
            updateLandmarks[104 * 2 + 1] = face.landmarks[21 * 2 + 1];


            //右眼
            updateLandmarks[58 * 2] = face.landmarks[30 * 2];
            updateLandmarks[58 * 2 + 1] = face.landmarks[30 * 2 + 1];
            updateLandmarks[59 * 2] = face.landmarks[31 * 2];
            updateLandmarks[59 * 2 + 1] = face.landmarks[31 * 2 + 1];
            updateLandmarks[60 * 2] = face.landmarks[33 * 2];
            updateLandmarks[60 * 2 + 1] = face.landmarks[33 * 2 + 1];
            updateLandmarks[61 * 2] = face.landmarks[34 * 2];
            updateLandmarks[61 * 2 + 1] = face.landmarks[34 * 2 + 1];
            updateLandmarks[62 * 2] = face.landmarks[35 * 2];
            updateLandmarks[62 * 2 + 1] = face.landmarks[35 * 2 + 1];
            updateLandmarks[63 * 2] = face.landmarks[37 * 2];
            updateLandmarks[63 * 2 + 1] = face.landmarks[37 * 2 + 1];
            updateLandmarks[75 * 2] = face.landmarks[32 * 2];
            updateLandmarks[75 * 2 + 1] = face.landmarks[32 * 2 + 1];
            updateLandmarks[76 * 2] = face.landmarks[36 * 2];
            updateLandmarks[76 * 2 + 1] = face.landmarks[36 * 2 + 1];
            updateLandmarks[77 * 2] = face.landmarks[38 * 2];
            updateLandmarks[77 * 2 + 1] = face.landmarks[38 * 2 + 1];
            updateLandmarks[105 * 2] = face.landmarks[38 * 2];
            updateLandmarks[105 * 2 + 1] = face.landmarks[38 * 2 + 1];



            //眉毛下沿
            for (int i = 64, j = 29; i < 68; i++, j--) {
                updateLandmarks[i * 2] = face.landmarks[j * 2];
                updateLandmarks[i * 2 + 1] = face.landmarks[j * 2 + 1];
            }
            for (int i = 68, j = 46; i < 72; i++, j--) {
                updateLandmarks[i * 2] = face.landmarks[j * 2];
                updateLandmarks[i * 2 + 1] = face.landmarks[j * 2 + 1];
            }


            //上嘴唇上沿
            updateLandmarks[84 * 2] = face.landmarks[58 * 2];
            updateLandmarks[84 * 2 + 1] = face.landmarks[58 * 2 + 1];
            updateLandmarks[85 * 2] = face.landmarks[59 * 2];
            updateLandmarks[85 * 2 + 1] = face.landmarks[59 * 2 + 1];
            updateLandmarks[86 * 2] = (face.landmarks[59 * 2] + face.landmarks[60 * 2]) / 2;
            updateLandmarks[86 * 2 + 1] = (face.landmarks[59 * 2 + 1] + face.landmarks[60 * 2 + 1]) / 2;
            updateLandmarks[87 * 2] = face.landmarks[60 * 2];
            updateLandmarks[87 * 2 + 1] = face.landmarks[60 * 2 + 1];
            updateLandmarks[88 * 2] = (face.landmarks[61 * 2] + face.landmarks[60 * 2]) / 2;
            updateLandmarks[88 * 2 + 1] = (face.landmarks[61 * 2 + 1] + face.landmarks[60 * 2 + 1]) / 2;
            updateLandmarks[89 * 2] = face.landmarks[61 * 2];
            updateLandmarks[89 * 2 + 1] = face.landmarks[61 * 2 + 1];
            updateLandmarks[90 * 2] = face.landmarks[62 * 2];
            updateLandmarks[90 * 2 + 1] = face.landmarks[62 * 2 + 1];


            //下嘴唇下沿
            distance = face.landmarks[62 * 2] - face.landmarks[58 * 2];
            average = distance / 6;
            updateLandmarks[91 * 2] = (float) (updateLandmarks[90 * 2] - average);
            updateLandmarks[92 * 2] = (float) (updateLandmarks[90 * 2] - average * 2);
            updateLandmarks[93 * 2] = face.landmarks[64 * 2];
            updateLandmarks[94 * 2] = (float) (updateLandmarks[90 * 2] - average * 4);
            updateLandmarks[95 * 2] = (float) (updateLandmarks[90 * 2] - average * 5);

            distance = face.landmarks[64 * 2 + 1] - face.landmarks[62 * 2 + 1];
            average = distance / 3;
            updateLandmarks[91 * 2 + 1] = (float) (updateLandmarks[90 * 2 + 1] + average);
            updateLandmarks[92 * 2 + 1] = (float) (updateLandmarks[90 * 2 + 1] + average * 2);
            updateLandmarks[93 * 2 + 1] = face.landmarks[64 * 2 + 1];
            updateLandmarks[94 * 2 + 1] = (float) (updateLandmarks[90 * 2 + 1] + average * 2);
            updateLandmarks[95 * 2 + 1] = (float) (updateLandmarks[90 * 2 + 1] + average);


            //上嘴唇下沿
            updateLandmarks[96 * 2] = face.landmarks[58 * 2];
            updateLandmarks[96 * 2 + 1] = face.landmarks[58 * 2 + 1];
            updateLandmarks[97 * 2] = face.landmarks[66 * 2];
            updateLandmarks[97 * 2 + 1] = face.landmarks[66 * 2 + 1];
            updateLandmarks[98 * 2] = face.landmarks[67 * 2];
            updateLandmarks[98 * 2 + 1] = face.landmarks[67 * 2 + 1];
            updateLandmarks[99 * 2] = face.landmarks[68 * 2];
            updateLandmarks[99 * 2 + 1] = face.landmarks[68 * 2 + 1];
            updateLandmarks[100 * 2] = face.landmarks[62 * 2];
            updateLandmarks[100 * 2 + 1] = face.landmarks[62 * 2 + 1];

            //下嘴唇上沿
            for (int i = 101, j = 69; i < 104; i++, j++) {
                updateLandmarks[i * 2] = face.landmarks[j * 2];
                updateLandmarks[i * 2 + 1] = face.landmarks[j * 2 + 1];
            }

            return updateLandmarks;
        }




        /**
         * 初始化人脸检测
         * @param context       上下文
         * @param orientation   图像角度，预览时设置相机的角度，如果是静态图片，则为0
         * @param width         图像宽度
         * @param height        图像高度
         */
//        private synchronized void internalPrepareFaceTracker(Context context, int orientation, int width, int height) {
//            FaceTrackParam faceTrackParam = FaceTrackParam.getInstance();
//            if (!faceTrackParam.canFaceTrack) {
//                return;
//            }
//            release();
//            facepp = new Facepp();
//            if (mSensorUtil == null) {
//                mSensorUtil = new SensorEventUtil(context);
//            }
//            ConUtil.acquireWakeLock(context);
//            if (!faceTrackParam.previewTrack) {
//                faceTrackParam.rotateAngle = orientation;
//            } else {
//                faceTrackParam.rotateAngle = faceTrackParam.isBackCamera ? orientation : 360 - orientation;
//            }
//
//            int left = 0;
//            int top = 0;
//            int right = width;
//            int bottom = height;
//            // 限定检测区域
//            if (faceTrackParam.enableROIDetect) {
//                float line = height * faceTrackParam.roiRatio;
//                left = (int) ((width - line) / 2.0f);
//                top = (int) ((height - line) / 2.0f);
//                right = width - left;
//                bottom = height - top;
//            }
//
//            facepp.init(context, ConUtil.getFileContent(context, R.raw.megviifacepp_0_4_7_model));
//            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
//            faceppConfig.interval = faceTrackParam.detectInterval;
//            faceppConfig.minFaceSize = faceTrackParam.minFaceSize;
//            faceppConfig.roi_left = left;
//            faceppConfig.roi_top = top;
//            faceppConfig.roi_right = right;
//            faceppConfig.roi_bottom = bottom;
//            faceppConfig.one_face_tracking = faceTrackParam.enableMultiFace ? 0 : 1;
//            faceppConfig.detectionMode = faceTrackParam.trackMode;
//            facepp.setFaceppConfig(faceppConfig);
//        }


        /**
         * 检测人脸
         * @param data      图像数据，预览时为NV21，静态图片则为RGBA格式
         * @param width     图像宽度
         * @param height    图像高度
         * @return          是否检测成功
         */
//        private synchronized void internalTrackFace(byte[] data, int width, int height) {
//            FaceTrackParam faceTrackParam = FaceTrackParam.getInstance();
//            if (!faceTrackParam.canFaceTrack || facepp == null) {
//                LandmarkEngine.getInstance().setFaceSize(0);
//                if (faceTrackParam.trackerCallback != null) {
//                    faceTrackParam.trackerCallback.onTrackingFinish();
//                }
//                return;
//            }
//
//            // 调整检测监督
//            long faceDetectTime_action = System.currentTimeMillis();
//            // 获取设备旋转
//            int orientation = faceTrackParam.previewTrack ? mSensorUtil.orientation : 0;
//            int rotation = 0;
//            if (orientation == 0) {         // 0
//                rotation = faceTrackParam.rotateAngle;
//            } else if (orientation == 1) {  // 90
//                rotation = 0;
//            } else if (orientation == 2) {  // 270
//                rotation = 180;
//            } else if (orientation == 3) {  // 180
//                rotation = 360 - faceTrackParam.rotateAngle;
//            }
//            // 设置旋转角度
//            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
//            if (faceppConfig.rotation != rotation) {
//                faceppConfig.rotation = rotation;
//                facepp.setFaceppConfig(faceppConfig);
//            }
//
//            // 人脸检测
//            final Facepp.Face[] faces = facepp.detect(data, width, height,
//                    faceTrackParam.previewTrack ? Facepp.IMAGEMODE_NV21 : Facepp.IMAGEMODE_RGBA);
//
//            // 计算检测时间
//            if (VERBOSE) {
//                final long algorithmTime = System.currentTimeMillis() - faceDetectTime_action;
//                Log.d("onFaceTracking", "track time = " + algorithmTime);
//            }
//
//            // 设置旋转方向
//            LandmarkEngine.getInstance().setOrientation(orientation);
//            // 设置是否需要翻转
//            LandmarkEngine.getInstance().setNeedFlip(faceTrackParam.previewTrack && faceTrackParam.isBackCamera);
//
//            // 计算人脸关键点
//            if (faces != null && faces.length > 0) {
//                for (int index = 0; index < faces.length; index++) {
//                    // 关键点个数
//                    if (faceTrackParam.enable106Points) {
//                        facepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK106);
//                    } else {
//                        facepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK81);
//                    }
//                    // 获取姿态角信息
//                    if (faceTrackParam.enable3DPose) {
//                        facepp.get3DPose(faces[index]);
//                    }
//                    Facepp.Face face = faces[index];
//
//                    OneFace oneFace = LandmarkEngine.getInstance().getOneFace(index);
//                    // 是否检测性别年龄属性
//                    if (faceTrackParam.enableFaceProperty) {
//                        facepp.getAgeGender(face);
//                        oneFace.gender = face.female > face.male ? OneFace.GENDER_WOMAN
//                                : OneFace.GENDER_MAN;
//                        oneFace.age = Math.max(face.age, 1);
//                    } else {
//                        oneFace.gender = -1;
//                        oneFace.age = -1;
//                    }
//
//                    // 姿态角和置信度
//                    oneFace.pitch = face.pitch;
//                    oneFace.yaw = face.yaw;
//                    oneFace.roll = face.roll;
//                    if (faceTrackParam.previewTrack) {
//
//                        if (faceTrackParam.isBackCamera) {
//                            oneFace.roll = (float) (Math.PI / 2.0f + oneFace.roll);
//                        } else {
//                            oneFace.roll = (float) (Math.PI / 2.0f - face.roll);
//                        }
//                    }
//                    oneFace.confidence = face.confidence;
//
//                    // 预览状态下，宽高交换
//                    if (faceTrackParam.previewTrack) {
//                        if (orientation == 1 || orientation == 2) {
//                            int temp = width;
//                            width = height;
//                            height = temp;
//                        }
//                    }
//
//                    // 获取一个人的关键点坐标
//                    if (oneFace.vertexPoints == null || oneFace.vertexPoints.length != face.points.length * 2) {
//                        oneFace.vertexPoints = new float[face.points.length * 2];
//                    }
//                    for (int i = 0; i < face.points.length; i++) {
//                        // orientation = 0、3 表示竖屏，1、2 表示横屏
//                        float x = (face.points[i].x / height) * 2 - 1;
//                        float y = (face.points[i].y / width) * 2 - 1;
//                        float[] point = new float[] {x, -y};
//                        if (orientation == 1) {
//                            if (faceTrackParam.previewTrack && faceTrackParam.isBackCamera) {
//                                point[0] = -y;
//                                point[1] = -x;
//                            } else {
//                                point[0] = y;
//                                point[1] = x;
//                            }
//                        } else if (orientation == 2) {
//                            if (faceTrackParam.previewTrack && faceTrackParam.isBackCamera) {
//                                point[0] = y;
//                                point[1] = x;
//                            } else {
//                                point[0] = -y;
//                                point[1] = -x;
//                            }
//                        } else if (orientation == 3) {
//                            point[0] = -x;
//                            point[1] = y;
//                        }
//                        // 顶点坐标
//                        if (faceTrackParam.previewTrack) {
//                            if (faceTrackParam.isBackCamera) {
//                                oneFace.vertexPoints[2 * i] = point[0];
//                            } else {
//                                oneFace.vertexPoints[2 * i] = -point[0];
//                            }
//                        } else { // 非预览状态下，左右不需要翻转
//                            oneFace.vertexPoints[2 * i] = point[0];
//                        }
//                        oneFace.vertexPoints[2 * i + 1] = point[1];
//                    }
//                    // 插入人脸对象
//                    LandmarkEngine.getInstance().putOneFace(index, oneFace);
//                }
//            }
//            // 设置人脸个数
//            LandmarkEngine.getInstance().setFaceSize(faces!= null ? faces.length : 0);
//            // 检测完成回调
//            if (faceTrackParam.trackerCallback != null) {
//                faceTrackParam.trackerCallback.onTrackingFinish();
//            }
//        }




    }

}
