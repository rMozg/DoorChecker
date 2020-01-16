package ru.mozgovoy.oleg.myhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootOrReplaceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MyTools.openSettings(context);
        if (MyTools.mSettings.getBoolean(MyTools.APP_CONTROL_DOOR, false)) {
            context.startService(new Intent(context, AutoControlDoorService.class));
        }
    }
}
