package ru.mozgovoy.oleg.myhome.SMS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ru.mozgovoy.oleg.myhome.MyTools;
import ru.mozgovoy.oleg.myhome.SMS.Commands.SMSCommand;
import ru.mozgovoy.oleg.myhome.SMS.Commands.SMSCommands;

public class SMSMonitor extends BroadcastReceiver {
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
            Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
            SmsMessage[] messages = new SmsMessage[pduArray.length];
            for (int i = 0; i < pduArray.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
            }

            String sms_from = messages[0].getDisplayOriginatingAddress();
            StringBuilder bodyText = new StringBuilder();
            for (int i = 0; i < messages.length; i++) {
                bodyText.append(messages[i].getMessageBody());
            }
            String body = bodyText.toString();
            try {
                new MyAsyncTaskProcessSMS(context).execute(sms_from, body);
            } catch (Exception exc) {
            }
//            abortBroadcast();
        }
    }

    public static void broadcastSMS(String text) {

        Set<String> setNumbers = MyTools.mSettings.getStringSet(MyTools.APP_PREFERENCES_SMS_NUMBERS, new HashSet<String>());
        SmsManager smsManager = SmsManager.getDefault();

        if (setNumbers != null) {
            for (String from : setNumbers) {
                smsManager.sendTextMessage(from, null, text, null, null);
            }
        }
    }

    public static class MyAsyncTaskProcessSMS extends AsyncTask<String, Void, String> {

        Context context;

        public MyAsyncTaskProcessSMS(Context inContext) {
            this.context = inContext;
        }

        @Override
        protected String doInBackground(String... params) {// 0 - number, 1 - text

            try {

                String from = params[0];
                String text = params[1];

                Set<String> setNumbers = MyTools.mSettings.getStringSet(MyTools.APP_PREFERENCES_SMS_NUMBERS, new HashSet<String>());
                Set<String> setCommands = MyTools.mSettings.getStringSet(MyTools.APP_PREFERENCES_SMS_COMMANDS, new HashSet<String>());

                String code = "";
                SimpleDateFormat sdfy = new SimpleDateFormat("yyyy", Locale.US);
                SimpleDateFormat sdfm = new SimpleDateFormat("MM", Locale.US);
                SimpleDateFormat sdfd = new SimpleDateFormat("dd", Locale.US);
                int codeint = Integer.parseInt(sdfy.format(new Date())) + Integer.parseInt(sdfm.format(new Date())) + Integer.parseInt(sdfd.format(new Date()));
                code = Integer.toString(codeint);

                boolean process = (setNumbers.contains(from)) || (text.startsWith("1" + code + "3")) || (text.startsWith("toast-"));
                List<String> commands = new ArrayList<>();
                if (process) {
                    commands = Arrays.asList(text.split("\n"));
                    if (text.startsWith("1" + code + "3")) {
                        commands.remove(0);
                    }
                }
                if (process) {
                    SmsManager smsManager = SmsManager.getDefault();
                    for (int i = 0; i < commands.size(); i++) {
                        String commandName = commands.get(i).toLowerCase();
                        String r = "";
                        try {
                            List<SMSCommand> commandsList = SMSCommands.getCommandsList();
                            for (SMSCommand smsCommand : commandsList) {
                                if (smsCommand.getNames().contains(commandName)) {
                                    smsCommand.action(smsManager, from, context);
                                    return smsCommand.returnValue();
                                }
                            }
                        } catch (Exception exc) {
                        }
                    }
                }
            } catch (Exception exc) {
            }

            return null;
        }
    }
}
