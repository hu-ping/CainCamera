package com.example.beautydemo.face;

/**
 * Created by deepglint on 2019/4/23.
 */

public class KSFaceFactory implements IFaceFactory {
    @Override
    public IFaceTracker createFaceTracker() {
        return KSFaceTracker.getInstance();
    }
}
