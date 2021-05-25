package com.coderonwork.facemon;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {


    Button b,b1;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        b = findViewById(R.id.start1);
        b1=findViewById(R.id.stop);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
        }
        Intent i = new Intent(MainActivity.this, MyService.class);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startService(i);
                    //Toast.makeText(this,"Service Started",Toast.LENGTH_LONG).show();
                }
                //finish();
            }
        });
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent i = new Intent(MainActivity.this, MyService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stopService(i);
                }
            }
        });
       /* if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
        } else {
            Camera camera = frontCam();
            Camera.Parameters campar = camera.getParameters();
            F = campar.getFocalLength();
            angleX = campar.getHorizontalViewAngle();
            angleY = campar.getVerticalViewAngle();
            sensorX = (float) (Math.tan(Math.toRadians(angleX / 2)) * 2 * F);
            sensorY = (float) (Math.tan(Math.toRadians(angleY / 2)) * 2 * F);
            camera.stopPreview();
            camera.release();
            textView = findViewById(R.id.text);
            createCameraSource();
        }

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
        detector.setProcessor(new LargestFaceFocusingProcessor(detector, new FaceTracker()));

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
    }

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
            if(distance<250){
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

// Vibrate for 400 milliseconds
                v.vibrate(100);
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
    }*/
    }
}