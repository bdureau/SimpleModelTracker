package com.bdureau.simplemodeltracker;
/**
 * @description: This class instanciate pretty much everything including the connection
 * @author: boris.dureau@neuf.fr
 **/

import android.Manifest;
import android.app.Application;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.content.ContextCompat;


import com.bdureau.simplemodeltracker.config.GlobalConfig;
import com.bdureau.simplemodeltracker.connection.BluetoothConnection;
import com.bdureau.simplemodeltracker.connection.UsbConnection;

import java.io.IOException;
import java.io.InputStream;

import java.util.Locale;

/**
 * @description: This is quite a major class used everywhere because it can point to your connection,
 * appconfig
 * @author: boris.dureau@neuf.fr
 **/
public class ConsoleApplication extends Application {
    private static boolean DataReady = false;
    public long lastReceived = 0;
    public String commandRet = "";

    private boolean exit = false;
    private GlobalConfig AppConf = null;
    private String address, moduleName;
    private String myTypeOfConnection = "bluetooth";

    private BluetoothConnection BTCon = null;
    private UsbConnection UsbCon = null;

    private Handler mHandler;

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public String lastReadResult;
    public String lastData;

    @Override
    public void onCreate() {

        super.onCreate();
        AppConf = new GlobalConfig(this);
        AppConf.ReadConfig();
        BTCon = new BluetoothConnection();
        UsbCon = new UsbConnection();

        myTypeOfConnection = AppConf.getConnectionTypeValue();
    }

    public void setConnectionType(String TypeOfConnection) {
        myTypeOfConnection = TypeOfConnection;
    }

    public String getConnectionType() {
        return myTypeOfConnection;
    }

    public void setAddress(String bTAddress) {
        address = bTAddress;
    }

    public String getAddress() {
        return address;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String bTmoduleName) {
        moduleName = bTmoduleName;
    }

    public InputStream getInputStream() {
        InputStream tmpIn = null;
        if (myTypeOfConnection.equals("bluetooth")) {
            tmpIn = BTCon.getInputStream();
        } else {
            tmpIn = UsbCon.getInputStream();
        }
        return tmpIn;
    }

    public void setConnected(boolean Connected) {
        if (myTypeOfConnection.equals("bluetooth")) {
            BTCon.setBTConnected(Connected);
        } else {
            UsbCon.setUSBConnected(Connected);
        }
    }

    public UsbConnection getUsbCon() {
        return UsbCon;
    }

    public boolean getConnected() {
        boolean ret = false;
        if (myTypeOfConnection.equals("bluetooth")) {
            ret = BTCon.getBTConnected();
        } else {
            ret = UsbCon.getUSBConnected();
        }
        return ret;
    }

    // connect to the bluetooth adapter
    public boolean connect() {
        boolean state = false;
        //appendLog("connect:");
        Log.d("TAG", "connecting BT ...");
        if (myTypeOfConnection.equals("bluetooth")) {
            Log.d("TAG", "connecting BT 2...");
            if (ContextCompat.checkSelfPermission(ConsoleApplication.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    //getApplicationContext()
                    Log.d("TAG", "connecting BT 3...");
                    //ActivityCompat.requestPermissions(this.getApplicationContext(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return false;
                }
            }
            state = BTCon.connect(address, getApplicationContext());
            Log.d("TAG", "connecting BT 4...");
            setConnectionType("bluetooth");

            /*if (!isConnectionValid()) {
                Disconnect();
                state = false;
            }*/
            if (!state)
                Disconnect();
        }
        return state;
    }

    // connect to the USB
    public boolean connect(UsbManager usbManager, UsbDevice device, int baudRate) {
        boolean state = false;
        if (myTypeOfConnection.equals("usb")) {
            state = UsbCon.connect(usbManager, device, baudRate);
            setConnectionType("usb");

            if (!state)
                Disconnect();
        }
        return state;
    }

    public void Disconnect() {
        if (myTypeOfConnection.equals("bluetooth")) {
            BTCon.Disconnect();
        } else {
            UsbCon.Disconnect();
        }
    }

    public void flush() {
        if (myTypeOfConnection.equals("bluetooth")) {
            BTCon.flush();
        }
    }

    public void write(String data) {
        if (myTypeOfConnection.equals("bluetooth")) {
            BTCon.write(data);
        } else {
            UsbCon.write(data);
        }
    }

    public void clearInput() {
        if (myTypeOfConnection.equals("bluetooth")) {
            BTCon.clearInput();
        } else {
            UsbCon.clearInput();
        }
    }

    public void setExit(boolean b) {
        this.exit = b;
    }

    public String ReadResult(long timeout) {

        // Reads in data while data is available

        this.exit = false;
        lastData = "";
        String fullBuff = "";
        String myMessage = "";
        lastReceived = System.currentTimeMillis();
        try {

            while (!this.exit) {
                if ((System.currentTimeMillis() - lastReceived) > timeout)
                    this.exit = true;
                if (getInputStream() != null)
                    if (getInputStream().available() > 0) {
                        // Read in the available character
                        char ch = (char) getInputStream().read();
                        lastData = lastData + ch;
                        if (ch == '$') {
                            // read entire sentence until the end
                            String tempBuff = "";
                            while (ch != '\n') {
                                // this is not the end of our command
                                ch = (char) getInputStream().read();
                                if (ch != '\r')
                                    if (ch != '\n')
                                        tempBuff = tempBuff
                                                + Character.toString(ch);
                            }
                            if (ch == '\r') {
                                ch = (char) getInputStream().read();
                            }

                            // Sentence currentSentence = null;
                            String currentSentence[] = new String[50];
                            if (!tempBuff.isEmpty()) {
                                //currentSentence = readSentence(tempBuff);
                                currentSentence = tempBuff.split(",");

                                fullBuff = fullBuff + tempBuff;
                            }

                            //long chk = 0;
                            if (currentSentence != null)
                                if (currentSentence.length > 2)
                                    switch (currentSentence[0]) {
                                        case "GPGGA":

                                            if (mHandler != null) {
                                                Log.d("Cons", currentSentence[0]);
                                                // Value 1 contain the current altitude
                                                if (currentSentence.length > 1) {
                                                    mHandler.obtainMessage(1, String.valueOf("$" + tempBuff)).sendToTarget();
                                                    Log.d("Cons", tempBuff);
                                                }
                                            }
                                            break;

                                        case "UNKNOWN":
                                            setDataReady(true);
                                            commandRet = currentSentence[0];
                                            break;
                                        default:

                                            break;
                                    }
                        }
                    }
            }
        } catch (IOException e) {
            myMessage = myMessage + " " + "error:" + e.getMessage();
        }
        return myMessage;
    }

    public void setDataReady(boolean value) {
        DataReady = value;
    }

    public boolean getDataReady() {
        return DataReady;
    }


    public Configuration getAppLocal() {

        Locale locale = null;
        if (AppConf.getApplicationLanguage() == 1) {
            locale = Locale.FRENCH;//new Locale("fr_FR");
        } else if (AppConf.getApplicationLanguage() == 2) {
            locale = Locale.ENGLISH;//new Locale("en_US");
        } else {
            locale = Locale.getDefault();
        }


        Configuration config = new Configuration();
        config.locale = locale;
        return config;

    }


    public GlobalConfig getAppConf() {
        return AppConf;
    }

    public void setAppConf(GlobalConfig value) {
        AppConf = value;
    }
}

