package com.zyxe.vipme;

//  How to Start a Foreground Service in Android (With Notification Channels)
//      https://www.youtube.com/watch?v=FbpD5RZtbCc
//  https://www.tutorialspoint.com/how-to-update-ui-from-intent-service-in-android


import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.time.*;


import static com.zyxe.vipme.App.CHANNEL_ID;

public class VIPService extends Service {
    private static final String TAG = "SAM";
    private static final String ON1 = "ON1";
    private static final String SWITCH = "SWITCH";
    private NotificationManagerCompat notificationManager;
    public double latitude;
    public double longitude;
    public double altitude;
    public double speed_mps;
    public double speed_mph;
    public double speed_mph_max = 0;
    public double distance_max = 0;

    String SERVER_IP;
    int SERVER_PORT;
    Socket socket;
    Thread Thread1 = null;

    PrintWriter output;
    InputStreamReader in;
    BufferedReader br;
    boolean loop = false;
    boolean VIP_OnSite = false;
    String VIP_OnSite_message = "";
    String gpsMe = "";              //  Set gpsMe to "";
    //  otherwise,
    //  passing in Thread → error.

    double lat_A = 37.815767;         //  Garage
    double lon_A = -121.900619;       //  Garage
    int homeRoam = 110;                  //  PC Room is 40.
    // double gps_Starting_Calibration_Distance;
    double distanceTo;
    int pollInterval = 1000;            // ms

    ImageView heartbeat;
    boolean heartbeat_status = false;

    private LocationListener listener;
    private LocationManager locationManager;

    Context globalcontext;
    // MediaPlayer mediaPlayer;
    boolean garage_open = false;
    long start = 0; // = System.currentTimeMillis()/1000;
    long end = 0;
    private BroadcastReceiver broadcastReceiver;
    boolean GPS_fix = false;

    String VIP_ON_SITE = "VIP On Site";
    String VIP_OFF_SITE = "VIP Off Site";

    String android_id = "";
    String VIP_name = "";
    //    boolean greet_VIP = false;

    String VIP_location;
    Boolean receive_server_updates = false;


//    String m3 = "VIP is almost at BEAR:";
//    String m2 = "Welcome to BEAR !";
    String m2 = "Welcome to Blackhawk";
    String m3 = "VIP is almost home:";
    Boolean notification_status = false;


    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate - 113 ******************************** ");
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String message = intent.getExtras().get("command").toString();
                    Log.i("SWITCH", "broadcastreceiver 98: " + message );

                    if (message.equals("exit"))
                    {
                        loop = false;
                        Log.i("exit", "broadcastreceiver 101: " + message );
                        stopSelf();
                    }

                    if (message.equals("Status")) {
                        Log.i(ON1,"onCreate - Status" );
                        new Thread(new Thread3("Status")).start();
                    }

                    if (message.equals("Status_Event")) {
                        Log.i(ON1,"onCreate - Status" );
                        new Thread(new Thread3("Status_Event")).start();
                    }

                    if (message.equals("Garage")) {
                        Log.i(ON1,"onCreate - Garage" );
                        new Thread(new Thread3("Garage")).start();
                    }

                    String d = "[=]";
                    String [] a = message.split(d);

                    if ( a[0].equals("receive_server_updates" ))
                    {
                        receive_server_updates = Boolean.parseBoolean(a[1]);
                        Log.i(SWITCH, "153 receive_server_updates" + receive_server_updates );
                    }

                    if (message.equals("update_UI"))
                    {
                        update_UI();
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("commands"));

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Log.i(TAG, "onCreate - 128 " + location + " *************************** ");
                if (location == null)
                    return;

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                altitude = location.getAltitude();
                speed_mps = location.getSpeed();
                speed_mph = speed_mps * 2.2369362920544;

                if (!GPS_fix)
                {
                    GPS_fix = true;
//                    Log.i(TAG, "onCreate - GPS_fix 136  *************************** ");
                    update_UI();
//                    Log.i(TAG, "onCreate 139 ******************************** ");

                    loop = true;
                    Thread1 = new Thread(new Thread1());
                    Thread1.start();
                }
                // Log.i(TAG, "VIPService - 144 ******************************** ");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, listener);
        notificationManager = NotificationManagerCompat.from(this);
    }       // End of onCreate

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "233 onStartCommand");
        android_id = intent.getStringExtra("android_id");
        VIP_location = intent.getStringExtra("VIP_location");
        new Thread( new Thread3("get_VIP")).start();
        Log.i(TAG, "onStartCommand - exit");
        //  The application will crash if there is no valid VIP.
        return START_STICKY;
    }


    void set_VIP_Notification()
    {
        Log.i(TAG, "244 set_VIP_notification");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sentinel Security")
                .setContentText( VIP_name )
                .setSmallIcon(R.drawable.ic_outline_security_24)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(3, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i(TAG, "onBind");
        return null;
    }



    //Use this method to show toast
    void showToast(final String message) {
        final Context appContext = globalcontext;
        if (null != appContext) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
                }

            });

        }
    }

    private void update_UI(){
        double d = checkDistanceTo();

        if (d < homeRoam) {
            VIP_OnSite = true;
            // Toast.makeText(getApplicationContext(), "Welcome VIP !\nHome Roam\n" + d, Toast.LENGTH_LONG).show();
            VIP_OnSite_message = VIP_ON_SITE;
        } else {
            // Toast.makeText(getApplicationContext(), "Remote VIP\n" + d, Toast.LENGTH_LONG).show();
            VIP_OnSite_message = VIP_OFF_SITE;
        }

        broadcastIntent( "VIP", VIP_OnSite_message );
        // sendOnChannel3( VIP_OnSite_message );
    }


    /**
     * This method checks the distance from one GPS coordinate to another.
     *
     * @return The distance between two GPS coordinates.
     * @author Sam Portillo
     */
    public double checkDistanceTo() {
        final double[] distance = new double[1];        //  Crazy circumvention

        //        if (l == null)
        //            return -1;

        Location locationA = new Location("");
        Location locationB = new Location("");
        locationA.setLatitude(lat_A);
        locationA.setLongitude(lon_A);

        if (speed_mph > speed_mph_max)
            speed_mph_max = speed_mph;

        locationB.setLatitude(latitude);
        locationB.setLongitude(longitude);
        //distance[0] = locationA.distanceTo(locationB);
        distanceTo = locationA.distanceTo(locationB);

        if (distanceTo > distance_max)
            distance_max = distanceTo;

        int a = (int) altitude;
        gpsMe = String.format("Altitude: %d,  Feet: %d,  Speed: %d", a, (int) (distanceTo * 3.28084), (int) speed_mph);

        return distanceTo;
    }

    /**
     * Creates socket connection with server on a new thread.
     * Instantiates the BufferedReader & PrintWriter instances.
     * Creates a new thread2 to receive incoming messages.
     * Continues to loop to feed GPS coordinates to server.
     * If the Disconnect button is pressed then it will stop looping.
     *
     * @author Sam Portillo
     */

    class Thread1 implements Runnable {
        public void run()
        {
            Log.i(TAG, "349 Thread1 - " );
            new Thread(new Thread3( VIP_OnSite_message )).start();

//            long start = java.time.Instant.now().getEpochSecond();
                long end = 0;     // System.currentTimeMillis() / 1000;


            do {
                checkDistanceTo();                                                          // Updates global variable distanceTo
                sendOnChannel2(VIP_OnSite_message);

                String s = String.format("%s %b, %s,  %s %f", "OnSite: ", VIP_OnSite, gpsMe, "Distance: ", distanceTo);
                Log.i(TAG, s);

                if (VIP_OnSite && distanceTo > 10 && speed_mph > 10) {
                    pollInterval = 1000;
                    VIP_OnSite = false;
                    distance_max = 0;
                    speed_mph_max = 0;
//                    Log.i(TAG, "Thread 1 - 360 ******************************** ");
                    update_UI();
                    new Thread(new Thread3("Garage_Close")).start();
                    sleepy(20000);
                }

                //if (VIP_OnSite && distanceTo > 10 && speed_mph > 5 && pollInterval < 30000) {
                if (VIP_OnSite && distanceTo > homeRoam) {
                    // pollInterval = 10000;
                    VIP_OnSite = false;
                    VIP_OnSite_message = VIP_OFF_SITE;
                    broadcastIntent("VIP", VIP_OnSite_message);
                    sendOnChannel2(VIP_OnSite_message);
                    distance_max = 0;
                    speed_mph_max = 0;
//                    Log.i(TAG, "Thread 1 - 360 ******************************** ");
                    update_UI();
                    new Thread(new Thread3(VIP_OFF_SITE)).start();
                }

//                Log.i(TAG, "Thread 1 - 364 ******************************** ");

                if (VIP_OnSite == false) {
                    if (pollInterval <= 1000 && distanceTo > 1000)
                        pollInterval = 30000;

                    if (!VIP_OnSite && distanceTo < 700 && distance_max > 700 && pollInterval > 1000) {
                        pollInterval = 1000;
//                        s = String.format("%s %f\n", "VIP is almost at BEAR: ", distanceTo);
                        s = m3;
                        showToast(s);
                    }

                    if (!VIP_OnSite && distanceTo < 500 && distance_max > 500 && speed_mph_max > 5 && pollInterval > 500) {
                        pollInterval = 100;
                        s = String.format("%s %f\n", m2, distanceTo);
                        showToast(s);
                    }

                    if (!VIP_OnSite && distanceTo < 130 && distance_max > homeRoam + 100 && speed_mph > 15) {
                        VIP_OnSite = true;
                        VIP_OnSite_message = VIP_ON_SITE;
                        broadcastIntent("VIP", VIP_OnSite_message);
                        sendOnChannel2(VIP_OnSite_message);
                        distance_max = 0;
                        speed_mph_max = 0;
                        // pollInterval = 30000;
                        update_UI();
                        new Thread(new Thread3("Garage_Open")).start();
                        new Thread(new Thread3(VIP_ON_SITE)).start();
                        sleepy(20000);
                    }
                }


                if (receive_server_updates && end < System.currentTimeMillis() / 1000) {
                    end = System.currentTimeMillis() / 1000 + 10;
                    System.out.println("time");
//                    Log.i(SWITCH, "Time *******************************");
                    new Thread(new Thread3("Status_Event")).start();
                }


                if ( speed_mph > 20 )
                {
                    String sammy = "gps=" + latitude + ", " + longitude + ", " + speed_mph;
                    new Thread(new Thread3( sammy )).start();
                }

                sleepy( pollInterval );

            } while (loop);

        }
    }






















    /**
     * Send messages to Server & create a new thread if not exit.
     *
     * @param 'String' smsMessage_EditText is the smsMessage_EditText to be sent to the server.
     * @author Sam Portillo
     */
    class Thread3 implements Runnable {
        private String message;

        Thread3(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            Log.i(TAG, "483 Thread3 - " + this.message );

            socket_connection();

//            Log.i("ON1", "553 Thread3 - " + output );
//            boolean b = socket.isConnected();
//            String sb = Boolean.toString( b );
//            Log.i("ON1", "556 Thread3 - boolean " + sb );
//            Log.i("ON1", "557 Thread3 - " + android_id + " " + message );

            output.println( android_id + "=" + message );
            output.flush();
            showToast(message);
            Log.i(TAG, "518 " + message );

            sleepy( 300 );
            socket_read();
            socket_disconnect();
            Log.i(TAG, "530 - Exiting Thread3 *****************" );
        }
    }


    public void sleepy(int i)
    {
        try {
            Thread.sleep( i );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void sendOnChannel1(String message) {
        String title = "Blackhawk Garage Alarm";
        // String message = "Closed";
        // if ( garage_open ) message = "Open";

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_1)
                .setSmallIcon(R.drawable.ic_outline_lock_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }

    public void sendOnChannel2(String title) {
        String message = gpsMe;

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_2)
                .setSmallIcon(R.drawable.ic_twotone_gps_fixed_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        notificationManager.notify(2, notification);
    }


    public void sendOnChannel4( String title, String message ) {
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_4)
                .setSmallIcon(R.drawable.ic_outline_error_outline_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        notificationManager.notify(4, notification);
    }



    public void sendOnChannel3(String message) {
        String title = "Sentinel Security";

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_outline_security_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(3, notification);
    }



    public void broadcastIntent( String action, String value )
    {
        Intent i = new Intent( action );
        i.putExtra("message", value);
        i.putExtra("updates", receive_server_updates);
        sendBroadcast(i);
    }

    public void socket_connection()
    {
        Log.i( TAG, "[" + VIP_location + "]");

        if ( VIP_location.equals("VIPMe - Blackhawk"))
        {
            // SERVER_IP = "10.0.0.34";     // PC
            // SERVER_PORT = 3000;          //  PC
            Log.i(TAG, "socket_connection → VIPMe - Blackhawk");
            //            SERVER_IP = "73.223.16.32";
            SERVER_IP = "24.6.125.77";  //  11.24.2021
            SERVER_PORT = 8070;
        }
        else
        {
            Log.i(TAG, "socket_connection → Man Cave");
            SERVER_IP = "24.6.125.77";  //  11.24.2021
            SERVER_PORT = 5000;
        }


        try {
            Log.i(TAG, "Thread1 - 341 - if server is down → socket will time out in 2 minutes.");
            socket = new Socket(SERVER_IP, SERVER_PORT);

            //                boolean b = socket.isConnected();
            //                String sb = Boolean.toString( b );
            output = new PrintWriter(socket.getOutputStream());
            in = new InputStreamReader(socket.getInputStream());
            br = new BufferedReader(in);
        }
        catch (SocketException e)
        {
            String error_message = "Server is down !";
            Log.i(TAG, error_message );
            showToast( error_message );
            broadcastIntent("UI", error_message );
            sendOnChannel4( "Socket Exception", error_message );
        }
        catch (IOException e) {
            e.printStackTrace();
            String error_message = "Server is down !";
            Log.i(TAG, error_message );
            showToast( error_message );
            broadcastIntent("UI", error_message );
            sendOnChannel4( "IO Exception", error_message );
        }

        //        socket_read();
    }

    public void socket_read()
    {
        try {
            Log.i(ON1, "socket_read");
            String message = "";

            while ( br.ready() )
            {
                message = br.readLine();
                Log.i(TAG, "socket_read - " + message );
                String[] arrOfStr = message.split("=", 2);
                if ( arrOfStr[0].equals("VIP"))
                {
                    VIP_name = arrOfStr[1];
                    set_VIP_Notification();          // 11.25.2021
                }

                if ( arrOfStr[0].equals("notification_status"))
                {
                    notification_status = Boolean.parseBoolean( arrOfStr[1] );
                    if (notification_status)
                    {
                        Log.i(TAG, "notify garage is open");
                        sendOnChannel1("Garage Door is Open !");
                    }
                    else
                    {
                        Log.i(TAG, "notify garage is close");
                        sendOnChannel1("Garage Door is Closed !");
                    }
                }



                broadcastIntent("UI", message);
                showToast(message);
            }

            Log.i(ON1, "socket_read - Out of loop !!!!!!!!!!!!!!!!!!! " );
        } catch (IOException e) {
            //e.printStackTrace();
            // Log.i(TAG, "IO Exception");
        }
    }

    public void socket_disconnect()
    {
        try {
            socket.close();
            in.close();
            br.close();
            output.close();
            // Log.i("exit", message );
            // stopSelf();
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }


}