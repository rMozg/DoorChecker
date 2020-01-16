package ru.mozgovoy.oleg.myhome;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ru.mozgovoy.oleg.myhome.SMS.SMSMonitor;

public class AutoControlDoorService extends Service {
    private static int NOTIFICATION_ID = 1;

    public static Thread autoControlThread = null;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // code to execute when the service is first created
    }

    @Override
    public void onDestroy() {
    }

    protected void showNotification() {
        Notification notification = new Notification();
        notification.icon = R.drawable.construction;
        notification.tickerText = "tickerText";
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.when = System.currentTimeMillis();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        notification.setLatestEventInfo(getApplicationContext(), "MyHome автоконтроль", "сервис запущен", contentIntent);
        startForeground(NOTIFICATION_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        showNotification();

        if (autoControlThread != null) {
            try {
                autoControlThread.interrupt();
            } catch (Exception exc) {
                MyTools.appendLog("сбой при прерывании фонового потока в сервисе" + exc.getMessage());
            }
        }
        autoControlThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int timeout = 20;//интервал проверки замка, в секундах

                    int maxOpenTimes = 6;//количество последовательных проверок завершившихся открытым состоянием, после которых шлётся смс
                    int openTimes = 0;//тут хранится количество последовательных раз проверок при открытом состоянии

                    int maxErrTimes = 3;//количество последовательных проверок завершившихся ошибкой, после которых шлётся смс
                    int errTimes = 0;//тут хранится количество последовательных раз проверок с ошибкой

                    boolean work = MyTools.mSettings.getBoolean(MyTools.APP_CONTROL_DOOR, false);
                    boolean newwork = work; //для контроля запуска/остановки сервиса

                    while (true) {

                        try {
                            Thread.sleep(timeout * 1000);
                        } catch (Exception exc) {
                            MyTools.appendLog("ошибка во время Thread.sleep\n" + exc.getMessage());
                        }

                        if (work) {
                            JeromeDriver.DigitalResult rLock = new JeromeDriver.DigitalResult(true, false, "");
                            JeromeDriver.DigitalResult rDoor = new JeromeDriver.DigitalResult(true, false, "");

                            try {
                                rLock = DoorMethods.checkLock();
                                rDoor = DoorMethods.checkDoor();
                                if (rLock.hasError || rDoor.hasError) {
                                    errTimes++;
                                } else {
                                    if (rLock.state && rDoor.state) {
                                        openTimes = 0;
                                        errTimes = 0;
                                    } else if (rLock.state && !rDoor.state) {
                                        errTimes++;
                                    } else if (!rLock.state && !rDoor.state) {
                                        openTimes++;
                                    } else if (!rLock.state && rDoor.state) {
                                        openTimes++;
                                    }
                                }
                            } catch (Exception exc) {
                                errTimes++;
                                MyTools.appendLog("ошибка внутри while(true){} в блоке if(work){}\n" + exc.getMessage());
                            }

                            if (openTimes == maxOpenTimes) {
                                SMSMonitor.broadcastSMS("замок открыт слишком долго");
                            }
                            if (errTimes == maxErrTimes) {
                                if (rLock.state && !rDoor.state) {
                                    SMSMonitor.broadcastSMS("открытая дверь при закрытом замке!");
                                } else {
                                    SMSMonitor.broadcastSMS("какая-то ошибка при автопроверке");
                                }
                            }
                        }

                        newwork = MyTools.mSettings.getBoolean(MyTools.APP_CONTROL_DOOR, false);
                        if (work != newwork) {
                            if (newwork) {
                                showNotification();
                                SMSMonitor.broadcastSMS("сервис автоконтроля запущен");
                            } else {
                                stopForeground(true);
                                SMSMonitor.broadcastSMS("сервис автоконтроля остановлен");
                            }
                        }
                        work = newwork;
                    }
                } catch (Exception exc) {
                }
            }
        });

        autoControlThread.start();

        return START_STICKY;
    }

}