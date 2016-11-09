package com.weiaett.cruelalarm.sheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.WakeUpActivity;
import com.weiaett.cruelalarm.models.Alarm;
import com.weiaett.cruelalarm.utils.Utils;

public class WakeUpBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WakeUpBroadcastReceiver", "onReceive()");
        Utils.lockOn(context);
        Intent alarmServiceIntent = new Intent(context, AlarmServiceBroadcastReceiver.class);
        context.sendBroadcast(alarmServiceIntent, null);

        Bundle bundle = intent.getExtras();
        final Alarm alarm = (Alarm) bundle.getSerializable(context.getString(R.string.intent_alarm));

        Intent wakeUpIntent = new Intent(context, WakeUpActivity.class);
        wakeUpIntent.putExtra(context.getString(R.string.intent_alarm), alarm);
        wakeUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(wakeUpIntent);
    }
}
