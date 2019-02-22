package com.victor.dlna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.victor.dlna.data.ClingDevice;
import com.victor.dlna.data.IDevice;
import com.victor.dlna.interfaces.BrowseRegistryListener;
import com.victor.dlna.interfaces.DeviceListChangedListener;
import com.victor.dlna.module.ClingUpnpService;
import com.victor.dlna.module.UPnPDiscovery;
import com.victor.dlna.util.UPnPDeviceFinder;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.ModelDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";

    private UPnPDeviceFinder mDevfinder  = null;

    /** 用于监听发现设备 */
    private BrowseRegistryListener mBrowseRegistryListener = new BrowseRegistryListener();

    private ServiceConnection mUpnpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "mUpnpServiceConnection onServiceConnected");

            ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
            ClingUpnpService beyondUpnpService = binder.getService();
            beyondUpnpService.getRegistry().addListener(mBrowseRegistryListener);
            beyondUpnpService.getControlPoint().search();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "mUpnpServiceConnection onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
//        searchUPnPdevices();
    }

    private void initialize () {
        bindServices();

        // 设置发现设备监听
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        ClingDevice info = (ClingDevice) device;
                        Device mDevice = info.getDevice();
                        DeviceDetails dd = mDevice.getDetails();

                        Log.e(TAG,"dd.getFriendlyName() = " + dd.getFriendlyName());
                        Log.e(TAG,"dd.getManufacturer() = " + dd.getManufacturerDetails().getManufacturer());
                        Log.e(TAG,"dd.getManufacturerURI() = " + dd.getManufacturerDetails().getManufacturerURI());
                        Log.e(TAG,"dd.getModelName() = " + dd.getModelDetails().getModelName());
                        Log.e(TAG,"dd.getModelNumber() = " + dd.getModelDetails().getModelNumber());
                        Log.e(TAG,"dd.getModelURI() = " + dd.getModelDetails().getModelURI());

                    }
                });
            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
            }
        });
    }
    private void bindServices() {
        // Bind UPnP service
        Intent upnpServiceIntent = new Intent(MainActivity.this, ClingUpnpService.class);
        bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unbindService(mUpnpServiceConnection);
    }

    private void searchUPnPdevices(){
        Log.e(TAG, "searchUPnPdevices");

        if(mDevfinder == null){
            mDevfinder = new UPnPDeviceFinder(true);
        }

        new SearchUPnPdevicesTask().execute();
    }

    class SearchUPnPdevicesTask extends AsyncTask<Void, Void, ArrayList<String>> {

        private Exception exception;

        protected ArrayList<String> doInBackground(Void...v) {
            return mDevfinder.getUPnPDevicesList();
        }

        protected void onPostExecute(ArrayList<String> devList) {
            for (String device : devList) {
//                if (device.contains("MediaRenderer")) {
                    Log.e(TAG,"---------------------------------------------------");
                    HashMap<String, String> map = parseRaw(device);
                    for(Map.Entry<String, String> entry: map.entrySet()) {
                        Log.e(TAG,entry.getKey() + "------>" + entry.getValue());
                    }
//                }
            }
        }
    }

    private static HashMap<String, String> parseRaw(String raw) {
        HashMap<String, String> results = new HashMap<>();
        for (String line : raw.split("\r\n")) {
            int colon = line.indexOf(":");
            if (colon != -1) {
                String key = line.substring(0, colon).trim().toLowerCase();
                String value = line.substring(colon + 1).trim();
                results.put("upnp_" + key, value);
            }
        }
        return results;
    }
}
