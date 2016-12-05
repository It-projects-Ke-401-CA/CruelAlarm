package com.weiaett.cruelalarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.support.test.runner.AndroidJUnitRunner;

public final class CustomTestRunner extends AndroidJUnitRunner {


    @Override
    public void onStart() {

        runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Context app = CustomTestRunner.this.getTargetContext().getApplicationContext();

                String name = CustomTestRunner.class.getSimpleName();
                unlockScreen(app, name);
                keepSceenAwake(app, name);
            }
        });

        super.onStart();
    }

    private void keepSceenAwake(Context app, String name) {
        PowerManager power = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        power.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, name)
                .acquire();
    }

    private void unlockScreen(Context app, String name) {
        KeyguardManager keyguard = (KeyguardManager) app.getSystemService(Context.KEYGUARD_SERVICE);
        keyguard.newKeyguardLock(name).disableKeyguard();
    }
}