package com.weiaett.cruelalarm.utils;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.weiaett.cruelalarm.sheduling.AlarmServiceBroadcastReceiver;
import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.models.Alarm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Weiss_A on 28.09.2016.
 * Useful stuff
 */

public class Utils {

    public static String getFormattedTime(Context context, Calendar calendar) {
        String format = context.getString(R.string.time_format);
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        return sdf.format(calendar.getTime().getTime());
    }

    public static void sortAlarms(List<Alarm> alarms) {
        Collections.sort(alarms, new Comparator<Alarm>() {
            @Override
            public int compare(Alarm first, Alarm second) {
                return first.getTime().compareTo(second.getTime());
            }
        });
    }

    public static void callTonePicker(Context context, Uri defaultUri, int requestCode) {
        final Intent ringtoneIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultUri);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultUri);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.tone_picker_title));
        ((Activity) context).startActivityForResult(ringtoneIntent, requestCode);
    }

    public static void expand(final View view) {
        view.setVisibility(View.VISIBLE);
        int finalHeight = dpToPx(155); // TODO: get real height
        ValueAnimator animator = slideAnimator(view, 0, finalHeight);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    public static void collapse(final View view) {
        int finalHeight = view.getHeight();
        ValueAnimator animator = slideAnimator(view, finalHeight, 0);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    private static ValueAnimator slideAnimator(final View view, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    public static void callAlarmScheduleService(Context context) {
        Intent alarmServiceIntent = new Intent(context, AlarmServiceBroadcastReceiver.class);
        context.sendBroadcast(alarmServiceIntent);
    }

    private static PowerManager.WakeLock wakeLock = null;
    public static void lockOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null)
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP, "CRUEL_ALARM");
        wakeLock.acquire();
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("CRUEL_ALARM");
        keyguardLock.disableKeyguard();
    }

    public static void lockOff() {
        try {
            if (wakeLock != null)
                wakeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }
}
