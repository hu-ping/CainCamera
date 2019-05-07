package com.smart.smartbeauty.api;

/**
 * Created by deepglint on 2019/4/29.
 */

public class SmartRenderParam {
    public int camareWidth = 0;
    public int cameraHeight = 0;
    public int orientation =  0;

    public SmartRenderParam(int w, int h, int degree) {
        camareWidth = w;
        cameraHeight = h;
        orientation = degree;
    }
}
