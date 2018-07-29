package com.proger.ruslan.advancedact;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MeasuringLevel extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;

    private float[] rotationMatrix, accelerometer, geomagnesis, orientation, reservedAccelData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView (new LevelMeasureVisible(getApplicationContext()));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

        rotationMatrix = new float[16];
        accelerometer = new float[3];
        geomagnesis = new float[3];
        orientation = new float[3];
        reservedAccelData = new float[3];

        sensorManager =  (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener (this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener (this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        loadSensorsData(sensorEvent);

//        if ((Math.round(Math.toDegrees(orientation[0])) < Math.round(Math.toDegrees(reservedAccelData[0]))  - 5 || (Math.round(Math.toDegrees(orientation[0])) > Math.round(Math.toDegrees(reservedAccelData[0]))  + 5))) {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, geomagnesis);
        SensorManager.getOrientation(rotationMatrix, orientation);

//            reservedAccelData = orientation.clone();
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void loadSensorsData (SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometer = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnesis = sensorEvent.values.clone ();
                break;
        }
    }

    class LevelMeasureVisible extends SurfaceView implements SurfaceHolder.Callback {
        Drawing pointer;

        public LevelMeasureVisible(Context context) {
            super(context);

            getHolder().addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            pointer = new Drawing(surfaceHolder);
            pointer.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            boolean retry = true;

            pointer.requestStop();

            while (retry) {
                try {
                    pointer.join();
                    retry = false;
                } catch (InterruptedException e) {}
            }
        }
    }

    class Drawing extends Thread {
        private volatile boolean running = true;
        private SurfaceHolder holder;
        private Paint paint;
        private float deltaRotation;

        public Drawing (SurfaceHolder holder) {
            this.holder = holder;
            paint = new Paint ();
            deltaRotation = Math.round (Math.toDegrees(orientation[0]));
        }

        public void run () {
            while (running) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    try {
                        paint.setStyle(Paint.Style.FILL_AND_STROKE);
                        paint.setColor (Color.BLACK);
                        canvas.drawPaint(paint);
                        paint.setColor(Color.GREEN);
                        float deltaRot = deltaRotation;

                        if (deltaRotation > Math.round (Math.toDegrees(orientation[0])) || deltaRotation < Math.round (Math.toDegrees(orientation[0]))) {
                            if (Math.round(Math.toDegrees(orientation[0])) > deltaRotation)
                                deltaRotation -= 10;
                            else if (Math.round(Math.toDegrees(orientation[0])) < deltaRotation)
                                deltaRotation += 10;
                        }

//                        if (deltaRotation < 90 && deltaRotation > -90)
                            canvas.rotate(deltaRot - deltaRotation, canvas.getWidth()/2, canvas.getHeight()/2);
//                        else if (deltaRotation < -90)
//                            canvas.rotate(-90, canvas.getWidth()/2, canvas.getHeight()/2);
//                        else if (deltaRotation > 90)
//                            canvas.rotate(90, canvas.getWidth()/2, canvas.getHeight()/2);

                        canvas.drawRect(-500, canvas.getHeight()/2, canvas.getWidth()+500, canvas.getHeight()*2, paint);
                        paint.setTextSize(80);
                        canvas.drawText(String.valueOf(Math.round(Math.toDegrees(orientation[0]))), 100, 100, paint);
                    } catch (Exception e) {

                    } finally {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
            try {
                Thread.sleep (3000);
            } catch (InterruptedException e) {}
        }

        public void requestStop () {
            running = false;
        }
    }
}
