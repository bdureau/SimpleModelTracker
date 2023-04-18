package com.bdureau.simplemodeltracker.config;
/**
 * @description: In this activity you should be able to choose the application languages and looks and feel.
 * Still a lot to do but it is a good start
 * @author: boris.dureau@neuf.fr
 **/

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bdureau.simplemodeltracker.ConsoleApplication;
import com.bdureau.simplemodeltracker.Help.AboutActivity;
import com.bdureau.simplemodeltracker.Help.HelpActivity;
import com.bdureau.simplemodeltracker.R;
import com.bdureau.simplemodeltracker.ShareHandler;
import com.bdureau.simplemodeltracker.config.AppConfig.AppConfig1Fragment;
import com.bdureau.simplemodeltracker.config.AppConfig.AppConfig2Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AppTabConfigActivity extends AppCompatActivity {
    Button btnDismiss, btnSave, bdtDefault;
    private ViewPager mViewPager;
    private SectionsPageAdapter adapter;
    private TextToSpeech mTTS;

    private AppConfig1Fragment appConfigPage1 = null;
    private AppConfig2Fragment appConfigPage2 = null;

    private AppConfigData appConfigData = null;

    private ConsoleApplication myBT;

    private TextView[] dotsSlide;
    private LinearLayout linearDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //get the Connection Application pointer
        myBT = (ConsoleApplication) getApplication();

        myBT.getAppConf().ReadConfig();
        //Check the local and force it if needed
        //getApplicationContext().getResources().updateConfiguration(myBT.getAppLocal(), null);
        // get the data for all the drop down
        appConfigData = new AppConfigData(this);
        setContentView(R.layout.activity_app_tab_config);

        mViewPager = (ViewPager) findViewById(R.id.container_config);
        setupViewPager(mViewPager);

        btnDismiss = (Button) findViewById(R.id.butDismiss);
        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();      //exit the application configuration activity
            }
        });

        btnSave = (Button) findViewById(R.id.butSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save the application configuration
                SaveConfig();
            }
        });

        bdtDefault = (Button) findViewById(R.id.butDefault);
        bdtDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //restore the application default configuration
                RestoreToDefault();
            }
        });

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = 0;

                    if (Locale.getDefault().getLanguage() == "en")
                        result = mTTS.setLanguage(Locale.ENGLISH);
                    else if (Locale.getDefault().getLanguage() == "fr")
                        result = mTTS.setLanguage(Locale.FRENCH);
                    else if (Locale.getDefault().getLanguage() == "tr")
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage() == "es")
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage() == "nl")
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage() == "it")
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage() == "hu")
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else if (Locale.getDefault().getLanguage() == "ru")
                        result = mTTS.setLanguage(getResources().getConfiguration().locale);
                    else
                        result = mTTS.setLanguage(Locale.ENGLISH);
                    try {
                        String[] itemsVoices;
                        String items = "";
                        for (Voice tmpVoice : mTTS.getVoices()) {
                            if (tmpVoice.getName().startsWith(Locale.getDefault().getLanguage())) {
                                if (items.equals(""))
                                    items = tmpVoice.getName();
                                else
                                    items = items + "," + tmpVoice.getName();
                                Log.d("Voice", tmpVoice.getName());
                            }
                        }

                        itemsVoices = items.split(",");

                        appConfigPage2.setVoices(itemsVoices);
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "Language not supported");
                        } else {

                        }
                    } catch (Exception e) {

                    }
                } else {
                    Log.e("TTS", "Init failed");
                }
            }
        }, "com.google.android.tts");
    }

    private void SaveConfig() {
        if(appConfigPage1.isViewCreated()) {
            myBT.getAppConf().setApplicationLanguage(appConfigPage1.getAppLanguage());
            myBT.getAppConf().setUnits(appConfigPage1.getAppUnit());
            myBT.getAppConf().setBaudRate(appConfigPage1.getBaudRate());
            myBT.getAppConf().setConnectionType(appConfigPage1.getConnectionType());

            myBT.getAppConf().setMapColor(appConfigPage1.getMapColor());
        }
        if(appConfigPage2.isViewCreated()) {
            myBT.getAppConf().setAcquisition_satellite_event(appConfigPage2.getAcquisitionSatelliteEvent());
            myBT.getAppConf().setConnectedDisconnected_event(appConfigPage2.getConnectedDisconnectedEvent());
            myBT.getAppConf().setDistance_event(appConfigPage2.getDistanceEvent());
            myBT.getAppConf().setNotConnected_event(appConfigPage2.getNotConnectedEvent());
            myBT.getAppConf().setTelemetryVoice(appConfigPage2.getTelemetryVoice());
        }
        myBT.getAppConf().SaveConfig();
        invalidateOptionsMenu();
        finish();
    }

    private void RestoreToDefault() {
        myBT.getAppConf().ResetDefaultConfig();
        if(appConfigPage1.isViewCreated()) {
            appConfigPage1.setAppLanguage(myBT.getAppConf().getApplicationLanguage());
            appConfigPage1.setAppUnit(myBT.getAppConf().getUnits());
            appConfigPage1.setBaudRate(myBT.getAppConf().getBaudRate());
            appConfigPage1.setConnectionType(myBT.getAppConf().getConnectionType());

            appConfigPage1.setMapColor(myBT.getAppConf().getMapColor());
        }
        if(appConfigPage2.isViewCreated()) {
            appConfigPage2.setNotConnectedEvent(myBT.getAppConf().getNotConnected_event());
            appConfigPage2.setAcquisitionSatelliteEvent(myBT.getAppConf().getAcquisition_satellite_event());
            appConfigPage2.setConnectedDisconnectedEvent(myBT.getAppConf().getConnectedDisconnected_event());
            appConfigPage2.setDistanceEvent(myBT.getAppConf().getDistance_event());
            appConfigPage2.setTelemetryVoice(myBT.getAppConf().getTelemetryVoice());
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new AppTabConfigActivity.SectionsPageAdapter(getSupportFragmentManager());

        appConfigPage1 = new AppConfig1Fragment(myBT, appConfigData);
        appConfigPage2 = new AppConfig2Fragment(myBT, appConfigData);

        adapter.addFragment(appConfigPage1, "TAB1");
        adapter.addFragment(appConfigPage2, "TAB2");


        linearDots = findViewById(R.id.idAppConfigLinearDots);
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

    public class SectionsPageAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList();
        private final List<String> mFragmentTitleList = new ArrayList();

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public SectionsPageAdapter(FragmentManager fm) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mTTS.shutdown();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_application_config, menu);
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
        //open help screen
        if (id == R.id.action_help) {
            Intent i = new Intent(AppTabConfigActivity.this, HelpActivity.class);
            i.putExtra("help_file", "help_config_application");
            startActivity(i);
            return true;
        }

        if (id == R.id.action_about) {
            Intent i = new Intent(AppTabConfigActivity.this, AboutActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
