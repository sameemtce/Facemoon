package com.coderonwork.facemon;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

public class MyService extends Service {
    public MyService() {
    }
    static final int IMAGE_WIDTH = 1024;
    static final int IMAGE_HEIGHT = 1024;

    static final int RIGHT_EYE = 0;
    static final int LEFT_EYE = 1;

    static final int AVERAGE_EYE_DISTANCE = 63; // in mm

    TextView textView;
    Context context;
    float F = 1f;           //focal length
    float sensorX, sensorY; //camera sensor dimensions
    float angleX, angleY;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            Intent snoozeIntent = new Intent(MyService. this, MainActivity. class ) ;
            //snoozeIntent.setAction( "ACTION_SNOOZE" ) ;
            //snoozeIntent.putExtra( "EXTRA_NOTIFICATION_ID" , 0 ) ;
            PendingIntent snoozePendingIntent = PendingIntent. getBroadcast (MyService. this, 0 , snoozeIntent , 0 ) ;

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
           // Intent i = new Intent(this,MainActivity2.class);
            NotificationCompat.Builder notification1 = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Background Notification")
                    .setContentText("APP is Running");

            Notification notification=notification1.build();

            startForeground(1, notification); }

        /*Notification builder = new NotificationCompat.Builder(this,"new")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Background Notify")
                .setContentText("App is running in the background")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        startForeground(1,builder);
        Intent i =new Intent(MyService.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager n= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        n.notify(0,builder);*/
        context = getApplicationContext();
        /*if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
        } else {*/
        Camera camera = frontCam();
        Camera.Parameters campar = camera.getParameters();
        F = campar.getFocalLength();
        angleX = campar.getHorizontalViewAngle();
        angleY = campar.getVerticalViewAngle();
        sensorX = (float) (Math.tan(Math.toRadians(angleX / 2)) * 2 * F);
        sensorY = (float) (Math.tan(Math.toRadians(angleY / 2)) * 2 * F);
        camera.stopPreview();
        camera.release();
        //textView = findViewById(R.id.text);
        createCameraSource();
        //}

        return flags;
    }


    private Camera frontCam() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            Log.v("CAMID", camIdx + "");
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("FAIL", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }


    public void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        detector.setProcessor(new LargestFaceFocusingProcessor(detector, new MyService.FaceTracker()));

        CameraSource cameraSource = new CameraSource.Builder(this, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
        System.out.println(cameraSource.getPreviewSize());

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraSource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /*public void showStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(message);
            }
        });
    }*/

    private class FaceTracker extends Tracker<Face> {


        private FaceTracker() {

        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face) {
            PointF leftEyePos = face.getLandmarks().get(LEFT_EYE).getPosition();
            PointF rightEyePos = face.getLandmarks().get(RIGHT_EYE).getPosition();

            float deltaX = Math.abs(leftEyePos.x - rightEyePos.x);
            float deltaY = Math.abs(leftEyePos.y - rightEyePos.y);

            float distance;
            if (deltaX >= deltaY) {
                distance = F * (AVERAGE_EYE_DISTANCE / sensorX) * (IMAGE_WIDTH / deltaX);
            } else {
                distance = F * (AVERAGE_EYE_DISTANCE / sensorY) * (IMAGE_HEIGHT / deltaY);
            }
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                if(distance<400){
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

// Vibrate for 400 milliseconds
                    v.vibrate(100);
                }
            }
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (distance < 270) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(100);
                }
            }
            //showStatus("distance: " + String.format("%.0f", distance) + "mm");
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);
            //showStatus("face not detected");
        }

        @Override
        public void onDone() {
            super.onDone();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopSelf();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}