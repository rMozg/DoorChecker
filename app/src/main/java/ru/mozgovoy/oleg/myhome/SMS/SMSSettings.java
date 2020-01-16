package ru.mozgovoy.oleg.myhome.SMS;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ru.mozgovoy.oleg.myhome.MyTools;
import ru.mozgovoy.oleg.myhome.R;


public class SMSSettings extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smssettings);

        Set<String> setNumbers;
        Set<String> setCommands;

        MyTools.openSettings(this);
        setNumbers = MyTools.mSettings.getStringSet(MyTools.APP_PREFERENCES_SMS_NUMBERS, new HashSet<String>());
        setCommands = MyTools.mSettings.getStringSet(MyTools.APP_PREFERENCES_SMS_COMMANDS, new HashSet<String>());

        final EditText etNumbers = (EditText) findViewById(R.id.editSMSNumbers);
        final EditText etCommands = (EditText) findViewById(R.id.editSMSCommands);

        String num = "";
        for (int i = 0; i < setNumbers.size(); i++) {
            num += setNumbers.toArray()[i];
            if (i < setNumbers.size() - 1) num += "\n";
        }
        etNumbers.setText(num);

        String com = "";
        for (int i = 0; i < setCommands.size(); i++) {
            num += setCommands.toArray()[i];
            if (i < setCommands.size() - 1) num += "\n";
        }
        etCommands.setText(com);

        Button butSave = (Button) findViewById(R.id.buttonSave);
        butSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> num = new HashSet<>(Arrays.asList(etNumbers.getText().toString().split("\n")));
                Set<String> com = new HashSet<>(Arrays.asList(etCommands.getText().toString().split("\n")));
                MyTools.saveSettingsSMS(num, com);
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
