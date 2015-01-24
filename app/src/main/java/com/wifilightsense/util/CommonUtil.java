package com.wifilightsense.util;

import android.bluetooth.BluetoothAdapter;

public class CommonUtil {
    public final static String TAG = "WifiLightSense";
    private static String DEVIC_ID;

    private CommonUtil() {
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        String deviceName = myDevice.getName();
        DEVIC_ID = android.os.Build.BRAND + "-" + android.os.Build.MODEL + "-" + deviceName;
    }

    public static String getDeviceId() {
        return DEVIC_ID;
    }
}
