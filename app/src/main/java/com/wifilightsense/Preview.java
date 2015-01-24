package com.wifilightsense;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {

    private final SurfaceHolder sHolder;
    private Camera sCamera;

    public void setCamera(Camera c) {
        sCamera = c;
    }

    public Preview(Context context, Camera camera) {
        super(context);
        sCamera = camera;

        sHolder = getHolder();

        sHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (sHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            sCamera.stopPreview();
        } catch (Exception ignored) {

        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            sCamera.setPreviewDisplay(sHolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sCamera.startPreview();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            sCamera.setPreviewDisplay(holder);
            sCamera.startPreview();
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview in surface created: " + e.getMessage());
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

}
