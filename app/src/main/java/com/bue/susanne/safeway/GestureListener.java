package com.bue.susanne.safeway;

import android.graphics.PointF;
import android.view.GestureDetector;

import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.MapGesture;

import java.util.List;

/**
 * Created by abaumgra on 28/01/2017.
 */

public class GestureListener implements MapGesture.OnGestureListener {


    @Override
    public void onPanStart() {

    }

    @Override
    public void onPanEnd() {

    }

    @Override
    public void onMultiFingerManipulationStart() {

    }

    @Override
    public void onMultiFingerManipulationEnd() {

    }

    @Override
    public boolean onMapObjectsSelected(List<ViewObject> list) {
        return false;
    }

    @Override
    public boolean onTapEvent(PointF pointF) {
        //pointF.
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(PointF pointF) {
        return false;
    }

    @Override
    public void onPinchLocked() {

    }

    @Override
    public boolean onPinchZoomEvent(float v, PointF pointF) {
        return false;
    }

    @Override
    public void onRotateLocked() {

    }

    @Override
    public boolean onRotateEvent(float v) {
        return false;
    }

    @Override
    public boolean onTiltEvent(float v) {
        return false;
    }

    @Override
    public boolean onLongPressEvent(PointF pointF) {
        return false;
    }

    @Override
    public void onLongPressRelease() {

    }

    @Override
    public boolean onTwoFingerTapEvent(PointF pointF) {
        return false;
    }
}
