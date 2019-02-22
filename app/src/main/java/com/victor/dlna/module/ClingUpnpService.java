package com.victor.dlna.module;

import android.content.Intent;
import android.os.IBinder;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.registry.Registry;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: ClingUpnpService.java
 * Author: Victor
 * Date: 2019/2/21 14:46
 * Description:
 * -----------------------------------------------------------------
 */
public class ClingUpnpService extends AndroidUpnpServiceImpl {

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new LocalBinder();
    }

    public Registry getRegistry() {
        return upnpService.getRegistry();
    }

    public ControlPoint getControlPoint() {
        return upnpService.getControlPoint();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public ClingUpnpService getService() {
            return ClingUpnpService.this;
        }
    }
}
