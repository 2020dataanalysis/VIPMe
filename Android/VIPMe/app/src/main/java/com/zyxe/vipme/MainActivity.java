package com.zyxe.vipme;

//  How to Start a Foreground Service in Android (With Notification Channels)
//      https://www.youtube.com/watch?v=FbpD5RZtbCc&t=142s
//      https://codinginflow.com/tutorials/android/foreground-service

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private static final String TAG = "SAM";
    private static final String ON1 = "ON1";
    // private static final String LC = "LC";

    public TextView VIP_Site_TextView;
    public TextView messageLog_TextView;     //  See status_Garage
    public Button garage_Button;
    private TextView device_id_TextView;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiver_VIP;
    private boolean service_started = false;
    private String android_id = "";
    private Spinner VIP_location_spinner;
    private Switch updates_switch;

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(ON1, "onResume");
        if (broadcastReceiver == null)
        {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    Log.i("VIP", "MainActivity");
                    Log.i("VIP", intent.getAction() );
                    //String message = intent.getExtras().get("message").toString();
                    //messageLog_TextView.append(intent.getExtras().get("garage_open").toString() + "\n");
                    messageLog_TextView.append(intent.getExtras().get("message").toString() + "\n");
                    if ( intent.getExtras().getBoolean("updates"))
                        updates_switch.setChecked( true );
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("UI"));

        if (broadcastReceiver_VIP == null)
        {
            broadcastReceiver_VIP = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    Log.i("VIP", "VIP");
                    Log.i("VIP", intent.getAction() );
                    String message = intent.getExtras().get("message").toString();
                    VIP_Site_TextView.setText( intent.getExtras().get("message").toString() );
                }
            };
        }
        registerReceiver(broadcastReceiver_VIP, new IntentFilter("VIP"));


        broadcast("update_UI");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(ON1, "onDestroy");
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(ON1, "onCreate");

        VIP_location_spinner = findViewById(R.id.location );
        ArrayAdapter<CharSequence> command_Adapter = ArrayAdapter.createFromResource(this, R.array.commands, android.R.layout.simple_spinner_item );
        command_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        VIP_location_spinner.setAdapter( command_Adapter );
        VIP_location_spinner.setOnItemSelectedListener( this );

        VIP_Site_TextView = findViewById(R.id.VIP_Site);
        messageLog_TextView = findViewById(R.id.messageLog_TextView);
        garage_Button = findViewById(R.id.garage_Button);
//        connect_Button.setText("Connect");
        messageLog_TextView.setMovementMethod(new ScrollingMovementMethod());
//        device_id_TextView = (TextView) findViewById(R.id.device_id);

        StringBuilder sb = new StringBuilder();
        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        sb.append("Android ID: " + android_id  + "\n");
//        device_id_TextView.setText(sb.toString());

        Log.i("save", "onCreate - 145 " + service_started );

        if (!runtime_permissions()) {
            updates_switch = (Switch) findViewById(R.id.updates_switch);

            enable_buttons();
            Log.i("save", "148 " + service_started );

        }
    }


    private void enable_buttons() {
        Log.i(ON1, "enable_buttons()");
        garage_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broadcast( "Garage");
            }
        });
    }


    private void broadcast(String value )
    {
        Log.i(ON1, "broadcast");
        Intent i = new Intent("commands");
        i.putExtra("command", value);
        sendBroadcast(i);
    }


    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            } else {
                runtime_permissions();
            }
        }
    }

    public void start_service()
    {
        //  First check if service is already running.

        Log.i(ON1, "start_service()");
        Intent serviceIntent = new Intent(this, VIPService.class);
        serviceIntent.putExtra("android_id", android_id );
        serviceIntent.putExtra( "VIP_location", VIP_location_spinner.getSelectedItem().toString() );
        ContextCompat.startForegroundService(this, serviceIntent);
    }


    public void startService(View v)
    {
        Log.i(ON1, "startService - 218" + service_started);
        start_service();
    }



    public void stopService(View v) {
//        Intent serviceIntent = new Intent( this, ExampleService.class);
//        stopService(serviceIntent);
        Log.i(ON1, "stopService - 218" + service_started );
        broadcast("exit");
        finish();       // Does not clear it from cache.  It will restart from cache with out any memory.
    }

    private void snooze(int t){

        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void updateUI(View view)
    {
        messageLog_TextView.setText("");
        broadcast("update_UI");
    }

    public void door_Status(View view) {
        broadcast( "Status");
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String text = adapterView.getItemAtPosition( i ).toString();
        Toast.makeText( adapterView.getContext(), text, Toast.LENGTH_LONG).show();
        //broadcast( "location=" + Integer.toString( i ) );
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void door_Status_event(View view)
        {
            broadcast( "Status_Event");
    }

    public void receive_server_updates(View view) {
        Log.i("SWITCH", "MainActivity");
        broadcast("receive_server_updates=" + Boolean.toString( updates_switch.isChecked() ));
    }
}