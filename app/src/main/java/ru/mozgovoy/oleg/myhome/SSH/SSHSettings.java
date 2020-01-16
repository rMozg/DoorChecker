package ru.mozgovoy.oleg.myhome.SSH;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import ru.mozgovoy.oleg.myhome.MyTools;
import ru.mozgovoy.oleg.myhome.R;


public class SSHSettings extends ActionBarActivity {

    private static Set<String> set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sshsettings);

        MyTools.openSettings(this);
        set = MyTools.mSettings.getStringSet(MyTools.APP_PREFERENCES_SSHCOMMANDS, new HashSet<String>());

        final EditText etHost = (EditText) findViewById(R.id.editTextHost);
        final EditText etUser = (EditText) findViewById(R.id.editTextUser);
        final EditText etPass = (EditText) findViewById(R.id.editTextPass);
        final EditText etName = (EditText) findViewById(R.id.editName);
        final EditText etCommand = (EditText) findViewById(R.id.editCommand);

        etHost.setText(MyTools.get(MyTools.APP_PREFERENCES_SSHSERVER, ""));
        etUser.setText(MyTools.get(MyTools.APP_PREFERENCES_SSHUSER, ""));
        etPass.setText(MyTools.get(MyTools.APP_PREFERENCES_SSHPASS, ""));


        Button butShow = (Button) findViewById(R.id.buttonShow);
        butShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < set.size(); i++) {
                    final String str = (String) (set.toArray()[i]);
                    int newLinePos = str.indexOf(System.getProperty("line.separator"));
                    final String name = str.substring(0, newLinePos);
                    final String command = str.substring(newLinePos + 1, str.length());

                    AlertDialog.Builder adShow = new AlertDialog.Builder(SSHSettings.this);
                    adShow.setTitle(name);
                    adShow.setMessage(command);
                    adShow.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    adShow.setNeutralButton("Изменить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            etName.setText(name);
                            etCommand.setText(command);
                        }
                    });
                    adShow.setNegativeButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            set.remove(str);
                        }
                    });
                    adShow.show();
                }
            }
        });


        Button butAdd = (Button) findViewById(R.id.buttonAdd);
        butAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = etName.getText().toString();
                String command = etCommand.getText().toString();

                set.add(name + System.getProperty("line.separator") + command);

                Toast t = Toast.makeText(getBaseContext(), "Сохранено", Toast.LENGTH_SHORT);
                t.show();
            }
        });


        Button butSave = (Button) findViewById(R.id.buttonSave);
        butSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyTools.saveSettingsSSH(etHost.getText().toString(), etUser.getText().toString(), etPass.getText().toString(), set);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
