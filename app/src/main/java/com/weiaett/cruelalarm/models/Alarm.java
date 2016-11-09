package com.weiaett.cruelalarm.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.sheduling.WakeUpBroadcastReceiver;
import com.weiaett.cruelalarm.utils.Weekday;
import com.weiaett.cruelalarm.utils.DBHelper;
import com.weiaett.cruelalarm.utils.Utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Weiss_A on 26.09.2016.
 * Alarm Model
 */
public class Alarm implements Serializable{

    private transient EnumSet<Weekday> days = EnumSet.noneOf(Weekday.class);

    private int id;
    private boolean isActive = true;
    private transient Uri toneUri;
    private String toneUriString;
    private String tone;
    private boolean hasVibration = false;
    private String description = "";
    private String time;
    private transient Context context;
    private List<String> images;

    public Alarm(Context context, String time) {
        SharedPreferences config = context.getSharedPreferences(context.
                getString(com.weiaett.cruelalarm.R.string.sp_config), Context.MODE_PRIVATE);
        this.context = context;
        this.time = time;
        this.toneUri = Uri.parse(config.getString(context.getString(com.weiaett.cruelalarm.R.string.sp_config_tone_uri),
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()));
        this.toneUriString = this.toneUri.toString();
        this.tone = RingtoneManager.getRingtone(context, this.toneUri).getTitle(context);
        this.hasVibration = config.getBoolean(context.getString(com.weiaett.cruelalarm.R.string.sp_config_vibration), false);
    }

    public Alarm(Context context) {
        this(context, context.getString(R.string.time_default));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Calendar getAlarmTime() {
        Calendar calendar = Calendar.getInstance();
        int currDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (currDay != 1) {
            currDay--;
        } else {
            currDay = 7;
        }
        currDay--;

        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.substring(3)));
        calendar.set(Calendar.SECOND, 0);

        if (this.getDays().isEmpty() || this.getDays().size() == 7) {
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        } else {
            int chosenDay = -1;
            for (Weekday day: getDays()) {
                if (day.ordinal() > currDay || day.ordinal() == currDay && calendar.after(Calendar.getInstance())) {
                    chosenDay = day.ordinal();
                    break;
                }
            }
            if (chosenDay == -1) {
                chosenDay = getDays().iterator().next().ordinal();
            }
            chosenDay++;
            if (chosenDay != 7) {
                chosenDay++;
            } else {
                chosenDay = 1;
            }
            calendar.set(Calendar.DAY_OF_WEEK, chosenDay);
        }
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        return calendar;
    }

    public void schedule(Context context) {

        Intent myIntent = new Intent(context, WakeUpBroadcastReceiver.class);
        myIntent.putExtra(context.getString(R.string.intent_alarm), this);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long time = getAlarmTime().getTimeInMillis();
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        Log.d("NextAlarm", String.format("%d", time));
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public Uri getToneUri() {
        return toneUri;
    }

    public void setToneUri(Uri toneUri) {
        this.toneUri = toneUri;
        this.toneUriString = toneUri.toString();
    }

    public String getToneUriString() {
        return toneUriString;
    }

    public boolean getHasVibration() {
        return hasVibration;
    }

    public void setHasVibration(boolean hasVibration) {
        this.hasVibration = hasVibration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
        DBHelper.getInstance(context).setAlarmPhotos(this.getId(), images);
    }

    public EnumSet<Weekday> getDays() {
        return days;
    }

    public void addDay(Weekday day){
        days.add(day);
    }

    public void removeDay(Weekday day) {
        days.remove(day);
    }

    public void deleteFromDatabase() {
        DBHelper.getInstance(context).deleteAlarm(this);
        Utils.callAlarmScheduleService(context);
    }

    public void addToDatabase() {
        DBHelper.getInstance(context).addAlarm(this);
        Utils.callAlarmScheduleService(context);
    }

    public void updateInDatabase() {
        DBHelper.getInstance(context).updateAlarm(this);
        Utils.callAlarmScheduleService(context);
    }

    public void updateAttributesInDatabase() {
        DBHelper.getInstance(context).updateAlarm(this);
    }
}