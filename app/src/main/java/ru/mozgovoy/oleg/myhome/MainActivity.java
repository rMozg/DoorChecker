package ru.mozgovoy.oleg.myhome;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ru.mozgovoy.oleg.myhome.SMS.SMSMonitor;
import ru.mozgovoy.oleg.myhome.SMS.SMSSettings;
import ru.mozgovoy.oleg.myhome.SSH.SSHSettings;


public class MainActivity extends ActionBarActivity {

    static Session session;
    EditText etResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyTools.openSettings(this);

//        MyTools.mSettings.edit().putBoolean(MyTools.APP_CONTROL_DOOR, true).commit();
        if (MyTools.mSettings.getBoolean(MyTools.APP_CONTROL_DOOR, false)) {
            startService(new Intent(this, AutoControlDoorService.class));
        }

        String server = MyTools.get(MyTools.APP_PREFERENCES_SSHSERVER, "");
        String user = MyTools.get(MyTools.APP_PREFERENCES_SSHUSER, "");
        String pass = MyTools.get(MyTools.APP_PREFERENCES_SSHPASS, "");
        try {
            session = new GetSession().execute(server, user, pass).get();
        } catch (Exception exc) {
        }

        Button butSSHSettings = (Button) findViewById(R.id.buttonOpenSSHSettings);
        butSSHSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setIntent = new Intent(MainActivity.this, SSHSettings.class);
                startActivity(setIntent);
            }
        });

        Button butSMSSettings = (Button) findViewById(R.id.buttonOpenSMSSettings);
        butSMSSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setIntent = new Intent(MainActivity.this, SMSSettings.class);
                startActivity(setIntent);
            }
        });

        Button buttonCheckDoor = (Button) findViewById(R.id.buttonCheckDoor);
        buttonCheckDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String answer = new SMSMonitor.MyAsyncTaskProcessSMS(getApplicationContext()).execute("", "toast-охрана").get();
                    Toast.makeText(MainActivity.this, answer, Toast.LENGTH_LONG).show();
                } catch (Exception exc) {
                }
            }
        });

        boolean current = MyTools.mSettings.getBoolean(MyTools.APP_CONTROL_DOOR, false);
        final Button buttonStopControl = (Button) findViewById(R.id.buttonStopControl);
        if (current) {
            buttonStopControl.setText("Стоп контроль");
        } else {
            buttonStopControl.setText("Старт контроль");
        }
        buttonStopControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String answer = new SMSMonitor.MyAsyncTaskProcessSMS(getApplicationContext()).execute("", "toast-startstop-control").get();
                    String message = "";
                    switch (answer) {
                        case "true":
                            message = "Сервис автоконтроля запущен";
                            buttonStopControl.setText("Стоп контроль");
                            break;
                        case "false":
                            message = "Сервис автоконтроля остановлен";
                            buttonStopControl.setText("Старт контроль");
                            break;
                    }
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                } catch (Exception exc) {
                }
            }
        });

        Set<String> setOfCommands = MyTools.mSettings.getStringSet(MyTools.APP_PREFERENCES_SSHCOMMANDS, new HashSet<String>());
        ArrayList listOfCommands = new ArrayList<>(setOfCommands);
        Collections.sort(listOfCommands);
        etResult = (EditText) findViewById(R.id.editTextResult);
        ListView lvButtons = (ListView) findViewById(R.id.listViewButtons);
        MyCommandsAdapter ma = new MyCommandsAdapter(this, listOfCommands);
        lvButtons.setAdapter(ma);
        ma.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ListView lvButtons = (ListView) findViewById(R.id.listViewButtons);
        ((MyCommandsAdapter) lvButtons.getAdapter()).notifyDataSetChanged();
    }

    class GetSession extends AsyncTask<String, Void, Session> {
        protected Session doInBackground(String... args) {

            final String host = args[0];
            final String user = args[1];
            final String pass = args[2];
            Session session = null;
            try {
                JSch jsch = new JSch();

                session = jsch.getSession(user, host, 22);
                session.setPassword(pass);

                UserInfo ui = new MyUserInfo() {
                };
                session.setUserInfo(ui);
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);

                session.connect(3000);
            } catch (Exception e) {
                System.out.println(e);
            }
            return session;
        }
    }


    class ExecCommand extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... args) {
            String output = "";
            try {
                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(args[0]);
                channel.connect(3000);

                final InputStream commandOutput = channel.getInputStream();
                StringBuilder outputBuffer = new StringBuilder();
                if (true) {
                    int readByte = 1;
                    ExecutorService executor = Executors.newFixedThreadPool(2);
                    Callable<Integer> readTask = new Callable<Integer>() {
                        @Override
                        public Integer call() throws Exception {
                            return commandOutput.read();
                        }
                    };
                    while (readByte >= 0) {
                        Future<Integer> future = executor.submit(readTask);
                        readByte = future.get(1000, TimeUnit.MILLISECONDS);
                        if (readByte >= 0) {
                            outputBuffer.append((char) readByte);
                        }
                    }

//                    int readByte = commandOutput.read();
//                    while (readByte != 0xffffffff) {
//                        outputBuffer.append((char) readByte);
//                        readByte = commandOutput.read();
//                    }
                    output = outputBuffer.toString();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            return output;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


    public static abstract class MyUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassword() {
            return null;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return false;
        }

        public boolean promptPassword(String message) {
            return false;
        }

        public void showMessage(String message) {
        }

        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo) {
            return null;
        }
    }


    private class MyCommandsAdapter extends ArrayAdapter<String> {

        public MyCommandsAdapter(Context context, List<String> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final String myCommandData = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_main_activity, parent, false);
            }

            int newLinePos = myCommandData.indexOf(System.getProperty("line.separator"));
            final String name = myCommandData.substring(0, newLinePos);
            final String command = myCommandData.substring(newLinePos + 1, myCommandData.length());

            Button butExecute = (Button) convertView.findViewById(R.id.buttonExecute);
            butExecute.setText(name);

            butExecute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String res = new ExecCommand().execute(command).get();
                        etResult.setText(res);
                    } catch (Exception exc) {
                    }
                }
            });

            return convertView;
        }
    }
}
