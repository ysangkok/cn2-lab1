package com.wifilightsense.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wifilightsense.util.CommonUtil;

public class PeriodicBroadcastReceiver extends BroadcastReceiver {
    @Nullable
    private static WakeLock wakeLock = null;

    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        getLock(context);
        Log.i(CommonUtil.TAG, "PeriodicBroadcastReceiver.onReceive called .....");
        Intent lightService = new Intent(context, LightService.class);
        context.startService(lightService);

    }


    private static synchronized void getLock(@NonNull Context ctx) {
        if (wakeLock == null) {
            PowerManager mgr = (PowerManager) ctx
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr
                    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, CommonUtil.TAG);
            wakeLock.setReferenceCounted(true);
        }
        wakeLock.acquire();
    }

    public static synchronized void releaseLock() {
        if (wakeLock != null) {
            wakeLock.release();
        }
    }

}
