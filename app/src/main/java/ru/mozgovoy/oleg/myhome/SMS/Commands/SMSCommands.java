package ru.mozgovoy.oleg.myhome.SMS.Commands;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

import java.util.ArrayList;
import java.util.List;

import ru.mozgovoy.oleg.myhome.DoorMethods;
import ru.mozgovoy.oleg.myhome.MyTools;

public class SMSCommands {
    public static List<SMSCommand> getCommandsList() {
        List<SMSCommand> commands = new ArrayList<>();

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("ping");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
                smsManager.sendTextMessage(from, null, "pong", null, null);
            }
        });

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("пинг");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
                smsManager.sendTextMessage(from, null, "понг", null, null);
            }
        });

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("контроль стоп");
                names.add("стоп контроль");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
                MyTools.mSettings.edit().putBoolean(MyTools.APP_CONTROL_DOOR, false).commit();
            }
        });

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("контроль старт");
                names.add("старт контроль");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
                MyTools.mSettings.edit().putBoolean(MyTools.APP_CONTROL_DOOR, true).commit();
            }
        });

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("дверь");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
                smsManager.sendTextMessage(from, null, DoorMethods.pubCheckDoor(), null, null);
            }
        });

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("замок");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
                smsManager.sendTextMessage(from, null, DoorMethods.pubCheckLock(), null, null);
            }
        });

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("охрана");
                names.add("безопасность");
                names.add("вход");
                names.add("выход");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
                smsManager.sendTextMessage(from, null, DoorMethods.pubCheckSecure(), null, null);
            }
        });

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("перезвони");
                names.add("позвони");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("tel:" + from));
                context.startActivity(intent);
            }
        });

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("toast-startstop-control");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
            }

            @Override
            public String returnValue() {
                boolean current = MyTools.mSettings.getBoolean(MyTools.APP_CONTROL_DOOR, false);
                current = !current;
                MyTools.mSettings.edit().putBoolean(MyTools.APP_CONTROL_DOOR, current).commit();
                return current ? "true" : "false";
            }
        });

        commands.add(new SMSCommand() {
            @Override
            public List<String> getNames() {
                List<String> names = new ArrayList<>();
                names.add("toast-охрана");
                return names;
            }

            @Override
            public void action(SmsManager smsManager, String from, Context context) {
            }

            @Override
            public String returnValue() {
                return DoorMethods.pubCheckSecure();
            }
        });

        return commands;
    }
}
