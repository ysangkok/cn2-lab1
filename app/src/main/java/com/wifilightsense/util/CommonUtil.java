package com.wifilightsense.util;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CommonUtil {
    public final static String TAG = "WifiLightSense";
    @NonNull
    private static String DEVIC_ID;
    public static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private CommonUtil() {
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        String deviceName = myDevice.getName();
        DEVIC_ID = android.os.Build.BRAND + "-" + android.os.Build.MODEL + "-" + deviceName;
    }

    public static
    @NonNull
    String getDeviceId() {
        return DEVIC_ID;
    }
}
