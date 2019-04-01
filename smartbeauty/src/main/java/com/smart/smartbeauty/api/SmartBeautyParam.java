package com.smart.smartbeauty.api;

import com.cgfay.filterlibrary.glfilter.beauty.bean.BeautyParam;

/**
 * Created by deepglint on 2019/3/5.
 */

public class SmartBeautyParam extends BeautyParam {
    // 是否显示对比效果
    public boolean showCompare;
    // 是否允许景深
    public boolean enableDepthBlur;
    // 是否允许暗角
    public boolean enableVignette;
    // 是否显示人脸关键点
    public boolean drawFacePoints;

    private static final SmartBeautyParam mInstance = new SmartBeautyParam();

    private SmartBeautyParam() {
        reset();
    }

    /**
     * 获取相机配置参数
     * @return
     */
    public static SmartBeautyParam getInstance() {
        return mInstance;
    }


    @Override
    public void reset() {
        super.reset();

        showCompare = false;
        enableDepthBlur = false;
        enableVignette = false;

        drawFacePoints = false;
    }

}
