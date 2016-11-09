package com.weiaett.cruelalarm.sheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.models.Alarm;
import com.weiaett.cruelalarm.utils.DBHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AlarmService extends Service {
    public AlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(this.getClass().getSimpleName(),"onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(this.getClass().getSimpleName(),"onStartCommand()");
        Alarm alarm = getNext();
        if (alarm != null) {
            alarm.schedule(getApplicationContext());
        } else {
            Intent myIntent = new Intent(getApplicationContext(), WakeUpBroadcastReceiver.class);
            myIntent.putExtra(this.getString(R.string.intent_alarm), new Alarm(getBaseContext()));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent,PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

            alarmManager.cancel(pendingIntent);
        }
        return START_NOT_STICKY;
    }

    private Alarm getNext() {
        List<Alarm> alarms = DBHelper.getInstance(this).getAllAlarms(this);
        Set<Alarm> alarmQueue;
        alarmQueue = new TreeSet<>(new Comparator<Alarm>() {
            @Override
            public int compare(Alarm first, Alarm second) {
                int result = 0;
                long diff = first.getAlarmTime().getTimeInMillis() - second.getAlarmTime().getTimeInMillis();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0){
                    return -1;
                }
                return result;
            }
        });

        for(Alarm alarm: alarms) {
            if (alarm.getIsActive())
                alarmQueue.add(alarm);
        }
        if (alarmQueue.iterator().hasNext()) {
            return alarmQueue.iterator().next();
        } else {
            return null;
        }
    }
}
