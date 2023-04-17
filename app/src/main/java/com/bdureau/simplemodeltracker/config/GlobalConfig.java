package com.bdureau.simplemodeltracker.config;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class GlobalConfig {
    Context context;

    SharedPreferences appConfig = null;
    SharedPreferences.Editor edit = null;
    AppConfigData appCfgData = null;
    //application language
    private int applicationLanguage = 0;
    //Graph units
    private int units = 0;

    //graph background color
    private int graphBackColor = 1;
    //graph color
    private int graphColor = 0;

    //graph font size
    private int fontSize = 10;
    // connection type is bluetooth
    private int connectionType = 0;
    // default baud rate for USB is 38400
    private int baudRate = 8;
    private int graphicsLibType = 0;

    private boolean allowMultipleDrogueMain = false;
    private boolean fullUSBSupport = false;

    private boolean say_connecteddisconnected_event = false;
    private boolean say_acquisiation_satellite_event = false;
    private boolean say_distance_event = false;
    private boolean say_notconnected_event = false;
    private boolean say_altitude_event = false;
    private boolean say_landing_event = false;
    private boolean say_burnout_event = false;
    private boolean say_warning_event = false;
    private boolean say_liftoff_event = false;
    private float rocketLatitude = 0.0f;
    private float rocketLongitude = 0.0f;

    private int telemetryVoice = 0;

    //map color
    private int mapColor = 0;
    //map type
    private int mapType = 2;

    private boolean allowManualRecording = true;
    private boolean useOpenMap = true;

    public GlobalConfig(Context current) {
        context = current;
        appConfig = context.getSharedPreferences("BearTrackerCfg", MODE_PRIVATE);
        edit = appConfig.edit();

        appCfgData = new AppConfigData(context);

    }

    public void ResetDefaultConfig() {
        applicationLanguage = 0; // default to english
        graphBackColor = 1;
        graphColor = 0;
        mapColor = 0;
        mapType = 2;
        fontSize = 10;
        units = 0; //default to meters
        baudRate = 8; // default to 38400 baud
        connectionType = 0;
        graphicsLibType = 1; //Default to MP android chart lib
        allowMultipleDrogueMain = false;
        fullUSBSupport = false;

        say_connecteddisconnected_event = false;
        say_acquisiation_satellite_event = false;
        say_distance_event = false;
        say_notconnected_event = false;

        say_altitude_event = false;
        say_landing_event = false;
        say_burnout_event = false;
        say_warning_event = false;
        say_liftoff_event = false;
        telemetryVoice = 0;
        rocketLatitude = 0.0f;
        rocketLongitude = 0.0f;
        allowManualRecording = true;
        useOpenMap = true;
    }

    public void ReadConfig() {
        try {
            //Application language
            int applicationLanguage = appConfig.getInt("AppLanguage", 0);
            setApplicationLanguage(applicationLanguage);

            //Application Units
            int appUnit = appConfig.getInt("Units", 0);
            setUnits(appUnit);

            //Graph color
            int graphColor = appConfig.getInt("GraphColor", 0);
            setGraphColor(graphColor);

            //Graph Background color
            int graphBackColor = appConfig.getInt("GraphBackColor", 1);
            setGraphBackColor(graphBackColor);

            //Map color
            int mapColor = appConfig.getInt("MapColor", 1);
            setMapColor(mapColor);

            //Map type
            int mapType = appConfig.getInt("MapType", 2);
            setMapType(mapType);

            //Font size
            int fontSize = appConfig.getInt("FontSize", 10);
            setFontSize(fontSize);

            //Baud rate
            int baudRate = appConfig.getInt("BaudRate", 8);
            setBaudRate(baudRate);

            //Connection type
            int connectionType = appConfig.getInt("ConnectionType", 0);
            setConnectionType(connectionType);

            //Graphics Lib Type
            int graphicsLibType = appConfig.getInt("GraphicsLibType", 1);
            setGraphicsLibType(graphicsLibType);

            //Allow multiple drogue and main
            boolean allowMultipleDrogueMain = appConfig.getBoolean("AllowMultipleDrogueMain", false);
            setAllowMultipleDrogueMain(allowMultipleDrogueMain);

            //enable full USB support
            boolean fullUSBSupport = appConfig.getBoolean("fullUSBSupport", false);
            setFullUSBSupport(fullUSBSupport);

            //say_connecteddisconnected_event
            boolean say_connecteddisconnected_event = appConfig.getBoolean("say_connecteddisconnected_event", false);
            setConnectedDisconnected_event(say_connecteddisconnected_event);

            //say_acquisition_satellite_event
            boolean say_acquisition_satellite_event = appConfig.getBoolean("say_acquisition_satellite_event", false);
            setAcquisition_satellite_event(say_acquisition_satellite_event);

            //say_distance_event
            boolean say_distance_event = appConfig.getBoolean("say_distance_event", false);
            setDistance_event(say_distance_event);

            //say_notconnected_event
            boolean say_notconnected_event = appConfig.getBoolean("say_notconnected_event", false);
            setNotConnected_event(say_notconnected_event);

            //altitude_event
            boolean say_altitude_event = appConfig.getBoolean("say_altitude_event", false);
            setAltitude_event(say_altitude_event);

            //landing_event
            boolean say_landing_event = appConfig.getBoolean("say_landing_event", false);
            setLanding_event(say_landing_event);

            //burnout_event
            boolean say_burnout_event = appConfig.getBoolean("say_burnout_event", false);
            setBurnout_event(say_burnout_event);

            //warning_event
            boolean say_warning_event = appConfig.getBoolean("say_warning_event", false);
            setWarning_event(say_warning_event);

            //liftoff_event
            boolean say_liftoff_event = appConfig.getBoolean("say_liftoff_event", false);
            setLiftOff_event(say_liftoff_event);

            //telemetryVoice
            int telemetryVoice = appConfig.getInt("telemetryVoice", 0);
            setTelemetryVoice(telemetryVoice);

            //rocketLatitude
            float rocketLatitude = appConfig.getFloat("rocketLatitude", 0.0f);
            setRocketLatitude(rocketLatitude);

            //rocketLongitude
            float rocketLongitude = appConfig.getFloat("rocketLongitude", 0.0f);
            setRocketLongitude(rocketLongitude);

            //allowManualRecording
            boolean allowManualRecording = appConfig.getBoolean("allowManualRecording", false);
            setManualRecording(allowManualRecording);

            //useOpenMap
            boolean useOpenMap = appConfig.getBoolean("useOpenMap", false);
            setUseOpenMap(useOpenMap);
        } catch (Exception e) {

        }
    }

    public void SaveConfig() {
        edit.putInt("AppLanguage", getApplicationLanguage());
        edit.putInt("Units", getUnits());
        edit.putInt("GraphColor", getGraphColor());
        edit.putInt("GraphBackColor", getGraphBackColor());
        edit.putInt("MapColor", getMapColor());
        edit.putInt("MapType", getMapType());
        edit.putInt("FontSize", getFontSize());
        edit.putInt("BaudRate", getBaudRate());
        edit.putInt("ConnectionType", getConnectionType());
        edit.putInt("GraphicsLibType", getGraphicsLibType());
        edit.putBoolean("AllowMultipleDrogueMain", getAllowMultipleDrogueMain());
        edit.putBoolean("fullUSBSupport", getFullUSBSupport());

        edit.putBoolean("say_connecteddisconnected_event", getConnectedDisconnected_event());
        edit.putBoolean("say_acquisition_satellite_event", getAcquisition_satellite_event());
        edit.putBoolean("say_distance_event", getDistance_event());
        edit.putBoolean("say_notconnected_event", getNotConnected_event());

        edit.putBoolean("say_altitude_event", getAltitude_event());
        edit.putBoolean("say_landing_event", getLanding_event());
        edit.putBoolean("say_burnout_event", getBurnout_event());
        edit.putBoolean("say_warning_event", getWarning_event());
        edit.putBoolean("say_liftoff_event", getLiftOff_event());
        edit.putInt("telemetryVoice", getTelemetryVoice());
        edit.putFloat("rocketLatitude", getRocketLatitude());
        edit.putFloat("rocketLongitude", getRocketLongitude());
        edit.putBoolean("allowManualRecording", getManualRecording());
        edit.putBoolean("useOpenMap", getUseOpenMap());
        edit.commit();

    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int value) {
        fontSize = value;
    }

    public int getApplicationLanguage() {
        return applicationLanguage;
    }

    public void setApplicationLanguage(int value) {
        applicationLanguage = value;
    }

    //return the unit id
    public int getUnits() {
        return units;
    }

    public String getUnitsValue() {
        return appCfgData.getUnitsByNbr(units);
    }

    //set the unit by id
    public void setUnits(int value) {
        units = value;
    }

    public int getGraphColor() {
        return graphColor;
    }

    public void setGraphColor(int value) {
        graphColor = value;
    }

    public int getGraphBackColor() {
        return graphBackColor;
    }

    public void setGraphBackColor(int value) {
        graphBackColor = value;
    }

    public int getMapColor() {
        return mapColor;
    }

    public void setMapColor(int value) {
        mapColor = value;
    }

    public int getMapType() {
        return mapType;
    }

    public void setMapType(int value) {
        mapType = value;
    }

    //get the id of the current connection type
    public int getConnectionType() {
        return connectionType;
    }

    //get the name of the current connection type
    public String getConnectionTypeValue() {
        return appCfgData.getConnectionTypeByNbr(connectionType);
    }

    public void setConnectionType(int value) {
        connectionType = value;
    }

    public int getGraphicsLibType() {
        return graphicsLibType;
    }

    public String getGraphicsLibTypeValue() {
        return appCfgData.getGraphicsLibTypeByNbr(graphicsLibType);
    }

    public void setGraphicsLibType(int value) {
        graphicsLibType = value;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public String getBaudRateValue() {
        return appCfgData.getBaudRateByNbr(baudRate);
    }

    public void setBaudRate(int value) {
        baudRate = value;
    }


    public void setAllowMultipleDrogueMain(boolean value) {
        allowMultipleDrogueMain = value;
    }

    public boolean getAllowMultipleDrogueMain() {
        return allowMultipleDrogueMain;
    }

    public void setFullUSBSupport(boolean value) {
        fullUSBSupport = value;
    }

    public boolean getFullUSBSupport() {
        return fullUSBSupport;
    }



    public void setConnectedDisconnected_event(boolean value) {
        say_connecteddisconnected_event = value;
    }

    public boolean getConnectedDisconnected_event() {
        return say_connecteddisconnected_event;
    }

    public void setAcquisition_satellite_event(boolean value) {
        say_acquisiation_satellite_event = value;
    }

    public boolean getAcquisition_satellite_event() {
        return say_acquisiation_satellite_event;
    }

    public void setDistance_event(boolean value) {
        say_distance_event = value;
    }

    public boolean getDistance_event() {
        return say_distance_event;
    }

    public void setNotConnected_event(boolean value) {
        say_notconnected_event = value;
    }

    public boolean getNotConnected_event() {
        return say_notconnected_event;
    }


    public void setAltitude_event(boolean value) {
        say_altitude_event = value;
    }

    public boolean getAltitude_event() {
        return say_altitude_event;
    }

    public void setLanding_event(boolean value) {
        say_landing_event = value;
    }

    public boolean getLanding_event() {
        return say_landing_event;
    }

    public void setBurnout_event(boolean value) {
        say_burnout_event = value;
    }

    public boolean getBurnout_event() {
        return say_burnout_event;
    }

    public void setWarning_event(boolean value) {
        say_warning_event = value;
    }

    public boolean getWarning_event() {
        return say_warning_event;
    }

    public void setLiftOff_event(boolean value) {
        say_liftoff_event = value;
    }

    public boolean getLiftOff_event() {
        return say_liftoff_event;
    }

    public void setTelemetryVoice(int value) {
        telemetryVoice = value;
    }

    public int getTelemetryVoice() {
        return telemetryVoice;
    }

    public void setRocketLatitude(float value) {
        rocketLatitude = value;
    }

    public float getRocketLatitude() {
        return rocketLatitude;
    }

    public void setRocketLongitude(float value) {
        rocketLongitude = value;
    }

    public float getRocketLongitude() {
        return rocketLongitude;
    }

    public void setManualRecording(boolean value) {
        allowManualRecording = value;
    }

    public boolean getManualRecording() {
        return allowManualRecording;
    }

    public void setUseOpenMap(boolean value) {
        useOpenMap = value;
    }

    public boolean getUseOpenMap() {
        return useOpenMap;
    }

    public int ConvertFont(int font) {
        return font + 8;
    }

    public int ConvertColor(int col) {

        int myColor = 0;

        switch (col) {

            case 0:
                myColor = Color.BLACK;
                break;
            case 1:
                myColor = Color.WHITE;
                break;
            case 2:
                myColor = Color.MAGENTA;
                break;
            case 3:
                myColor = Color.BLUE;
                break;
            case 4:
                myColor = Color.YELLOW;
                break;
            case 5:
                myColor = Color.GREEN;
                break;
            case 6:
                myColor = Color.GRAY;
                break;
            case 7:
                myColor = Color.CYAN;
                break;
            case 8:
                myColor = Color.DKGRAY;
                break;
            case 9:
                myColor = Color.LTGRAY;
                break;
            case 10:
                myColor = Color.RED;
                break;
        }
        return myColor;
    }
}
