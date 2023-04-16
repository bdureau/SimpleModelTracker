package com.bdureau.simplemodeltracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bdureau.simplemodeltracker.Help.AboutActivity;
import com.bdureau.simplemodeltracker.Help.HelpActivity;
import com.bdureau.simplemodeltracker.config.AppTabConfigActivity;
import com.bdureau.simplemodeltracker.config.ConfigModules.Config3DR;
import com.bdureau.simplemodeltracker.config.ConfigModules.ConfigBT;
import com.bdureau.simplemodeltracker.config.ConfigModules.ConfigLoraE220;
import com.bdureau.simplemodeltracker.config.ConfigModules.ConfigLoraE32;
import com.bdureau.simplemodeltracker.connection.SearchBluetooth;
import com.bdureau.simplemodeltracker.nmea.Parser;
import com.bdureau.simplemodeltracker.track.TrackGPSTrameFragment;
import com.bdureau.simplemodeltracker.track.TrackInfoFragment;
import com.bdureau.simplemodeltracker.track.TrackMapFragment;

import org.osmdroid.config.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainScreenActivity extends AppCompatActivity {
    public String TAG = "MainScreenActivity";
    private static ViewPager mViewPager; // it is static to avoid losing the map
    String address = null;

    private TextView[] dotsSlide;
    private LinearLayout linearDots;
    public LocationBroadCastReceiver receiver = null;
    SectionsStatusPageAdapter adapter;
    private TrackMapFragment tabPage1 = null;
    private TrackInfoFragment tabPage2 = null;
    private TrackGPSTrameFragment tabPage3 = null;

    private float rocketLatitude = 48.8698f, rocketLongitude = 2.2190f;

    private Button btnDismiss, butAudio, btnConnect;
    private ConsoleApplication myBT;
    Thread rocketTelemetry;
    private boolean telemetry = false;
    private TextToSpeech mTTS;
    //private long lastSpeakTime = 1000;
    //private long distanceTime = 0;

    private long notConnectedTime =0;
    private long lastSpeakNotConnectedTime =1000;

    private long lastReceivedMessageTime = 0;

    private boolean soundOn = true;
    private boolean sateliteAcquisitionCompleteSaid=false;
    Intent locIntent = null;

    UsbManager usbManager;
    UsbDevice device;
    double distance;
    boolean receiving = false;
    Timer timerDistance;
    Timer timerNotReceiving;

    public final String ACTION_USB_PERMISSION = "com.bdureau.simplemodeltracker.USB_PERMISSION";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = true;
                if (android.os.Build.VERSION.SDK_INT < 31)
                    granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);

                if (granted) {
                    Log.d("Flight win", "Permission granted");
                    if (myBT.connect(usbManager, device, Integer.parseInt(myBT.getAppConf().getBaudRateValue()))) {
                        //Log.d("baud rate", "baud:"+myBT.getAppConf().getBaudRateValue());
                        myBT.setConnected(true);
                        Log.d("Flight win", "about to enableUI");
                        myBT.setConnectionType("usb");
                        if (!telemetry) {
                            telemetry = true;
                        }
                        if (soundOn) {
                            mTTS.speak(getString(R.string.connected), TextToSpeech.QUEUE_FLUSH, null);
                        }
                        startTelemetry();
                    }

                } else {
                    msg("PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                msg("I can connect via usb");
                myBT.setConnectionType("usb");
                telemetry = true;
                btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi32x32,
                        0, 0, 0);
                connect();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                if (myBT.getConnectionType().equals("usb"))
                    if (myBT.getConnected()) {
                        myBT.Disconnect();

                        telemetry = false;
                        myBT.setConnected(false);
                        Log.d(TAG, "Stopped telemetry");
                        btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi_error32x32,
                                0, 0, 0);
                        if (soundOn) {
                            mTTS.speak(getString(R.string.disconnected), TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    lastReceivedMessageTime = System.currentTimeMillis();
                    Parser p = new Parser();
                    try {
                        Location l = p.parse((String) msg.obj);
                        if (l != null) {
                            setLatitudeValue(l.getLatitude() + "");
                            setLongitudeValue(l.getLongitude() + "");
                            int nbrOfSat= l.getExtras().getInt("satellites");
                            if(!sateliteAcquisitionCompleteSaid && nbrOfSat > 2 ) {
                                sateliteAcquisitionCompleteSaid = true;
                                if(soundOn)
                                    mTTS.speak(getString(R.string.sat_acq_complete_msg), TextToSpeech.QUEUE_FLUSH, null);
                            }
                            else {
                                if(sateliteAcquisitionCompleteSaid && nbrOfSat < 3) {
                                    sateliteAcquisitionCompleteSaid = false;
                                    /*if(soundOn)
                                        mTTS.speak(getString(R.string.sat_acq_lost_msg), TextToSpeech.QUEUE_FLUSH, null);*/
                                }
                            }

                            if (tabPage2 != null) {
                                tabPage2.setLatitudeValue(l.getLatitude() + "");
                                tabPage2.setLongitudeValue(l.getLongitude() + "");
                                tabPage2.setHdopVal(l.getAccuracy()+"");
                                tabPage2.setGPSAltitudeVal(l.getAltitude()+"");
                                tabPage2.setGPSSpeedVal(l.getSpeed()+"");
                                tabPage2.setSatellitesVal(l.getExtras().getInt("satellites") +"");
                                //tabPage2.setTimeSatValue(l.getElapsedRealtimeAgeMillis()+"");
                                receiving = true;
                            }
                            else {
                                receiving = false;
                                sateliteAcquisitionCompleteSaid = false;
                            }
                        }
                        else {
                            Log.d(TAG, "Connection lost");
                            sateliteAcquisitionCompleteSaid = false;
                            notConnectedTime = System.currentTimeMillis();
                            if ((notConnectedTime - lastSpeakNotConnectedTime) > 60000) {
                                if (soundOn)
                                    mTTS.speak(getString(R.string.sat_acq_lost_msg), TextToSpeech.QUEUE_FLUSH, null);
                                lastSpeakNotConnectedTime = notConnectedTime;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (tabPage3 != null)
                        tabPage3.setTrame((String) msg.obj);
                    break;
            }
        }
    };

    private void setGPSTram(String value) {
        Log.d(TAG, value);
        Parser p = new Parser();
        Location l = p.parse(value);
        if (l != null) {
            setLatitudeValue(l.getLatitude() + "");
            setLongitudeValue(l.getLongitude() + "");

            if (tabPage2 != null) {
                tabPage2.setLatitudeValue(l.getLatitude() + "");
                tabPage2.setLongitudeValue(l.getLongitude() + "");
                tabPage2.setHdopVal("");
                tabPage2.setGPSAltitudeVal("");
                tabPage2.setGPSSpeedVal("");
            }
        }

    }

    private void setLatitudeValue(String value) {
        if (value.matches("\\d+(?:\\.\\d+)?")) {
            float val = Float.parseFloat(value);
            Log.d("track", "latitude:" + value);
            if (val != 0.0f) {
                rocketLatitude = Float.parseFloat(value);
                myBT.getAppConf().setRocketLatitude(rocketLatitude);
            }
        }
    }

    private void setLongitudeValue(String value) {
        if (value.matches("\\d+(?:\\.\\d+)?")) {
            float val = Float.parseFloat(value);
            Log.d("track", "longitude:" + value);
            if (val != 0.0f) {
                rocketLongitude = Float.parseFloat(value);
                myBT.getAppConf().setRocketLongitude(rocketLongitude);
                myBT.getAppConf().SaveConfig();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState()");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState()");
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        myBT = (ConsoleApplication) getApplication();
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        receiver = new LocationBroadCastReceiver();

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);

        LocationManager lm = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.Cancel, null)
                    .show();
        }

        setContentView(R.layout.activity_mainscreen_tab);

        myBT.setHandler(handler);
        myBT.setConnectionType("usb");

        rocketLatitude = myBT.getAppConf().getRocketLatitude();

        rocketLongitude = myBT.getAppConf().getRocketLongitude();

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        //textViewdistance = (TextView) findViewById(R.id.textViewdistance);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                startService();
            }
        } else {
            startService();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
            }
            if (checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            }
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }


        //init text to speech
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = 0;

                    if (Locale.getDefault().getLanguage().equals("en"))
                        result = mTTS.setLanguage(Locale.ENGLISH);
                    else if (Locale.getDefault().getLanguage().equals("fr"))
                        result = mTTS.setLanguage(Locale.FRENCH);
                    else if (Locale.getDefault().getLanguage().equals("tr"))
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage().equals("nl"))
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage().equals("es"))
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage().equals("it"))
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage().equals("hu"))
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage().equals("ru"))
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else
                        result = mTTS.setLanguage(Locale.ENGLISH);

                    Log.d("Voice", myBT.getAppConf().getTelemetryVoice() + "");

                    int i = 0;
                    try {
                        for (Voice tmpVoice : mTTS.getVoices()) {

                            if (tmpVoice.getName().startsWith(Locale.getDefault().getLanguage())) {
                                Log.d("Voice", tmpVoice.getName());
                                if (myBT.getAppConf().getTelemetryVoice() == i) {
                                    mTTS.setVoice(tmpVoice);
                                    Log.d("Voice", "Found voice");
                                    break;
                                }
                                i++;
                            }
                        }
                    } catch (Exception e) {

                    }


                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {

                    }
                } else {
                    Log.e("TTS", "Init failed");
                }
            }
        });
        mTTS.setPitch(1.0f);
        mTTS.setSpeechRate(1.0f);


        btnDismiss = (Button) findViewById(R.id.butDismiss);
        btnConnect = (Button) findViewById(R.id.butConnect);
        btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi_error32x32,
                0, 0, 0);
        //butShareMap = (Button) findViewById(R.id.butShareMap);
        butAudio = (Button) findViewById(R.id.butAudio);
        butAudio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.audio_on32x32,
                0, 0, 0);

        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
        butAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundOn) {
                    soundOn = false;
                    butAudio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.audio_off_32x32,
                            0, 0, 0);
                } else {
                    soundOn = true;
                    butAudio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.audio_on32x32,
                            0, 0, 0);
                }
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "connected clicked");
                myBT.getAppConf().ReadConfig();
                if (myBT.getAppConf().getConnectionType() == 0)
                    myBT.setConnectionType("bluetooth");
                else
                    myBT.setConnectionType("usb");

                if (myBT.getConnected()) {
                    //connected = false;
                    btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi_error32x32,
                            0, 0, 0);
                    //if (myBT.getConnectionType().equals("usb"))
                    //if (myBT.getConnected()) {
                    myBT.Disconnect();
                    telemetry = false;
                    myBT.setConnected(false);
                    Log.d(TAG, "Stopped telemetry");
                    if (soundOn) {
                        mTTS.speak(getString(R.string.disconnected), TextToSpeech.QUEUE_FLUSH, null);
                    }
                    //}

                } else {
                    if (myBT.getConnectionType().equals("bluetooth")) {
                        address = myBT.getAddress();
                        Log.d(TAG,"Connecting using bluetooth");
                        if (address != null) {
                            Log.d(TAG,"Connecting using bluetooth2");
                            new ConnectBT().execute(); //Call the class to connect
                            Log.d(TAG,"Connecting using bluetooth3");
                            while(!myBT.getConnected())
                            {

                            }
                            if (myBT.getConnected()) {
                                Log.d(TAG,"Connecting using bluetooth4");
                                btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi32x32,
                                        0, 0, 0);
                                telemetry = true;
                                if (soundOn) {
                                    mTTS.speak(getString(R.string.connected), TextToSpeech.QUEUE_FLUSH, null);
                                }
                                startTelemetry();
                            }
                        } else {
                            // choose the bluetooth device
                            Intent i = new Intent(MainScreenActivity.this, SearchBluetooth.class);
                            startActivity(i);
                        }
                    } else {
                        myBT.setModuleName("USB");
                        //this is a USB connection
                        telemetry = true;
                        btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi32x32,
                                0, 0, 0);
                        connect();
                    }

                }
            }
        });

       /* butShareMap.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                takeMapScreenshot();
            }
        });*/

        //tell the distance every 15 seconds
        timerDistance = new Timer();
        timerDistance.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (soundOn) {
                    if(myBT.getConnected()) {
                        mTTS.speak("Distance" + " " + String.valueOf((int) distance) + " "
                                + myBT.getAppConf().getUnitsValue(), TextToSpeech.QUEUE_FLUSH, null);
                        Log.d(TAG, "unit value:" + myBT.getAppConf().getUnitsValue());
                    } else
                    {
                        mTTS.speak(getString(R.string.notconnected_msg), TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }

        }, 0, 15000);


        timerNotReceiving = new Timer();
        timerNotReceiving.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if(!receiving) {
                    if (soundOn)
                        mTTS.speak(getString(R.string.sat_acq_lost_msg), TextToSpeech.QUEUE_FLUSH, null);
                }
            }

        }, 0, 30000);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new SectionsStatusPageAdapter(getSupportFragmentManager());

        tabPage1 = new TrackMapFragment(myBT);
        tabPage2 = new TrackInfoFragment(myBT);
        tabPage3 = new TrackGPSTrameFragment();

        adapter.addFragment(tabPage1, "TAB1");
        adapter.addFragment(tabPage2, "TAB2");
        adapter.addFragment(tabPage3, "TAB2");


        linearDots = findViewById(R.id.idAltiStatusLinearDots);
        agregaIndicateDots(0, adapter.getCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(viewListener);

    }

    public void agregaIndicateDots(int pos, int nbr) {
        dotsSlide = new TextView[nbr];
        linearDots.removeAllViews();

        for (int i = 0; i < dotsSlide.length; i++) {
            dotsSlide[i] = new TextView(this);
            dotsSlide[i].setText(Html.fromHtml("&#8226;"));
            dotsSlide[i].setTextSize(35);
            dotsSlide[i].setTextColor(getResources().getColor(R.color.colorWhiteTransparent));
            linearDots.addView(dotsSlide[i]);
        }

        if (dotsSlide.length > 0) {
            dotsSlide[pos].setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {
        }

        @Override
        public void onPageSelected(int i) {
            agregaIndicateDots(i, adapter.getCount());
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    };

    public class SectionsStatusPageAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList();
        private final List<String> mFragmentTitleList = new ArrayList();

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public SectionsStatusPageAdapter(FragmentManager fm) {
            super(fm);
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }

    public void connect() {
        myBT.setModuleName("USB");
        //this is a USB connection
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();

                PendingIntent pi;
                if (android.os.Build.VERSION.SDK_INT >= 31) {
                    pi = PendingIntent.getBroadcast(MainScreenActivity.this, 0,
                            new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                } else {
                    pi = PendingIntent.getBroadcast(MainScreenActivity.this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                }

                usbManager.requestPermission(device, pi);
                keep = false;

                if (!keep)
                    break;
            }
        }
    }

    public void startTelemetry() {
        telemetry = true;
        Log.d(TAG, "Started telemetry");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!telemetry) break;
                    if (myBT.getConnected())
                        myBT.ReadResult(1000);
                }
            }
        };
        rocketTelemetry = new Thread(r);
        rocketTelemetry.start();
    }

    private void startService() {
        IntentFilter filter = new IntentFilter("ACT_LOC");
        registerReceiver(receiver, filter);
        locIntent = new Intent(MainScreenActivity.this, LocationService.class);
        startService(locIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locIntent != null)
            stopService(locIntent);
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        if(timerDistance !=null)
            timerDistance.cancel();
        if(timerNotReceiving!=null)
            timerNotReceiving.cancel();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService();
                } else {
                    Toast.makeText(this, "permission need to be granted", Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Configuration.getInstance().load(getApplicationContext(),
        //        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        /*if(myBT.getConnected() && !status) {
            myBT.flush();
            myBT.clearInput();
            status = true;
        }*/
        if (receiver == null)
            startService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Configuration.getInstance().save(getApplicationContext(),
        //        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        /*if (receiver !=null) {
            try {
                unregisterReceiver(receiver);
                receiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            receiver = null;
        }*/
    }

    public class LocationBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d ("coordinate",intent.getAction());
            //distanceTime = System.currentTimeMillis();
            if (intent.getAction().equals("ACT_LOC")) {
                double latitude = intent.getDoubleExtra("latitude", 0f);
                double longitude = intent.getDoubleExtra("longitude", 0f);
                if (tabPage2 != null) {
                    tabPage2.setTelLatitudeValue(latitude + "");
                    tabPage2.setTelLongitudeValue(longitude + "");
                }
                distance = LocationUtils.distanceBetweenCoordinate(latitude, rocketLatitude, longitude, rocketLongitude);
                //textViewdistance.setText(String.format("%.2f",distance )+ " " + myBT.getAppConf().getUnitsValue());
                // Tell distance every 15 secondes
                /*if ((distanceTime - lastSpeakTime) > 15000) {
                    if (soundOn) {
                        if(myBT.getConnected()) {
                            mTTS.speak("Distance" + " " + String.valueOf((int) distance) + " "
                                    + myBT.getAppConf().getUnitsValue(), TextToSpeech.QUEUE_FLUSH, null);
                        } else
                        {
                            mTTS.speak(getString(R.string.notconnected_msg), TextToSpeech.QUEUE_FLUSH, null);

                        }
                    }
                    lastSpeakTime = distanceTime;
                }*/

                //Warn if we have not received GPS trame
                if((lastReceivedMessageTime -System.currentTimeMillis()) > 60000) {
                    notConnectedTime = System.currentTimeMillis();
                    if ((notConnectedTime - lastSpeakNotConnectedTime) > 60000) {
                        if (soundOn)
                            mTTS.speak(getString(R.string.sat_acq_lost_msg), TextToSpeech.QUEUE_FLUSH, null);
                        lastSpeakNotConnectedTime = notConnectedTime;
                    }
                }

                if (tabPage1 != null) {
                    tabPage1.updateMap(latitude, longitude, rocketLatitude, rocketLongitude);
                    tabPage1.setDistance(String.format("%.2f", distance) + " " + myBT.getAppConf().getUnitsValue());
                }
            }
        }
    }

    private void takeMapScreenshot() {
        Date date = new Date();
        CharSequence format = DateFormat.format("MM-dd-yyyy_hh:mm:ss", date);

        try {
            File mainDir = new File(
                    this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FilShare");
            if (!mainDir.exists()) {
                boolean mkdir = mainDir.mkdir();
            }

            String path = mainDir + "/" + "AltiMultiCurve" + "-" + format + ".jpeg";
            findViewById(android.R.id.content).getRootView().setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(findViewById(android.R.id.content).getRootView().getDrawingCache());
            findViewById(android.R.id.content).getRootView().setDrawingCacheEnabled(false);


            File imageFile = new File(path);
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            ShareHandler.shareScreenShot(imageFile, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        myBT.getAppConf().ReadConfig();
        //only enable bluetooth module search if connection type is bluetooth
        if (myBT.getAppConf().getConnectionType() == 1) {
            menu.findItem(R.id.action_bluetooth).setEnabled(false);
        } else {
            menu.findItem(R.id.action_bluetooth).setEnabled(true);
        }

        //if we are connected then enable some menu options and if not disable them
        if (myBT.getConnected()) {
            // We are connected so no need to choose the bluetooth
            menu.findItem(R.id.action_bluetooth).setEnabled(false);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //share screen
        if (id == R.id.action_share) {
            ShareHandler.takeScreenShot(findViewById(android.R.id.content).getRootView(), this);
            return true;
        }
        //Open application configuration
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainScreenActivity.this, AppTabConfigActivity.class);
            startActivity(i);
            return true;
        }
        //open help screen
        if (id == R.id.action_help) {
            Intent i = new Intent(MainScreenActivity.this, HelpActivity.class);
            i.putExtra("help_file", "help");
            startActivity(i);
            return true;
        }
        if (id == R.id.action_bluetooth) {
            // choose the bluetooth device
            Intent i = new Intent(MainScreenActivity.this, SearchBluetooth.class);
            startActivity(i);
            return true;
        }
        //Open the 3DR module config
        if (id == R.id.action_mod3dr_settings) {
            Intent i = new Intent(MainScreenActivity.this, Config3DR.class);
            startActivity(i);
            return true;
        }
        //Open the bluetooth module config
        if (id == R.id.action_modbt_settings) {
            Intent i = new Intent(MainScreenActivity.this, ConfigBT.class);
            startActivity(i);
            return true;
        }
        //Open the lora module config
        if (id == R.id.action_modlora_settings) {
            Intent i = new Intent(MainScreenActivity.this, ConfigLoraE220.class);
            startActivity(i);
            return true;
        }
        //Open the lora module config
        if (id == R.id.action_modlorae32_settings) {
            Intent i = new Intent(MainScreenActivity.this, ConfigLoraE32.class);
            startActivity(i);
            return true;
        }
        //Open the about screen
        if (id == R.id.action_about) {
            Intent i = new Intent(MainScreenActivity.this, AboutActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* This is the Bluetooth connection sub class */
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private AlertDialog.Builder builder = null;
        private AlertDialog alert;
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            //"Connecting...", "Please wait!!!"
            builder = new AlertDialog.Builder(MainScreenActivity.this);
            //Connecting...
            builder.setMessage(getResources().getString(R.string.MS_msg1) + "\n" + myBT.getModuleName())
                    .setTitle(getResources().getString(R.string.MS_msg2))
                    .setCancelable(false)
                    .setNegativeButton(getResources().getString(R.string.main_screen_actity), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                            myBT.setExit(true);
                            myBT.Disconnect();
                        }
                    });
            alert = builder.create();
            alert.show();
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {

            if (!myBT.getConnected()) {
                if (myBT.connect())
                    ConnectSuccess = true;
                else
                    ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Log.d(TAG,"connection unsuccessfull");
            } else {
                //Connected.
                myBT.setConnected(true);
                //EnableUI();
                Log.d(TAG,"connection success");
            }
            alert.dismiss();
        }
    }
}
