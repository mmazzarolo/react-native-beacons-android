package com.mmazzarolo.beaconsandroid;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.telecom.Call;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matteo on 12/04/16.
 */
public class BeaconsAndroidConsumer implements BeaconConsumer {
    public interface Callback {
        void onBeaconServiceConnect();
    }

    Context applicationContext;

    public BeaconsAndroidConsumer(Context context) {
        this.applicationContext = context;
    }

    @Override
    public void onBeaconServiceConnect() {
        ((Callback) applicationContext).onBeaconServiceConnect();
    }

    @Override
    public Context getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        applicationContext.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return applicationContext.bindService(intent, serviceConnection, i);
    }
}
