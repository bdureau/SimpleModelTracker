package com.bdureau.simplemodeltracker.config;

import android.content.Context;

import com.bdureau.simplemodeltracker.R;

/**
 *   @description: This class define all the values for the application conf drop down
 *
 *   @author: boris.dureau@neuf.fr
 *
 **/
public class AppConfigData {

    private Context context;

    // cannot initialize those that require translations
    //"English", "French", "Phone language"
    private String[] itemsLanguages = null;

    private String[] itemsColor = null;
    private String[] itemsModel = null;

    //"Meters", "Feet"
    private String[] itemsUnits = null;
    private String[] itemsFontSize = new String[]{"8","9", "10", "11", "12","13",
            "14", "15", "16", "17", "18", "19", "20", "25", "30", "40"};

    private String[] itemsBaudRate = new String[]{ "300",
            "1200",
            "2400",
            "4800",
            "9600",
            "14400",
            "19200",
            "28800",
            "38400",
            "57600",
            "115200",
            "230400"};
    private String[] itemsConnectionType = new String[]{ "bluetooth",
            "usb"};



    private String rocketLatitude = "0.0";
    private String rocketLongitude = "0.0";


    public AppConfigData(Context current)
    {
        context = current;

        itemsLanguages = new String[]{
                context.getResources().getString(R.string.phone_language),//"Phone language"
                context.getResources().getString(R.string.phone_english)// "English",

        };
        itemsUnits = new String[]{
                context.getResources().getString(R.string.config_unit_meters),//"Meters",
                context.getResources().getString(R.string.config_unit_feet)  //"Feet"
        };

        itemsColor = new String[]{
                context.getResources().getString(R.string.color_black), //"BLACK",
                context.getResources().getString(R.string.color_white), //"WHITE",
                context.getResources().getString(R.string.color_magenta), //"MAGENTA",
                context.getResources().getString(R.string.color_blue), //"BLUE",
                context.getResources().getString(R.string.color_yellow), //"YELLOW",
                context.getResources().getString(R.string.color_green), //"GREEN",
                context.getResources().getString(R.string.color_gray), //"GRAY",
                context.getResources().getString(R.string.color_cyan), //"CYAN",
                context.getResources().getString(R.string.color_dkgray), //"DKGRAY",
                context.getResources().getString(R.string.color_ltgray), //"LTGRAY",
                context.getResources().getString(R.string.color_red) //"RED"
        };

        itemsModel = new String[] {context.getResources().getString(R.string.model_rocket), //rocket
                context.getResources().getString(R.string.model_boat), //boat
                context.getResources().getString(R.string.model_car), //car
                context.getResources().getString(R.string.model_ballon), //ballon
                context.getResources().getString(R.string.model_plane) //plane
        };
    }
    public String [] getItemsLanguages() {
        return itemsLanguages;
    }
    public String getLanguageByNbr(int langNbr) {
        return itemsLanguages [langNbr];
    }

    public String [] getItemsColor() {
        return itemsColor;
    }

    public String  getColorByNbr(int colorNbr) {
        return itemsColor[colorNbr];
    }

    public String[] getItemsUnits() {
        return itemsUnits;
    }
    public String getUnitsByNbr(int unitNbr) {
        return itemsUnits[unitNbr];
    }

    public String[] getItemsFontSize() {
        return itemsFontSize;
    }
    public String getFontSizeByNbr(int fontSize) {
        return itemsFontSize[fontSize];
    }
    public String[] getItemsBaudRate () {
        return itemsBaudRate;
    }

    public String getBaudRateByNbr(int baudRateNbr) {
        return itemsBaudRate[baudRateNbr];
    }

    public String[] getItemsConnectionType () {
        return itemsConnectionType;
    }

    public String getConnectionTypeByNbr (int connectionTypeNbr) {
        return itemsConnectionType[connectionTypeNbr];
    }
    public String[] getItemsModelType() {return itemsModel;}
    public String getModelTypeByNbr(int modelType) {return itemsModel[modelType];}

    public String [] getItemsColorMap() {
        return itemsColor;
    }
    public String  getColorMapByNbr(int colorNbr) {
        return itemsColor[colorNbr];
    }


}
