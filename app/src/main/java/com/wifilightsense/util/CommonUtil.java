package com.wifilightsense.util;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CommonUtil {
    public final static String TAG = "WifiLightSense";
    public static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static
    @NonNull
    String getDeviceId() {
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        String deviceName = myDevice.getName();
        return android.os.Build.BRAND + "-" + android.os.Build.MODEL + "-" + deviceName;
    }
}
