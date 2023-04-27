package com.bdureau.simplemodeltracker.config.AppConfig;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


import com.bdureau.simplemodeltracker.ConsoleApplication;
import com.bdureau.simplemodeltracker.R;
import com.bdureau.simplemodeltracker.config.AppConfigData;


public class AppConfig1Fragment extends Fragment {
    private Spinner spMapColor, spAppLanguage, spAppUnit, spBaudRate, spConnectionType;
    private Spinner spModelType;
    public ImageView imageAlti;

    private boolean ViewCreated = false;
    private ConsoleApplication BT;

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

    public void setModelType(int value) {
        this.spModelType.setSelection(value);
    }

    public int getModelType() {
        return (int) this.spModelType.getSelectedItemId();
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

        imageAlti = (ImageView) view.findViewById(R.id.imageAlti);
        //ModelType
        spModelType = (Spinner) view.findViewById(R.id.spinnerModelType);

        ArrayAdapter<String> adapterModelType = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, appConfigData.getItemsModelType());
        spModelType.setAdapter(adapterModelType);

        spModelType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (spModelType.getSelectedItemId()== 0) {
                    imageAlti.setImageDrawable(getResources().getDrawable(R.drawable.ic_rocket_map, view.getContext().getTheme()));
                } else if (spModelType.getSelectedItemId()== 1) {
                    imageAlti.setImageDrawable(getResources().getDrawable(R.drawable.ic_boat_map, view.getContext().getTheme()));
                } else if (spModelType.getSelectedItemId()== 2) {
                    imageAlti.setImageDrawable(getResources().getDrawable(R.drawable.ic_car_map, view.getContext().getTheme()));
                } else if (spModelType.getSelectedItemId()== 3) {
                    imageAlti.setImageDrawable(getResources().getDrawable(R.drawable.ic_hot_air_balloon_map, view.getContext().getTheme()));
                }else if (spModelType.getSelectedItemId()== 4) {
                    imageAlti.setImageDrawable(getResources().getDrawable(R.drawable.ic_plane_map, view.getContext().getTheme()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        spAppLanguage.setSelection(BT.getAppConf().getApplicationLanguage());
        spAppUnit.setSelection(BT.getAppConf().getUnits());
        spBaudRate.setSelection(BT.getAppConf().getBaudRate());
        spConnectionType.setSelection(BT.getAppConf().getConnectionType());
        spModelType.setSelection(BT.getAppConf().getModelType());
        ViewCreated = true;
        return view;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
       /* try {
            mTTS.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
    private void msg(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }

}