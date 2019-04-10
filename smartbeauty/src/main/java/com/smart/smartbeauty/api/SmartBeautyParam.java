package com.smart.smartbeauty.api;

/**
 * Created by deepglint on 2019/3/5.
 */

public class SmartBeautyParam{
    // 磨皮程度 0.0 ~ 1.0f
    public float beautyIntensity;
    // 美肤程度 0.0 ~ 0.5f
    public float complexionIntensity;
    // 瘦脸程度 0.0 ~ 1.0f
    public float faceLift;
    // 削脸程度 0.0 ~ 1.0f
    public float faceShave;
    // 小脸 0.0 ~ 1.0f
    public float faceNarrow;
    // 下巴-1.0f ~ 1.0f
    public float chinIntensity;
    // 法令纹 0.0 ~ 1.0f
    public float nasolabialFoldsIntensity;
    // 额头 -1.0f ~ 1.0f
    public float foreheadIntensity;
    // 大眼 0.0f ~ 1.0f
    public float eyeEnlargeIntensity;
    // 眼距 -1.0f ~ 1.0f
    public float eyeDistanceIntensity;
    // 眼角 -1.0f ~ 1.0f
    public float eyeCornerIntensity;
    // 卧蚕 0.0f ~ 1.0f
    public float eyeFurrowsIntensity;
    // 眼袋 0.0 ~ 1.0f
    public float eyeBagsIntensity;
    // 亮眼 0.0 ~ 1.0f
    public float eyeBrightIntensity;
    // 瘦鼻 0.0 ~ 1.0f
    public float noseThinIntensity;
    // 鼻翼 0.0 ~ 1.0f
    public float alaeIntensity;
    // 长鼻子 0.0 ~ 1.0f
    public float proboscisIntensity;
    // 嘴型 0.0 ~ 1.0f;
    public float mouthEnlargeIntensity;
    // 美牙 0.0 ~ 1.0f
    public float teethBeautyIntensity;





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

    public void reset() {
        beautyIntensity = 0.5f;
        complexionIntensity = 0.5f;
        faceLift = 0.0f;
        faceShave = 0.0f;
        faceNarrow = 0.0f;
        chinIntensity = 0.0f;
        nasolabialFoldsIntensity = 0.0f;
        foreheadIntensity = 0.0f;
        eyeEnlargeIntensity = 0.0f;
        eyeDistanceIntensity = 0.0f;
        eyeCornerIntensity = 0.0f;
        eyeFurrowsIntensity = 0.0f;
        eyeBagsIntensity = 0.0f;
        eyeBrightIntensity = 0.0f;
        noseThinIntensity = 0.0f;
        alaeIntensity = 0.0f;
        proboscisIntensity = 0.0f;
        mouthEnlargeIntensity = 0.0f;
        teethBeautyIntensity = 0.0f;




        showCompare = false;
        enableDepthBlur = false;
        enableVignette = false;
        drawFacePoints = false;
    }

}
