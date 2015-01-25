package com.wifilightsense.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.wifilightsense.pojos.LightReadings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LightManager {

    @NonNull
    private final Context mContext;
    private float lightValue = -1F;
    private Date timeStampt;
    private final PackageManager manager;
    @NonNull
    private final List<LightReadings> lightReadings;

    public LightManager(@NonNull Context mContext) {
        this.mContext = mContext;
        manager = mContext.getPackageManager();
        lightReadings = new ArrayList<>();
    }

    @NonNull
    public List<LightReadings> generateLightReadingsList() {
        List<LightReadings> result;
        if (!hasLightSensorFeature()) {
            return new ArrayList<>();
        }
        setLightValue(-1F);
        registerLightSensor();
        Date d1 = new Date();

        result = doReadings();

        Date d2 = new Date();
        Log.e("Difference", "" + (int) ((d2.getTime() - d1.getTime())));
        unregisterLightSensor();
        return result;
    }

    private boolean hasLightSensorFeature() {
        return manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT);
    }

    private void registerLightSensor() {
        SensorManager sMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor LightSensor = sMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (LightSensor != null) {
            sMgr.registerListener(lightSensorEventListener, LightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void unregisterLightSensor() {
        SensorManager mySensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (LightSensor != null) {
            mySensorManager.unregisterListener(lightSensorEventListener, LightSensor);
        }
    }

    @NonNull
    private List<LightReadings> doReadings() {
        for (int i = 0; i < 10; i++) {
            if (lightValue > 0) {
                Log.i(CommonUtil.TAG, "Reading  # " + i + " - " + getTimeStampt() + "  -  " + getLightValue());
                if (getTimeStampt() == null) {
                	Log.w(CommonUtil.TAG, "timestamp null");
                } else {
                	lightReadings.add(new LightReadings(getTimeStampt(), getLightValue()));
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return lightReadings;
    }

    private void setLightValue(float lightValue) {
        synchronized (this) {
            this.lightValue = lightValue;
        }
    }


    Date getTimeStampt() {
        return timeStampt;
    }

    void setTimeStampt(Date timeStampt) {
        this.timeStampt = timeStampt;
    }

    float getLightValue() {
        return lightValue;
    }

    private final SensorEventListener lightSensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(@NonNull SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                synchronized (this) {
                    //Log.i(CommonUtil.TAG,"onSensorChanged : "+event.values[0]+" : : "+event.timestamp );
                    setLightValue(event.values[0]);
                    long timestamp = event.timestamp / 1000 / 1000;
                    if (System.currentTimeMillis() - timestamp > TimeUnit.DAYS.toMillis(2))
                        timestamp = System.currentTimeMillis() + (event.timestamp - System.nanoTime()) / 1000000L;
                    setTimeStampt(new Date(timestamp));
                }

            }
        }
    };
}
