package com.smart.smartbeauty.api;

import com.cgfay.filterlibrary.landmark.LandmarkEngine;
import com.cgfay.filterlibrary.landmark.OneFace;

/**
 * Created by deepglint on 2019/3/11.
 */

public class SmartLandmark {


    private static class SmartLandmarkHolder {
        public static SmartLandmark instance = new SmartLandmark();
    }

    public static SmartLandmark getInstance() {
        return SmartLandmark.SmartLandmarkHolder.instance;
    }
    /**
     * 设置旋转角度
     * @param orientation
     */
    public void setOrientation(int orientation) {
        LandmarkEngine.getInstance().setOrientation(orientation);
    }


    /**
     * 设置是否需要翻转
     * @param flip
     */
    public void setNeedFlip(boolean flip) {
        // 设置是否需要翻转
        LandmarkEngine.getInstance().setNeedFlip(flip);
    }


    /**
     * 获取一个人脸关键点数据对象
     * @return
     */
    public OneFace getFreeFace() {
        return  LandmarkEngine.getInstance().getFreeFace();
    }


    /**
     * 插入一个人脸关键点数据对象
     *
     */
    public void putOneFace(OneFace oneFace) {
        LandmarkEngine.getInstance().putOneFace(oneFace);
    }

    /**
     * 清空所有人脸对象
     */
    public void clearAll() {
        LandmarkEngine.getInstance().clearAll();
    }

    /**
     * 设置人脸数
     * @param size
     */
    public void setFaceSize(int size) {
        LandmarkEngine.getInstance().setFaceSize(size);
    }





    private static SmartOneFace toSmartOneFace(OneFace face) {

        SmartOneFace copy = new SmartOneFace();

        copy.confidence = face.confidence;
        copy.pitch = face.pitch;
        copy.yaw = face.yaw;
        copy.roll = face.roll;
        copy.age = face.age;
        copy.gender = face.gender;

        if (face.vertexPoints != null) {
            copy.vertexPoints = face.vertexPoints;
        }

        return copy;
    }

    private static OneFace toOneFace(SmartOneFace face) {
        OneFace copy = new OneFace();

        copy.confidence = face.confidence;
        copy.pitch = face.pitch;
        copy.yaw = face.yaw;
        copy.roll = face.roll;
        copy.age = face.age;
        copy.gender = face.gender;
        copy.vertexPoints = face.vertexPoints;

        return copy;
    }
}
