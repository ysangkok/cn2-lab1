package com.wifilightsense;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wifilightsense.service.NetworkService;
import com.wifilightsense.service.PingCallback;
import com.wifilightsense.util.CommonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkActivity extends Activity implements PingCallback {

    private final Class<NetworkService> cls = NetworkService.class;
    @Nullable
    private Camera cam;
    private final Handler mHandler = new Handler();

    private FrameLayout camView;

    public static boolean isMyServiceRunning(@NonNull Context act, @NonNull Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(getClass().getSimpleName(), "onCreate");
        super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        Button start_listener = new Button(this);
        Button stop_listener = new Button(this);
        Button broadcast_ping = new Button(this);
        broadcast_ping.setText("Broadcast ping on subnet");
        start_listener.setText("Start listener");

        stop_listener.setText("Stop listener");
        camView = new FrameLayout(this);
        final TextView server_status = new TextView(this);

        final TextView pong_display = new TextView(this);
        ll.addView(start_listener);
        ll.addView(stop_listener);
        ll.addView(server_status);
        ll.addView(broadcast_ping);
        ll.addView(pong_display);
        ll.addView(camView);
        setContentView(ll);
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isMyServiceRunning(NetworkActivity.this, cls)) {
                            server_status.setTextColor(Color.GREEN);
                            server_status.setText("Running");
                        } else {
                            server_status.setTextColor(Color.RED);
                            server_status.setText("Stopped");
                        }

                        if (mIsBound) {
                            if (mBoundService != null) {
                                pong_display.setText(mBoundService.pongs.toString());
                            }
                        }
                    }
                });
            }
        }, 0, 1000);

        start_listener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pong_display.setText("");

                if (!mIsBound) {
                    mIsBound = bindService(new Intent(NetworkActivity.this, cls), mConnection, Context.BIND_AUTO_CREATE);
                }
            }
        });

        stop_listener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unbind();
            }
        });

        broadcast_ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!checkFlashAvailability()) return;

                broadcastUDPPacket(NetworkActivity.this, new DatagramPacket(new byte[]{0}, 1));

                if (flashOn())
                    return;

                turnOnFlashLight();
                new CountDownTimer(5000, 5000) {
                    @Override
                    public void onFinish() {
                        turnOffFlashLight();
                    }

                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                }.start();
            }
        });
    }

    private boolean flashOn() {
        if (cam==null) return false;
        String flashmode = cam.getParameters().getFlashMode();
        Log.w(CommonUtil.TAG, "flashOn(): " + flashmode);
        return flashmode.equals(Parameters.FLASH_MODE_TORCH);
    }

    private static void broadcastUDPPacket(@NonNull Context toastContext, @NonNull DatagramPacket p) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            sendUDPPacket(p, getBroadcastAddr());
            Log.e(CommonUtil.TAG, "Didnt get exception on packet send.");
        } catch (NoBroadcastAddressAvailableException e) {
            Toast t = Toast.makeText(toastContext, "No broadcast address available, are you sure you're connected?", Toast.LENGTH_SHORT);
            t.show();
        }
        Log.e(CommonUtil.TAG, "Tried to send packet.");
    }

    public static void sendUDPPacket(@NonNull final DatagramPacket p, final InetAddress addr) {
        try {
            // receive with:
            // socat UDP-LISTEN:4242,rcvbuf=1 - | head -c1 | iconv -f iso-8859-1
            // -t utf-8

            DatagramSocket s = new DatagramSocket();
            p.setAddress(addr);
            p.setPort(4242);
            s.send(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private static InetAddress getBroadcastAddr() throws NoBroadcastAddressAvailableException {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            try {
                if (networkInterface.isLoopback())
                    continue;
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
            // Don't want to broadcast to the loopback
            // interface
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null)
                    continue;
                return broadcast;
            }
        }
        throw new NoBroadcastAddressAvailableException();
    }

    public static boolean isOwnAddress(InetAddress addr) {
        Enumeration<NetworkInterface> l;
        try {
            l = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while (l.hasMoreElements()) {
            NetworkInterface i = l.nextElement();
            for (InterfaceAddress j : i.getInterfaceAddresses())
                if (j.getAddress().equals(addr))
                    return true;
        }
        return false;
    }

    @Nullable
    private NetworkService mBoundService;

    @Nullable
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, @NonNull IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            System.out.println(service.toString());
            NetworkService.LocalBinder b = (NetworkService.LocalBinder) service;
            mBoundService = b.getService();

            mBoundService.setPingCallback(NetworkActivity.this);
            // Tell the user about this for our demo.
            Toast.makeText(NetworkActivity.this, "local service connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(NetworkActivity.this, "local service disconnected", Toast.LENGTH_SHORT).show();
        }
    };
    private boolean mIsBound;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbind();
    }

    private void unbind() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            if (mBoundService != null) mBoundService.setPingCallback(null);
            mIsBound = false;
        }
    }

    private boolean checkFlashAvailability() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) return true;

        //if flash doesnt exist show alert message
        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Alert!");
        alert.setMessage("This Device does not support flash light!");
        alert.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();
        return false;
    }

    //************* turning on Flash *******************
    private void turnOnFlashLight() {
        if (cam != null) cam.release();
        cam = Camera.open(Camera.getNumberOfCameras()-1);
        if (flashOn()) {
                return;
        }
        Camera.Parameters params;
        (params = cam.getParameters()).setFlashMode(Parameters.FLASH_MODE_TORCH);
        cam.setParameters(params);
        cam.startPreview();
    }

    //*************** turning of Flash *****************
    private void turnOffFlashLight() {
        if (!flashOn()) {
            Log.w(CommonUtil.TAG, "TRYING TO TURN OFF FLASHLIGHT THAT ISN'T ON, status: " + (cam == null ? "no cam" : cam.getParameters().getFlashMode()));
            return;
        }

        Camera.Parameters params;
        (params = cam.getParameters()).setFlashMode(Parameters.FLASH_MODE_OFF);
        cam.setParameters(params);
        cam.stopPreview();
        cam.release();
    }

    @Override protected void onPause() {
        super.onPause();
        if (loopThread != null) loopThread.interrupt();
        if (cam != null) cam.release();
    }

    private void runWithDeadline(final Runnable r) {
        AsyncTask<Void, Void, Void> ru = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                r.run();
                return null;
            }
        };

        try {
            ru.execute().get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Camera operation timeout");
        }
    }

    private void takePhoto() {
/*
        Log.w(CommonUtil.TAG, "Releasing camera");
        if (cam != null) cam.release();

        Log.w(CommonUtil.TAG, "Opening camera");
        cam = Camera.open(Camera.getNumberOfCameras()-1);
        Runnable runnable;
        mHandler.postAtFrontOfQueue(runnable = new Runnable(){
            @Override
            public void run() {
                Preview sView = new Preview(NetworkActivity.this, cam);
                camView.removeAllViews();
                camView.addView(sView);
                synchronized(this) { this.notifyAll(); }
            }
        });
        Log.w(CommonUtil.TAG, "Waiting for new preview");
        try {
            synchronized(runnable) { runnable.wait(); }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Log.w(CommonUtil.TAG, "Taking picture");
        */
        cam.takePicture(null, null, jPic);
        cam.startPreview();
        //Log.w(CommonUtil.TAG, "Took picture");
    }

    @NonNull
    private final PictureCallback jPic = new PictureCallback() {

        public void onPictureTaken(@NonNull byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                MediaStore.Images.Media.insertImage(getContentResolver(), pictureFile.getAbsolutePath(), pictureFile.getName(), pictureFile.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    };

    @NonNull
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "WifiLightSense");
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                throw new RuntimeException("Couldn't make directory");
            }
        }
        if (!mediaStorageDir.isDirectory()) throw new RuntimeException("mediaStorageDir isn't a directory");
        // Create a media file name
        String timeStamp = CommonUtil.dateFormat.format(new Date());

        return new File(mediaStorageDir.getPath() + File.separator + CommonUtil.getDeviceId() + "-" + NetworkService.getNetworkIdentifierOrPlaceholder() + "-" + timeStamp + ".jpg");
    }

    private final Runnable pictureTaker = new Runnable() {
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            final AtomicInteger i = new AtomicInteger(0);
            while (System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(20)) {
                //mHandler.post(new Runnable() {
                //    @Override
                //    public void run() {
                Log.e(CommonUtil.TAG, "calling takePhoto, photo " + i.addAndGet(1));
                takePhoto();
                //    }
                //});
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                } catch (InterruptedException e) {
                    Log.e(CommonUtil.TAG, "loopThread interrupted");
                    return;
                }
            }
        }
    };

    @Nullable
    Thread loopThread;

    static int pictureTakerThreadNumber = 0;

    @Override
    public void onPing() {
        Log.e("Faisal", "Ping Received ......");

        if (cam != null) cam.release();
        cam = Camera.open(Camera.getNumberOfCameras() - 1);
        Preview sView = new Preview(NetworkActivity.this, cam);
        camView.removeAllViews();
        camView.addView(sView);

        if (loopThread == null || !loopThread.isAlive()) {
            loopThread = new Thread(pictureTaker);
            loopThread.setName("PictureTakerThread-" + ++pictureTakerThreadNumber);
            loopThread.start();
        } else {
            Log.w(CommonUtil.TAG, "Loop thread already started");
        }

        Intent brdIntent = new Intent();
        brdIntent.setAction("com.wifilightsense.pingaction");
        sendBroadcast(brdIntent);
    }
}

class NoBroadcastAddressAvailableException extends Exception {

}
