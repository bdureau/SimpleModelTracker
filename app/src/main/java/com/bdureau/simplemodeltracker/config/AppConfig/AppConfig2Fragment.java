package com.bdureau.simplemodeltracker.config.AppConfig;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bdureau.simplemodeltracker.ConsoleApplication;
import com.bdureau.simplemodeltracker.R;
import com.bdureau.simplemodeltracker.config.AppConfigData;

import java.util.Locale;

public class AppConfig2Fragment extends Fragment {
    private CheckBox cbConnectedDisconnectedEvent, cbDistanceEvent;
    private CheckBox  cbAcquisitionSatelliteEvent, cbNotConnectedEvent;

    private ConsoleApplication BT;
    private Button btnTestVoice;
    private TextToSpeech mTTS;
    private Spinner spTelemetryVoice;
    private int nbrVoices = 0;
    boolean ViewCreated = false;
    private AppConfigData appConfigData;

    public AppConfig2Fragment(ConsoleApplication lBT,
                              AppConfigData cfgData) {
        BT = lBT;
        appConfigData = cfgData;
    }

    public boolean getConnectedDisconnectedEvent() {
        return cbConnectedDisconnectedEvent.isChecked();
    }

    public void setConnectedDisconnectedEvent(boolean value) {
        cbConnectedDisconnectedEvent.setChecked(value);
    }

    public boolean getAcquisitionSatelliteEvent() {
        return cbAcquisitionSatelliteEvent.isChecked();
    }

    public void setAcquisitionSatelliteEvent(boolean value) {
        cbAcquisitionSatelliteEvent.setChecked(value);
    }

    public boolean getDistanceEvent() {
        return cbDistanceEvent.isChecked();
    }

    public void setDistanceEvent(boolean value) {
        cbDistanceEvent.setChecked(value);
    }

    public boolean getNotConnectedEvent() {
        return cbNotConnectedEvent.isChecked();
    }

    public void setNotConnectedEvent(boolean value) {
        cbNotConnectedEvent.setChecked(value);
    }

    public void setVoices(String itemsVoices[]) {
        nbrVoices = itemsVoices.length;
        ArrayAdapter<String> adapterVoice = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, itemsVoices);
        spTelemetryVoice.setAdapter(adapterVoice);
        if (BT.getAppConf().getTelemetryVoice() < nbrVoices)
            spTelemetryVoice.setSelection(BT.getAppConf().getTelemetryVoice());
    }



    public void setTelemetryVoice(int value) {
        if (value < nbrVoices)
            this.spTelemetryVoice.setSelection(value);
    }

    public int getTelemetryVoice() {
        return (int) this.spTelemetryVoice.getSelectedItemId();
    }

    public boolean isViewCreated() {
        return ViewCreated;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_app_config_tab2, container, false);
        cbConnectedDisconnectedEvent = (CheckBox) view.findViewById(R.id.checkBoxAllowTelemetryEvent1);
        cbAcquisitionSatelliteEvent = (CheckBox) view.findViewById(R.id.checkBoxAllowTelemetryEvent7);
        cbDistanceEvent = (CheckBox) view.findViewById(R.id.checkBoxAllowTelemetryEvent8);
        cbNotConnectedEvent = (CheckBox) view.findViewById(R.id.checkBoxAllowTelemetryEvent9);

        spTelemetryVoice = (Spinner) view.findViewById(R.id.spinnerTelemetryVoice);

        cbConnectedDisconnectedEvent.setChecked(BT.getAppConf().getConnectedDisconnected_event());
        cbAcquisitionSatelliteEvent.setChecked(BT.getAppConf().getAcquisition_satellite_event());
        cbDistanceEvent.setChecked(BT.getAppConf().getDistance_event());
        cbNotConnectedEvent.setChecked(BT.getAppConf().getNotConnected_event());



        btnTestVoice = (Button) view.findViewById(R.id.butTestVoice);
        btnTestVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //init text to speech
                mTTS = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            int result = 0;

                            if (Locale.getDefault().getLanguage().equals("en"))
                                //result = mTTS.setLanguage(Locale.ENGLISH);
                                result = mTTS.setLanguage(getResources().getConfiguration().locale);
                            else if (Locale.getDefault().getLanguage().equals("fr"))
                                result = mTTS.setLanguage(getResources().getConfiguration().locale);
                            else if (Locale.getDefault().getLanguage().equals("es"))
                                result = mTTS.setLanguage(getResources().getConfiguration().locale);
                                //result = mTTS.setLanguage(Locale.FRENCH);
                            else if (Locale.getDefault().getLanguage().equals("tr"))
                                result = mTTS.setLanguage(getResources().getConfiguration().locale);
                            else if (Locale.getDefault().getLanguage().equals("nl"))
                                result = mTTS.setLanguage(getResources().getConfiguration().locale);
                            else if (Locale.getDefault().getLanguage().equals("it"))
                                result = mTTS.setLanguage(getResources().getConfiguration().locale);
                            else if (Locale.getDefault().getLanguage().equals("hu"))
                                result = mTTS.setLanguage(getResources().getConfiguration().locale);
                            else if (Locale.getDefault().getLanguage().equals("ru"))
                                result = mTTS.setLanguage(getResources().getConfiguration().locale);
                            else
                                result = mTTS.setLanguage(Locale.ENGLISH);


                            Log.d("Voice", BT.getAppConf().getTelemetryVoice() + "");
                            try {
                                for (Voice tmpVoice : mTTS.getVoices()) {
                                    Log.d("Voice", tmpVoice.getName());
                                    if (tmpVoice.getName().equals(spTelemetryVoice.getSelectedItem().toString())) {
                                        mTTS.setVoice(tmpVoice);
                                        Log.d("Voice", "Found voice");
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                msg(Locale.getDefault().getLanguage());
                            }

                            mTTS.setPitch(1.0f);
                            mTTS.setSpeechRate(1.0f);
                            if (Locale.getDefault().getLanguage().equals("en"))
                                mTTS.speak("Bearaltimeter altimeters are the best", TextToSpeech.QUEUE_FLUSH, null);
                            if (Locale.getDefault().getLanguage().equals("fr"))
                                mTTS.speak("Les altimètres Bearaltimeter sont les meilleurs", TextToSpeech.QUEUE_FLUSH, null);
                            if (Locale.getDefault().getLanguage().equals("es"))
                                mTTS.speak("Los altimietros Bearaltimeter son los mejores", TextToSpeech.QUEUE_FLUSH, null);
                            if (Locale.getDefault().getLanguage().equals("it"))
                                mTTS.speak("Gli altimetri Bearaltimeter sono i migliori", TextToSpeech.QUEUE_FLUSH, null);
                            if (Locale.getDefault().getLanguage().equals("tr"))
                                mTTS.speak("Roket inis yapti", TextToSpeech.QUEUE_FLUSH, null);
                            if (Locale.getDefault().getLanguage().equals("nl"))
                                mTTS.speak("De Bearaltimeter-hoogtemeters zijn de beste", TextToSpeech.QUEUE_FLUSH, null);
                            if (Locale.getDefault().getLanguage().equals("hu"))
                                mTTS.speak("A Bearaltiméter a legjobb", TextToSpeech.QUEUE_FLUSH, null);
                            if (Locale.getDefault().getLanguage().equals("ru"))
                                mTTS.speak("Медвежатник - это лучшее", TextToSpeech.QUEUE_FLUSH, null);
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language not supported");
                            } else {
                                //msg(Locale.getDefault().getLanguage());
                            }
                        } else {
                            Log.e("TTS", "Init failed");
                        }
                    }
                });

            }
        });
        ViewCreated = true;
        return view;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //mTTS.shutdown();
    }
    private void msg(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }
}
