package com.bdureau.simplemodeltracker;
/*
 * =================================================================================================
 * Project:        Simple Model Tracker (Rocket GPS & Telemetry Tracker)
 * Package:        com.bdureau.simplemodeltracker
 * Class:          MainScreenActivity.java
 *
 * Description:    Main application activity providing real-time rocket telemetry visualization,
 *                 GPS coordinates tracking, live OpenStreetMap rendering, and text-to-speech (TTS)
 *                 audio announcements. Supports USB OTG and Bluetooth serial links.
 *
 * Author:         Bear Instruments / Rocket Telemetry Team
 * Target Platform: Android API 21+ (Compatible with Android 13/14 modern broadcast restrictions)
 * =================================================================================================
 */
import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
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
import androidx.core.content.ContextCompat;
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
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private TextView[] dotsSlide;
    private LinearLayout linearDots;
    public LocationBroadCastReceiver receiver = null;
    private boolean locationReceiverRegistered = false;
    private boolean usbReceiverRegistered = false;
    SectionsStatusPageAdapter adapter;
    private TrackMapFragment tabPage1 = null;
    private TrackInfoFragment tabPage2 = null;
    private TrackGPSTrameFragment tabPage3 = null;

    private float rocketLatitude = 48.8698f, rocketLongitude = 2.2190f;

    private Button btnDismiss, butAudio, btnConnect;
    private ConsoleApplication myBT;
    Thread rocketTelemetry;
    // read by the telemetry thread and the timers: volatile so that every thread sees the change
    private volatile boolean telemetry = false;
    private TextToSpeech mTTS;

    private long notConnectedTime = 0;
    private long lastSpeakNotConnectedTime = 1000;

    private volatile long lastReceivedMessageTime = 0;

    private volatile boolean soundOn = true;
    private boolean sateliteAcquisitionCompleteSaid = false;
    Intent locIntent = null;

    UsbManager usbManager;
    UsbDevice device;
    volatile double distance;
    volatile boolean receiving = false;
    Timer timerDistance;
    Timer timerNotReceiving;

    public final String ACTION_USB_PERMISSION = "com.bdureau.simplemodeltracker.USB_PERMISSION";

    // Languages for which the text to speech uses the language of the phone
    private static final List<String> TTS_LANGUAGES = Arrays.asList("en", "fr", "tr", "nl", "es", "it", "hu", "ru");

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(ACTION_USB_PERMISSION)) {
                // The extra is only filled when the PendingIntent is mutable; the USB manager
                // is the reliable source on every Android version
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        || (device != null && usbManager.hasPermission(device));

                if (granted && device != null) {
                    Log.d(TAG, "Permission granted");
                    if (myBT.connect(usbManager, device, Integer.parseInt(myBT.getAppConf().getBaudRateValue()))) {
                        myBT.setConnected(true);
                        myBT.setConnectionType("usb");
                        btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi32x32,
                                0, 0, 0);
                        if (myBT.getAppConf().getConnectedDisconnected_event()) {
                            speak(getString(R.string.connected));
                        }
                        startTelemetry();
                    } else {
                        setDisconnectedUI();
                    }
                } else {
                    msg("PERM NOT GRANTED");
                    setDisconnectedUI();
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                msg("I can connect via usb");
                myBT.setConnectionType("usb");
                btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi32x32,
                        0, 0, 0);
                connect();
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                if ("usb".equals(myBT.getConnectionType()) && myBT.getConnected()) {
                    myBT.Disconnect();
                    myBT.setConnected(false);
                    setDisconnectedUI();
                    Log.d(TAG, "Stopped telemetry");
                    if (myBT.getAppConf().getConnectedDisconnected_event()) {
                        speak(getString(R.string.disconnected));
                    }
                }
            }
        }
    };

    // Messages from the telemetry thread are handled on the main (UI) thread
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    lastReceivedMessageTime = System.currentTimeMillis();
                    Parser p = new Parser();
                    try {
                        Location l = p.parse((String) msg.obj);
                        if (l != null) {
                            setLatitudeValue(l.getLatitude());
                            setLongitudeValue(l.getLongitude());
                            int nbrOfSat = (l.getExtras() != null) ? l.getExtras().getInt("satellites") : 0;
                            if (!sateliteAcquisitionCompleteSaid && nbrOfSat > 2) {
                                sateliteAcquisitionCompleteSaid = true;
                                if (myBT.getAppConf().getAcquisition_satellite_event())
                                    speak(getString(R.string.sat_acq_complete_msg));
                            } else if (sateliteAcquisitionCompleteSaid && nbrOfSat < 3) {
                                sateliteAcquisitionCompleteSaid = false;
                            }

                            if (tabPage2 != null) {
                                tabPage2.setLatitudeValue(l.getLatitude() + "");
                                tabPage2.setLongitudeValue(l.getLongitude() + "");
                                tabPage2.setHdopVal(l.getAccuracy() + "");
                                tabPage2.setGPSAltitudeVal(l.getAltitude() + "");
                                tabPage2.setGPSSpeedVal(l.getSpeed() + "");
                                tabPage2.setSatellitesVal(nbrOfSat + "");
                                receiving = true;
                            } else {
                                receiving = false;
                                sateliteAcquisitionCompleteSaid = false;
                            }
                        } else {
                            Log.d(TAG, "Connection lost");
                            sateliteAcquisitionCompleteSaid = false;
                            notConnectedTime = System.currentTimeMillis();
                            if ((notConnectedTime - lastSpeakNotConnectedTime) > 60000) {
                                if (myBT.getAppConf().getAcquisition_satellite_event())
                                    speak(getString(R.string.sat_acq_lost_msg));
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

    // Coordinates can be negative (west of Greenwich, southern hemisphere): 0 means "no fix"
    private void setLatitudeValue(double value) {
        Log.d("track", "latitude:" + value);
        if (value != 0.0 && !Double.isNaN(value)) {
            rocketLatitude = (float) value;
            myBT.getAppConf().setRocketLatitude(rocketLatitude);
        }
    }

    private void setLongitudeValue(double value) {
        Log.d("track", "longitude:" + value);
        if (value != 0.0 && !Double.isNaN(value)) {
            rocketLongitude = (float) value;
            myBT.getAppConf().setRocketLongitude(rocketLongitude);
            myBT.getAppConf().SaveConfig();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState()");
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState()");
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    // Speaks a message if the sound is on (callable from any thread)
    private void speak(String text) {
        if (soundOn && mTTS != null)
            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SimpleModelTracker");
    }

    // Shows the "not connected" icon and stops the telemetry thread
    private void setDisconnectedUI() {
        telemetry = false;
        btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi_error32x32,
                0, 0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        myBT = (ConsoleApplication) getApplication();
        Configuration.getInstance().setUserAgentValue(getPackageName());

        org.osmdroid.config.IConfigurationProvider osmConf = org.osmdroid.config.Configuration.getInstance();
        File basePath = new File(getCacheDir().getAbsolutePath(), "osmdroid");
        osmConf.setOsmdroidBasePath(basePath);
        File tileCache = new File(osmConf.getOsmdroidBasePath().getAbsolutePath(), "tile");
        osmConf.setOsmdroidTileCache(tileCache);

        receiver = new LocationBroadCastReceiver();
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            startService();
        }

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

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

        // USB events go to the USB receiver (they were registered on the location receiver).
        // System broadcasts and our own PendingIntent are delivered to a not exported receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        ContextCompat.registerReceiver(this, broadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        usbReceiverRegistered = true;

        // INTERNET and ACCESS_NETWORK_STATE are normal permissions granted at install time:
        // they must only be declared in the manifest, not requested at run time.

        //init text to speech
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    String language = Locale.getDefault().getLanguage();
                    int result;
                    if (language.equals("en"))
                        result = mTTS.setLanguage(Locale.ENGLISH);
                    else if (language.equals("fr"))
                        result = mTTS.setLanguage(Locale.FRENCH);
                    else if (TTS_LANGUAGES.contains(language))
                        result = mTTS.setLanguage(Locale.getDefault());
                    else
                        result = mTTS.setLanguage(Locale.ENGLISH);

                    Log.d("Voice", myBT.getAppConf().getTelemetryVoice() + "");

                    int i = 0;
                    try {
                        if (mTTS.getVoices() != null) {
                            for (Voice tmpVoice : mTTS.getVoices()) {
                                if (tmpVoice.getName().startsWith(language)) {
                                    Log.d("Voice", tmpVoice.getName());
                                    if (myBT.getAppConf().getTelemetryVoice() == i) {
                                        mTTS.setVoice(tmpVoice);
                                        Log.d("Voice", "Found voice");
                                        break;
                                    }
                                    i++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("TTS", "Cannot set the voice", e);
                    }

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
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
                    myBT.Disconnect();
                    myBT.setConnected(false);
                    setDisconnectedUI();
                    Log.d(TAG, "Stopped telemetry");
                    if (myBT.getAppConf().getConnectedDisconnected_event()) {
                        speak(getString(R.string.disconnected));
                    }

                } else {
                    if (myBT.getConnectionType().equals("bluetooth")) {
                        address = myBT.getAddress();
                        Log.d(TAG, "Connecting using bluetooth");
                        if (address != null) {
                            new ConnectBT().execute(); //Call the class to connect
                        } else {
                            // choose the bluetooth device
                            Intent i = new Intent(MainScreenActivity.this, SearchBluetooth.class);
                            startActivity(i);
                        }
                    } else {
                        //this is a USB connection
                        btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi32x32,
                                0, 0, 0);
                        connect();
                    }
                }
            }
        });

        //tell the distance every 15 seconds
        timerDistance = new Timer();
        timerDistance.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (soundOn) {
                    if (myBT.getConnected()) {
                        if (myBT.getAppConf().getDistance_event()) {
                            speak(getString(R.string.distance) + " " + (int) distance + " "
                                    + myBT.getAppConf().getUnitsValue());
                        }
                        Log.d(TAG, "unit value:" + myBT.getAppConf().getUnitsValue());
                    } else {
                        if (myBT.getAppConf().getNotConnected_event())
                            speak(getString(R.string.notconnected_msg));
                    }
                }
            }

        }, 0, 15000);

        timerNotReceiving = new Timer();
        timerNotReceiving.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (!receiving) {
                    if (myBT.getAppConf().getAcquisition_satellite_event())
                        speak(getString(R.string.sat_acq_lost_msg));
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
        adapter.addFragment(tabPage3, "TAB3");

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
            dotsSlide[i].setText("\u2022");   // bullet
            dotsSlide[i].setTextSize(35);
            dotsSlide[i].setTextColor(ContextCompat.getColor(this, R.color.colorWhiteTransparent));
            linearDots.addView(dotsSlide[i]);
        }

        if (dotsSlide.length > 0) {
            dotsSlide[pos].setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
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
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

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
            return mFragmentTitleList.get(position);
        }

        @NonNull
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
        if (usbDevices.isEmpty()) {
            msg(getString(R.string.notconnected_msg));
            setDisconnectedUI();
            return;
        }
        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            device = entry.getValue();
            // Explicit intent (our package): it can be mutable on Android 12+, so that the
            // system can add EXTRA_PERMISSION_GRANTED, and it reaches the not exported receiver
            Intent permissionIntent = new Intent(ACTION_USB_PERMISSION).setPackage(getPackageName());
            int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    ? PendingIntent.FLAG_MUTABLE : 0;
            PendingIntent pi = PendingIntent.getBroadcast(MainScreenActivity.this, 0,
                    permissionIntent, flags);
            usbManager.requestPermission(device, pi);
            break;   // only the first USB device is used
        }
    }

    public void startTelemetry() {
        telemetry = true;
        // only one reading thread at a time
        if (rocketTelemetry != null && rocketTelemetry.isAlive())
            return;
        Log.d(TAG, "Started telemetry");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (telemetry) {
                    if (myBT.getConnected()) {
                        myBT.ReadResult(1000);
                    } else {
                        // wait instead of looping at full speed while not connected
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        };
        rocketTelemetry = new Thread(r);
        rocketTelemetry.start();
    }

    private void startService() {
        if (!locationReceiverRegistered) {
            // ACT_LOC is sent by our own LocationService: the receiver does not need to be exported
            IntentFilter filter = new IntentFilter("ACT_LOC");
            ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            locationReceiverRegistered = true;
        }
        if (locIntent == null) {
            locIntent = new Intent(MainScreenActivity.this, LocationService.class);
            startService(locIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        telemetry = false;
        if (locIntent != null)
            stopService(locIntent);
        if (locationReceiverRegistered) {
            unregisterReceiver(receiver);
            locationReceiverRegistered = false;
        }
        if (usbReceiverRegistered) {
            unregisterReceiver(broadcastReceiver);
            usbReceiverRegistered = false;
        }
        if (timerDistance != null)
            timerDistance.cancel();
        if (timerNotReceiving != null)
            timerNotReceiving.cancel();
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                Toast.makeText(this, "permission need to be granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // restart the location updates if the permission was granted meanwhile (e.g. in the settings)
        if (Build.VERSION.SDK_INT < 23
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            startService();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public class LocationBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACT_LOC".equals(intent.getAction())) {
                double latitude = intent.getDoubleExtra("latitude", 0d);
                double longitude = intent.getDoubleExtra("longitude", 0d);

                if (tabPage2 != null) {
                    tabPage2.setTelLatitudeValue(latitude + "");
                    tabPage2.setTelLongitudeValue(longitude + "");
                }
                distance = LocationUtils.distanceBetweenCoordinate(latitude, rocketLatitude, longitude, rocketLongitude);

                //Warn if we have not received GPS trame for one minute
                if ((System.currentTimeMillis() - lastReceivedMessageTime) > 60000) {
                    notConnectedTime = System.currentTimeMillis();
                    if ((notConnectedTime - lastSpeakNotConnectedTime) > 60000) {
                        speak(getString(R.string.sat_acq_lost_msg));
                        lastSpeakNotConnectedTime = notConnectedTime;
                    }
                }

                if (tabPage1 != null) {
                    tabPage1.updateMap(latitude, longitude, rocketLatitude, rocketLongitude);
                    tabPage1.setDistance(String.format(Locale.getDefault(), "%.2f", distance) + " " + myBT.getAppConf().getUnitsValue());
                }
            }
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
        //and if we are not connected (no need to choose the bluetooth module then)
        boolean bluetooth = myBT.getAppConf().getConnectionType() != 1;
        menu.findItem(R.id.action_bluetooth).setEnabled(bluetooth && !myBT.getConnected());
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
            Log.d(TAG, "Connecting...");
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
                ConnectSuccess = myBT.connect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Log.d(TAG, "connection unsuccessfull");
            } else {
                //Connected.
                myBT.setConnected(true);
                btnConnect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi32x32,
                        0, 0, 0);
                if (myBT.getAppConf().getConnectedDisconnected_event()) {
                    speak(getString(R.string.connected));
                }
                startTelemetry();
            }
            if (alert != null && alert.isShowing())
                alert.dismiss();
        }
    }
}