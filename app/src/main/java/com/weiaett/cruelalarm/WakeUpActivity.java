package com.weiaett.cruelalarm;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.weiaett.cruelalarm.models.Alarm;
import com.weiaett.cruelalarm.sheduling.WakeUpBroadcastReceiver;
import com.weiaett.cruelalarm.utils.Utils;

import java.util.Calendar;

public class WakeUpActivity extends AppCompatActivity {

    MediaPlayer player;
    int interval;
    Alarm alarm;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_up);

        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        try {
            Bundle bundle = this.getIntent().getExtras();
            alarm = (Alarm) bundle.getSerializable(this.getString(R.string.intent_alarm));
        } catch (RuntimeException e) {
            alarm = new Alarm(this);
        }

        ((TextView) this.findViewById(R.id.tvDescription)).setText(alarm.getDescription());

        player = MediaPlayer.create(this, Uri.parse(alarm.getToneUriString()));
        player.setLooping(true);
        player.start();

        if (alarm.getHasVibration()) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 1000, 200, 200, 200 };
            vibrator.vibrate(pattern, 0);
        }

        SharedPreferences config = this.getSharedPreferences(this.getString(R.string.sp_config),
                Context.MODE_PRIVATE);
        interval = config.getInt(this.getString(R.string.sp_config_interval), 5);

        findViewById(R.id.btnRepeat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repeat();
            }
        });
        findViewById(R.id.btnTurnOff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                terminate();
            }
        });
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
        if (Build.VERSION.SDK_INT > 21)
            finishAndRemoveTask();
        else {
            int iWantToExit = 10 / 0; // TODO
        }
    }
}