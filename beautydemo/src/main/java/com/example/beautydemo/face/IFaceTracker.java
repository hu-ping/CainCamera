package com.example.beautydemo.face;

import android.content.Context;

/**
 * Created by deepglint on 2019/4/23.
 */

public interface IFaceTracker {
    void initTracker(Context context);

    void setFaceCallback(FaceTrackerCallback callback);

    void setBackCamera(boolean backCamera);

    void prepareFaceTracker(Context context, String code, int orientation, int width, int height);

    void trackFace(byte[] data, int width, int height);


    void destroyTracker();
}
