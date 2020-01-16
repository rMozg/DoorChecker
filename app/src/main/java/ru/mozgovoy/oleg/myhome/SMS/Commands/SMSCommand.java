package ru.mozgovoy.oleg.myhome.SMS.Commands;

import android.content.Context;
import android.telephony.SmsManager;

import java.util.List;

public abstract class SMSCommand {
    public abstract List<String> getNames();

    public abstract void action(SmsManager smsManager, String from, Context context);

    public String returnValue() {
        return null;
    }
}
