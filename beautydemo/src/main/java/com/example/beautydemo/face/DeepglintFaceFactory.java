package com.example.beautydemo.face;

/**
 * Created by deepglint on 2019/4/23.
 */

public class DeepglintFaceFactory implements IFaceFactory {
    public IFaceTracker createFaceTracker() {
        return DeepglintFaceTracker.getInstance();
    }
}
