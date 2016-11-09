package com.weiaett.cruelalarm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.weiaett.cruelalarm.photo_manager.PhotoManagerActivity;
import com.weiaett.cruelalarm.utils.Utils;


public class SettingsActivity extends AppCompatActivity {

    private TextView tvToneLabel;
    private TextView tvTone;
    private Switch swHasVibration;
    private TextView tvIntervalLabel;
    private TextView tvInterval;
    private TextView tvAbout;
    private TextView tvPhoto;
    private SharedPreferences config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tvToneLabel = (TextView) findViewById(R.id.tvToneLabel);
        tvTone = (TextView) findViewById(R.id.tvTone);
        swHasVibration = (Switch) findViewById(R.id.switchVibration);
        tvIntervalLabel = (TextView) findViewById(R.id.tvIntervalLabel);
        tvInterval = (TextView) findViewById(R.id.tvInterval);
        tvAbout = (TextView) findViewById(R.id.tvAbout);
        tvPhoto = (TextView) findViewById(R.id.tvPhotoManager);
        config = this.getSharedPreferences(this.getString(R.string.sp_config), Context.MODE_PRIVATE);

        tvTone.setText(config.getString(this.getString(R.string.sp_config_tone),
                getString(com.weiaett.cruelalarm.R.string.label_default)));
        swHasVibration.setChecked(config.getBoolean(this.getString(R.string.sp_config_vibration), false));
        tvInterval.setText(String.format(this.getString(R.string.formatted_interval),
                (config.getInt(this.getString(R.string.sp_config_interval),
                        Integer.parseInt(getString(com.weiaett.cruelalarm.R.string.default_interval))))));

        setupViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent ringtoneIntent) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Uri uri = ringtoneIntent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            String title = RingtoneManager.getRingtone(SettingsActivity.this, uri).
                    getTitle(SettingsActivity.this);
            SharedPreferences.Editor editor = config.edit();
            editor.putString(this.getString(R.string.sp_config_tone), title);
            editor.putString(this.getString(R.string.sp_config_tone_uri), uri.toString());
            editor.apply();
            tvTone.setText(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        tvTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.callTonePicker(SettingsActivity.this, Uri.parse(config.getString
                        (getBaseContext().getString(R.string.sp_config_tone_uri),
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString())), 0);
            }
        });
        tvToneLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.callTonePicker(SettingsActivity.this, Uri.parse(config.getString
                        (getBaseContext().getString(R.string.sp_config_tone_uri),
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString())), 0);
            }
        });
        swHasVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = config.edit();
                editor.putBoolean(getBaseContext().getString(R.string.sp_config_vibration), isChecked);
                editor.apply();
            }
        });
        tvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, PhotoManagerActivity.class);
                startActivity(intent);
            }
        });
        tvInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callIntervalTimePicker();
            }
        });
        tvIntervalLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callIntervalTimePicker();
            }
        });
        tvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAboutDialog();
            }
        });
    }

    private void callIntervalTimePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final NumberPicker numberPicker = new NumberPicker(this);
        setDividerColor(numberPicker, R.color.colorAccent);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMinValue(5);
        numberPicker.setMaxValue(15);
        numberPicker.setValue(5);
        numberPicker.setWrapSelectorWheel(false);
        builder.setPositiveButton(com.weiaett.cruelalarm.R.string.button_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = config.edit();
                editor.putInt(getBaseContext().getString(R.string.sp_config_interval), numberPicker.getValue());
                editor.apply();
                tvInterval.setText(String.format(getBaseContext().getString(R.string.formatted_interval),
                        numberPicker.getValue()));
            }
        });
        builder.setNegativeButton(com.weiaett.cruelalarm.R.string.button_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        final FrameLayout parent = new FrameLayout(this);
        parent.addView(numberPicker, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
        builder.setView(parent);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void callAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(com.weiaett.cruelalarm.R.string.button_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        final LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        final ImageView logo = new ImageView(this);
        final TextView title = new TextView(this);
        final TextView version = new TextView(this);
        logo.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        title.setText(R.string.label_copyright);
        title.setTextSize(18);
        String versionName = getString(com.weiaett.cruelalarm.R.string.default_version);
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version.setText(String.format(getString(R.string.label_version), versionName));
        version.setTextSize(14);
        parent.addView(logo);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1.0f;
        params.gravity = Gravity.CENTER;
        parent.addView(title, params);
        parent.addView(version, params);
        builder.setTitle(com.weiaett.cruelalarm.R.string.label_about);
        builder.setView(parent);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
