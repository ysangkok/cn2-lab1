/**
 *
 */
package com.wifilightsense.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wifilightsense.util.CommonUtil;

/**
 * @author FAISAL
 */
public class LightService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(CommonUtil.TAG, "onStartCommand .... ");
        new LightSensorTask(this).execute();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(CommonUtil.TAG, "Service stopped ......");
    }
}
