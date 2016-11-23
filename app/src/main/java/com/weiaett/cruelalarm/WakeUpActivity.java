package com.weiaett.cruelalarm;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.weiaett.cruelalarm.Math.MathActivity;
import com.weiaett.cruelalarm.models.Alarm;
import com.weiaett.cruelalarm.sheduling.WakeUpBroadcastReceiver;
import com.weiaett.cruelalarm.utils.DBHelper;
import com.weiaett.cruelalarm.utils.ImageLoader;
import com.weiaett.cruelalarm.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class WakeUpActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private MediaPlayer player;
    private int interval;
    private Alarm alarm;
    private Vibrator vibrator;
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private View viewCamera;
    private View viewWakeUp;
    private ImageView photoFull;
    private ImageView photoPreview;
    private ImageView ivSurrender;
    private int attempt = 0;
    private boolean canSurrender = false;
    private File refPhoto;

    // TODO: real comparison + empty photo manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_up);

        Utils.unlockScreen(this);

        try {
            Bundle bundle = this.getIntent().getExtras();
            alarm = (Alarm) bundle.getSerializable(this.getString(R.string.intent_alarm));
        } catch (RuntimeException e) {
            alarm = new Alarm(this);
        }

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        alarm = DBHelper.getInstance(this).getAllAlarms(this).get(0);

        ((TextView) this.findViewById(R.id.tvDescription)).setText(alarm.getDescription());
        viewCamera = findViewById(R.id.incCamera);
        viewWakeUp = findViewById(R.id.incWakeUp);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camera = getCameraInstance();

        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();

        Camera.Size bestSize = supportedSizes.get(0);
        for(int i = 1; i < supportedSizes.size(); i++){
            if((supportedSizes.get(i).width * supportedSizes.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = supportedSizes.get(i);
            }
        }

        params.setPictureSize(bestSize.width, bestSize.height);
        camera.setParameters(params);

        player = MediaPlayer.create(this, Uri.parse(alarm.getToneUriString()));
        player.setLooping(true);
        //player.start();

        if (alarm.getHasVibration()) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 1000, 200, 200, 200 };
//            vibrator.vibrate(pattern, 0);
        }

        SharedPreferences config = this.getSharedPreferences(this.getString(R.string.sp_config),
                Context.MODE_PRIVATE);
        interval = config.getInt(this.getString(R.string.sp_config_interval), 5);

        findViewById(R.id.btnReady).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        findViewById(R.id.ivPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, WakeUpActivity.this.onPictureTaken);
            }
        });

        photoPreview = (ImageView) findViewById(R.id.ivPhotoPreview);
        photoFull = (ImageView) findViewById(R.id.ivPhotoFull);
        findViewById(R.id.ivShowPhoto).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: // нажатие
                        photoFull.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_MOVE: // движение
                        break;
                    case MotionEvent.ACTION_UP: // отпускание
                    case MotionEvent.ACTION_CANCEL:
                        photoFull.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });

        ivSurrender = (ImageView) findViewById(R.id.ivSurrender);
        ivSurrender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callMathTask();
            }
        });
        setupPhoto();

        new CountDownTimer(3600000, 100000) { // 1 hour
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                terminate();
            }
        }.start();

        new CountDownTimer(120000, 10000) { // 3 minutes
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                canSurrender = true;
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0) {
            returnToMainScreen();
            callRepeatDialog();
        }
    }

    private void callMathTask() {
        Intent mathIntent = new Intent(WakeUpActivity.this, MathActivity.class);
        startActivityForResult(mathIntent, 0);
    }

    private void takePhoto() {
        viewWakeUp.setVisibility(View.GONE);
        viewCamera.setVisibility(View.VISIBLE);
        if (attempt > 3 && canSurrender) {
            ivSurrender.setVisibility(View.VISIBLE);
        }

        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void returnToMainScreen() {
        viewCamera.setVisibility(View.GONE);
        viewWakeUp.setVisibility(View.VISIBLE);
    }

    public Camera getCameraInstance(){
        if (camera == null) {
            try {
                camera = Camera.open(); // attempt to get a Camera instance
            } catch (Exception e) {
                e.printStackTrace();
                // Camera is not available (in use or does not exist)
            }
        }
        camera.setDisplayOrientation(90);
        return camera; // returns null if camera is unavailable
    }

    private Camera.PictureCallback onPictureTaken = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File lastPhoto = ImageLoader.prepareFile(WakeUpActivity.this);
            if (lastPhoto == null){
                Log.d("Camera", "Error creating media file, check storage permissions");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(lastPhoto);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("Camera", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Camera", "Error accessing file: " + e.getMessage());
            }

            // compare refPhoto and lastPhoto
            if (false) { // photo were matched
                returnToMainScreen();
                callRepeatDialog();
            } else { // photo were not matched
                Toast.makeText(WakeUpActivity.this, "Фотографии не совпадают", Toast.LENGTH_SHORT).show();
                returnToMainScreen();
                setupPhoto();
                attempt++;
            }
            lastPhoto.delete();
        }
    };

    private void callRepeatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                repeat();
            }
        });
        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                terminate();
            }
        });
        builder.setTitle("Повтор будильника");
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupPhoto() {

        Random random = new Random();
        String path;
        boolean fileExists;
        do {
            int index = random.nextInt(alarm.getImages().size());
            path = alarm.getImages().get(index);
            refPhoto = new File(path);
            if (!refPhoto.exists()) {
                alarm.getImages().remove(index);
                fileExists = false;
            } else {
                fileExists = true;
            }
        } while (!fileExists && !alarm.getImages().isEmpty());

        if (fileExists) {
            Glide.with(this)
                    .load(path)
                    .fitCenter()
                    .into(photoPreview);

            Glide.with(this)
                    .load(path)
                    .fitCenter()
                    .into(photoFull);
        } else {
            callMathTask();
        }
    }

    private void repeat() {
        Toast.makeText(this, String.format("Будильник будет повторен через %s минут", interval),
                Toast.LENGTH_SHORT).show();

        Intent myIntent = new Intent(this, WakeUpBroadcastReceiver.class);
        myIntent.putExtra(this.getString(R.string.intent_alarm), alarm);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, interval);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        terminate();
    }

    private void terminate() {
        Utils.lockOff();
        player.stop();
        if (vibrator != null)
            vibrator.cancel();
//        Utils.lockScreen(this);
        if (Build.VERSION.SDK_INT > 21)
            finishAndRemoveTask();
        else {
            int iWantToExit = 10 / 0; // TODO
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            // lock volume
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
            // it works on some old devices
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_HOME:
                return false;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null){
            camera.release();
            camera = null;
        }
        if (isApplicationSentToBackground(this)) {
            if (Build.VERSION.SDK_INT > 21)
                finishAndRemoveTask();
        }
    }

    @Override
    protected void onDestroy() {
        Intent myIntent = new Intent(this, WakeUpBroadcastReceiver.class);
        myIntent.putExtra(this.getString(R.string.intent_alarm), alarm);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (camera == null) {
                camera = getCameraInstance();
            }
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d("Camera", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (surfaceHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (Exception e){
            Log.d("Camera", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    private boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}