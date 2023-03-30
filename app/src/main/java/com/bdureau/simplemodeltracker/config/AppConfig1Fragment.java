package com.bdureau.simplemodeltracker.config;

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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


import com.bdureau.simplemodeltracker.ConsoleApplication;
import com.bdureau.simplemodeltracker.R;

import java.util.Locale;

public class AppConfig1Fragment extends Fragment {
    private Spinner spMapColor, spAppLanguage, spAppUnit, spBaudRate, spConnectionType;
    //private CheckBox cbAllowManualRecording, cbUseOpenMap;
    private boolean ViewCreated = false;
    private ConsoleApplication BT;
    private Button btnTestVoice;
    private TextToSpeech mTTS;
    private Spinner spTelemetryVoice;
    private int nbrVoices = 0;
    private AppConfigData appConfigData;

    public AppConfig1Fragment(ConsoleApplication lBT,
                              AppConfigData cfgData) {
        BT = lBT;
        appConfigData = cfgData;
    }

    public int getMapColor() {
        return (int) this.spMapColor.getSelectedItemId();
    }

    public void setMapColor(int value) {
        this.spMapColor.setSelection(value);
    }

    public void setTelemetryVoice(int value) {
        if (value < nbrVoices)
            this.spTelemetryVoice.setSelection(value);
    }

    public int getTelemetryVoice() {
        return (int) this.spTelemetryVoice.getSelectedItemId();
    }

    public int getAppLanguage() {
        return (int) this.spAppLanguage.getSelectedItemId();
    }

    public void setAppLanguage(int value) {
        this.spAppLanguage.setSelection(value);
    }

    public int getAppUnit() {
        return (int) this.spAppUnit.getSelectedItemId();
    }

    public void setAppUnit(int value) {
        this.spAppUnit.setSelection(value);
    }

    public int getBaudRate() {
        return (int) this.spBaudRate.getSelectedItemId();
    }

    public void setBaudRate(int value) {
        this.spBaudRate.setSelection(value);
    }

    public int getConnectionType() {
        return (int) this.spConnectionType.getSelectedItemId();
    }

    public void setConnectionType(int value) {
        this.spConnectionType.setSelection(value);
    }

    public void setVoices(String itemsVoices[]) {
        nbrVoices = itemsVoices.length;
        ArrayAdapter<String> adapterVoice = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, itemsVoices);
        spTelemetryVoice.setAdapter(adapterVoice);
        if (BT.getAppConf().getTelemetryVoice() < nbrVoices)
            spTelemetryVoice.setSelection(BT.getAppConf().getTelemetryVoice());
    }

    public boolean isViewCreated() {
        return ViewCreated;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_app_config_tab, container, false);
        // map color
        spMapColor = (Spinner) view.findViewById(R.id.spinnerMapColor);
        ArrayAdapter<String> adapterMapColor = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, appConfigData.getItemsColor());
        spMapColor.setAdapter(adapterMapColor);
        spMapColor.setSelection(BT.getAppConf().getMapColor());

        //Language
        spAppLanguage = (Spinner) view.findViewById(R.id.spinnerLanguage);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, appConfigData.getItemsLanguages());
        spAppLanguage.setAdapter(adapter);

        //units
        spAppUnit = (Spinner) view.findViewById(R.id.spinnerUnits);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, appConfigData.getItemsUnits());
        spAppUnit.setAdapter(adapter2);

        //Baud Rate
        spBaudRate = (Spinner) view.findViewById(R.id.spinnerBaudRate);

        ArrayAdapter<String> adapterBaudRate = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, appConfigData.getItemsBaudRate());
        spBaudRate.setAdapter(adapterBaudRate);

        //connection type
        spConnectionType = (Spinner) view.findViewById(R.id.spinnerConnectionType);

        ArrayAdapter<String> adapterConnectionType = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, appConfigData.getItemsConnectionType());
        spConnectionType.setAdapter(adapterConnectionType);

        spTelemetryVoice = (Spinner) view.findViewById(R.id.spinnerTelemetryVoice);

        //init text to speech
        /*mTTS = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
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
        });*/

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

        spAppLanguage.setSelection(BT.getAppConf().getApplicationLanguage());
        spAppUnit.setSelection(BT.getAppConf().getUnits());
        spBaudRate.setSelection(BT.getAppConf().getBaudRate());
        spConnectionType.setSelection(BT.getAppConf().getConnectionType());

        ViewCreated = true;
        return view;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mTTS.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void msg(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }

}