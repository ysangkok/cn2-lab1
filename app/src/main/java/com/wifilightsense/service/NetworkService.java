package com.wifilightsense.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.wifilightsense.NetworkActivity;
import com.wifilightsense.R;
import com.wifilightsense.util.CommonUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkService extends Service {

    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private final int NOTIFICATION = 82194;
    @Nullable
    private PingCallback pingCallback;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {

        @NonNull
        public NetworkService getService() {
            return NetworkService.this;
        }
    }

    private Thread t;

    public List<InetAddress> pongs;

    @Override
    public void onCreate() {
        final Handler handler = new Handler();

        pongs = new ArrayList<>();

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting. We put an icon in the
        // status bar.
        showNotification();

        t = new Thread(new Runnable() {
            public void run() {
                try {
                    // talk using
                    // socat - UDP-DATAGRAM:10.42.0.255:4242,broadcast
                    // remember to fix ip!!!!
                    DatagramChannel channel = DatagramChannel.open();
                    DatagramSocket s = channel.socket();
                    s.setSoTimeout(100);
                    s.setReuseAddress(true);
                    s.bind(new InetSocketAddress("0.0.0.0", 4242));
                    byte[] d = new byte[]{-1};
                    while (!t.isInterrupted()
                            && NetworkActivity.isMyServiceRunning(NetworkService.this, NetworkService.this.getClass())) {
                        final DatagramPacket p = new DatagramPacket(d, d.length);
                        try {
                            s.receive(p);
                        } catch (SocketTimeoutException e) {
                            continue;
                        }
                        if (NetworkActivity.isOwnAddress(p.getAddress()))
                            continue;
                        switch (d[0]) {
                            case 0: // PING
                                handler.post(new Runnable() {
                                    public void run() {
                                        address = p.getAddress();
                                        Toast.makeText(NetworkService.this, "Got PING; sending PONG! " + p.getAddress().toString(),
                                                Toast.LENGTH_SHORT).show();
                                        //NetworkActivity.onPing();
                                        if (pingCallback != null) {
                                            pingCallback.onPing();
                                        } else {
                                            Log.e(CommonUtil.TAG, "Ping callback is null");
                                        }

                                    }
                                });
                                NetworkActivity.sendUDPPacket(new DatagramPacket(new byte[]{1}, 1), p.getAddress()); // PONG
                                break;
                            default: // PONG
                                pongs.add(p.getAddress());
                                Log.e("HelloIntentService", "data byt " + Arrays.toString(d));
                                Log.e("HelloIntentService", "data str " + new String(d, Charset.forName("ISO-8859-1")));
                                Log.e("HelloIntentService", "addr " + p.getAddress().toString());
                                break;
                        }
                    }
                    s.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("HelloIntentService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        try {
            if (t != null)
                t.join(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Tell the user we stopped.
        Toast.makeText(this, "local service stopped", Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the
        // expanded notification
        CharSequence text = "local service started";

        // The PendingIntent to launch our activity if the user selects this
        // notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NetworkActivity.class), 0);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.favicon, text, System.currentTimeMillis());

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "local service label", text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    public void setPingCallback(@Nullable PingCallback pingCallback) {
        this.pingCallback = pingCallback;
    }

    @NonNull
    public static String getNetworkIdentifierOrPlaceholder() {
        return address != null ? address.getHostAddress() : "unknown";
    }

    private static InetAddress address;


}