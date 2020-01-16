package ru.mozgovoy.oleg.myhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * Created by Oleg on 02.04.2015.
 */
public class MyTools {

    public static final String APP_CONTROL_DOOR = "enabledoorcontrol";
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_SSHSERVER = "sshserver";
    public static final String APP_PREFERENCES_SSHUSER = "sshuser";
    public static final String APP_PREFERENCES_SSHPASS = "sshpass";
    public static final String APP_PREFERENCES_SSHCOMMANDS = "sshcommands";
    public static final String APP_PREFERENCES_SMS_NUMBERS = "smsnumbers";
    public static final String APP_PREFERENCES_SMS_COMMANDS = "smscommands";

    public static SharedPreferences mSettings;

    public static void openSettings(Context context) {
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static void saveSettingsSSH(String server, String user, String pass, Set<String> setCommands) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.remove(APP_PREFERENCES_SSHSERVER);
        editor.remove(APP_PREFERENCES_SSHUSER);
        editor.remove(APP_PREFERENCES_SSHPASS);
        editor.remove(APP_PREFERENCES_SSHCOMMANDS);

        editor.putString(APP_PREFERENCES_SSHSERVER, server);
        editor.putString(APP_PREFERENCES_SSHUSER, user);
        editor.putString(APP_PREFERENCES_SSHPASS, pass);
        editor.putStringSet(APP_PREFERENCES_SSHCOMMANDS, setCommands);

        editor.commit();
    }

    public static void saveSettingsSMS(Set<String> smsNumbers, Set<String> smsCommands) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.remove(APP_PREFERENCES_SMS_NUMBERS);
        editor.remove(APP_PREFERENCES_SMS_COMMANDS);

        editor.putStringSet(APP_PREFERENCES_SMS_NUMBERS, smsNumbers);
        editor.putStringSet(APP_PREFERENCES_SMS_COMMANDS, smsCommands);

        editor.commit();
    }

    public static String get(String setting, String defaultValue) {
        if (mSettings.contains(setting)) {
            return mSettings.getString(setting, defaultValue);
        }
        return defaultValue;
    }


    public static void appendLog(String text) {
        File logFile = new File(Environment.getExternalStorageDirectory() + "MyHomeLog.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.newLine();
            buf.append("--- start of log message ---");
            buf.newLine();
            buf.append(text);
            buf.newLine();
            buf.append("--- end of log message ---");
            buf.newLine();
            buf.close();
        } catch (IOException e) {
        }
    }

    public static String getExceptionTextForSms(Exception exc){
        String r = "exc: ";
        if (exc.getMessage() != null) {
            if (exc.getMessage().length() > 40) {
                r += exc.getMessage().substring(0, 40);
            } else {
                r += exc.getMessage();
            }
        }
        return r;
    }
}
