package com.smart.smartbeauty.api;

import android.util.SparseArray;



/**
 * Created by deepglint on 2019/3/12.
 */

public class SmartOneFace {
    // 性别标识
    public static final int GENDER_MAN = 0;
    public static final int GENDER_WOMAN = 1;
    // 置信度
    public float confidence;
    // 俯仰角(绕x轴旋转)
    public float pitch;
    // 偏航角(绕y轴旋转)
    public float yaw;
    // 翻滚角(绕z轴旋转)
    public float roll;
    // 年龄
    public float age;
    // 性别
    public int gender;
    // 顶点坐标
    public float[] vertexPoints;

    @Override
    protected SmartOneFace clone() {
        SmartOneFace copy = new SmartOneFace();
        copy.confidence = this.confidence;
        copy.pitch = this.pitch;
        copy.yaw = this.yaw;
        copy.roll = this.roll;
        copy.age = this.age;
        copy.gender = this.gender;
        copy.vertexPoints = this.vertexPoints.clone();
        return copy;
    }

    /**
     * 复制数据
     * @param origin
     * @return
     */
    public static SmartOneFace[] arrayCopy(SmartOneFace[] origin) {
        if (origin == null) {
            return null;
        }
        SmartOneFace[] copy = new SmartOneFace[origin.length];
        for (int i = 0; i < origin.length; i++) {
            copy[i] = origin[i].clone();
        }
        return copy;
    }

    /**
     * 复制数据
     * @param origin
     * @return
     */
    public static SmartOneFace[] arrayCopy(SparseArray<SmartOneFace> origin) {
        if (origin == null) {
            return null;
        }
        SmartOneFace[] copy = new SmartOneFace[origin.size()];
        for (int i = 0; i < origin.size(); i++) {
            copy[i] = origin.get(i).clone();
        }
        return copy;
    }
}
